from fastapi import HTTPException


# ----- Математика -----

def calculate_consumption(liters: float, prev_odometer: int, curr_odometer: int) -> float:
    """Расход топлива в литрах на 100 км."""
    distance = curr_odometer - prev_odometer
    if distance <= 0:
        raise ValueError("Distance must be positive")
    return round((liters / distance) * 100, 2)


def calculate_total_cost(liters: float, price_per_liter: float) -> float:
    """Стоимость заправки."""
    return round(liters * price_per_liter, 2)


# ----- Вспомогательные запросы (изолированы по user_id) -----

def get_previous_refuel(db_session, user_id: int, current_odometer: int):
    """
    Возвращает последнюю заправку пользователя
    с odometer строго меньше current_odometer.
    """
    from app.models.refuel import Refuel
    return (
        db_session.query(Refuel)
        .filter(Refuel.user_id == user_id, Refuel.odometer < current_odometer)
        .order_by(Refuel.id.desc())
        .first()
    )


def validate_new_odometer(prev_odometer: int, new_odometer: int):
    """Проверяет, что новый пробег больше предыдущего."""
    if new_odometer <= prev_odometer:
        raise HTTPException(
            status_code=400,
            detail=f"New odometer value must be greater than previous ({prev_odometer})",
        )


def recalc_consumption_for_next_refuel(
    db_session, user_id: int, deleted_id: int, deleted_odometer: int
):
    """
    После удаления заправки пересчитывает consumption
    у следующей заправки того же пользователя.
    """
    from app.models.refuel import Refuel

    # Следующая заправка этого пользователя после удалённой
    next_refuel = (
        db_session.query(Refuel)
        .filter(Refuel.user_id == user_id, Refuel.id > deleted_id)
        .order_by(Refuel.id.asc())
        .first()
    )
    if next_refuel is None:
        return

    # Предыдущая заправка относительно next_refuel (того же пользователя)
    prev_refuel = (
        db_session.query(Refuel)
        .filter(
            Refuel.user_id == user_id,
            Refuel.odometer < next_refuel.odometer,
            Refuel.id != next_refuel.id,
        )
        .order_by(Refuel.id.desc())
        .first()
    )

    if prev_refuel is None:
        next_refuel.consumption = None
    else:
        try:
            next_refuel.consumption = calculate_consumption(
                next_refuel.liters,
                prev_refuel.odometer,
                next_refuel.odometer,
            )
        except ValueError:
            next_refuel.consumption = None

    db_session.add(next_refuel)
    db_session.commit()


# ----- Статистика (все функции принимают user_id) -----

def get_summary_stats(db_session, user_id: int) -> dict:
    """Сводная статистика по всем заправкам пользователя."""
    from app.models.refuel import Refuel

    refuels = (
        db_session.query(Refuel)
        .filter(Refuel.user_id == user_id)
        .order_by(Refuel.id.asc())
        .all()
    )

    if not refuels:
        return {
            "user_id": user_id,
            "total_spent": 0.0,
            "total_liters": 0.0,
            "avg_consumption": None,
            "best_consumption": None,
            "worst_consumption": None,
            "avg_price_per_liter": 0.0,
            "refuels_count": 0,
            "total_km": 0,
        }

    total_spent = sum(r.total_cost for r in refuels)
    total_liters = sum(r.liters for r in refuels)
    avg_price = round(total_spent / total_liters, 2) if total_liters > 0 else 0.0
    total_km = refuels[-1].odometer - refuels[0].odometer if len(refuels) > 1 else 0

    consumptions = [r.consumption for r in refuels if r.consumption is not None]
    if consumptions:
        avg_consumption = round(sum(consumptions) / len(consumptions), 2)
        best_consumption = min(consumptions)
        worst_consumption = max(consumptions)
    else:
        avg_consumption = best_consumption = worst_consumption = None

    return {
        "user_id": user_id,
        "total_spent": round(total_spent, 2),
        "total_liters": round(total_liters, 2),
        "avg_consumption": avg_consumption,
        "best_consumption": best_consumption,
        "worst_consumption": worst_consumption,
        "avg_price_per_liter": avg_price,
        "refuels_count": len(refuels),
        "total_km": total_km,
    }


def get_monthly_stats(db_session, user_id: int, months: int = 6) -> list[dict]:
    """Статистика по месяцам за последние N месяцев для пользователя."""
    from app.models.refuel import Refuel
    from sqlalchemy import func

    results = (
        db_session.query(
            func.strftime("%Y-%m", Refuel.created_at).label("month"),
            func.sum(Refuel.total_cost).label("total_spent"),
            func.sum(Refuel.liters).label("total_liters"),
            func.avg(Refuel.consumption).label("avg_consumption"),
            func.avg(Refuel.price).label("avg_price"),
            func.count(Refuel.id).label("refuels_count"),
        )
        .filter(Refuel.user_id == user_id)
        .group_by("month")
        .order_by(func.strftime("%Y-%m", Refuel.created_at).desc())
        .limit(months)
        .all()
    )

    data = [
        {
            "month": row.month,
            "total_spent": round(row.total_spent, 2) if row.total_spent else 0.0,
            "total_liters": round(row.total_liters, 2) if row.total_liters else 0.0,
            "avg_consumption": round(row.avg_consumption, 2) if row.avg_consumption else None,
            "avg_price": round(row.avg_price, 2) if row.avg_price else 0.0,
            "refuels_count": row.refuels_count or 0,
        }
        for row in results
    ]

    # Возвращаем в хронологическом порядке (старые → новые)
    data.reverse()
    return data


def get_consumption_trend(db_session, user_id: int, limit: int = 20) -> list[dict]:
    """Точки графика тренда расхода топлива для пользователя."""
    from app.models.refuel import Refuel

    refuels = (
        db_session.query(Refuel)
        .filter(Refuel.user_id == user_id, Refuel.consumption.isnot(None))
        .order_by(Refuel.created_at.desc())
        .limit(limit)
        .all()
    )

    points = [
        {
            "date": r.created_at.strftime("%Y-%m-%d") if r.created_at else "",
            "consumption": r.consumption,
            "odometer": r.odometer,
        }
        for r in refuels
    ]

    # Возвращаем в хронологическом порядке
    points.reverse()
    return points
