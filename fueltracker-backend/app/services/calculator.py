# Placeholder for business logic calculations

from fastapi import HTTPException
from app.database import SessionLocal


def calculate_consumption(liters: float, prev_odometer: int, curr_odometer: int) -> float:
    """Расход в литрах на 100 км"""
    distance = curr_odometer - prev_odometer
    if distance <= 0:
        raise ValueError("Distance must be positive")
    return round((liters / distance) * 100, 2)


def calculate_total_cost(liters: float, price_per_liter: float) -> float:
    return round(liters * price_per_liter, 2)


def get_previous_refuel(db_session, current_odometer: int):
    """Возвращает последнюю заправку с odometer < current_odometer (по id)"""
    from app.models.refuel import Refuel
    return db_session.query(Refuel).filter(
        Refuel.odometer < current_odometer
    ).order_by(Refuel.id.desc()).first()


def validate_new_odometer(prev_odometer: int, new_odometer: int):
    """Проверяет, что новый пробег больше предыдущего"""
    if new_odometer <= prev_odometer:
        raise HTTPException(
            status_code=400,
            detail=f"New odometer value must be greater than previous ({prev_odometer})"
        )


def recalc_consumption_for_next_refuel(db_session, deleted_id: int, deleted_odometer: int):
    from app.models.refuel import Refuel

    next_refuel = db_session.query(Refuel).filter(
        Refuel.id > deleted_id
    ).order_by(Refuel.id.asc()).first()

    if next_refuel is None:
        return

    prev_refuel = db_session.query(Refuel).filter(
        Refuel.odometer < next_refuel.odometer,
        Refuel.id != next_refuel.id
    ).order_by(Refuel.id.desc()).first()

    if prev_refuel is None:
        next_refuel.consumption = None
    else:
        try:
            next_refuel.consumption = calculate_consumption(
                next_refuel.liters,
                prev_refuel.odometer,
                next_refuel.odometer
            )
        except ValueError:
            next_refuel.consumption = None

    db_session.add(next_refuel)
    db_session.commit()


# ----- Статистика -----

def get_summary_stats(db_session) -> dict:
    """Возвращает сводную статистику по всем заправкам."""
    from app.models.refuel import Refuel
    refuels = db_session.query(Refuel).order_by(Refuel.id.asc()).all()

    if not refuels:
        return {
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
    total_refuels = len(refuels)
    avg_price = round(total_spent / total_liters, 2) if total_liters > 0 else 0.0

    # Общий пробег: разница между первым и последним odometer
    total_km = refuels[-1].odometer - refuels[0].odometer if len(refuels) > 1 else 0

    # Средний, лучший, худший расход (только записи с не-None consumption)
    consumptions = [r.consumption for r in refuels if r.consumption is not None]
    if consumptions:
        avg_consumption = round(sum(consumptions) / len(consumptions), 2)
        best_consumption = min(consumptions)
        worst_consumption = max(consumptions)
    else:
        avg_consumption = None
        best_consumption = None
        worst_consumption = None

    return {
        "total_spent": round(total_spent, 2),
        "total_liters": round(total_liters, 2),
        "avg_consumption": avg_consumption,
        "best_consumption": best_consumption,
        "worst_consumption": worst_consumption,
        "avg_price_per_liter": avg_price,
        "refuels_count": total_refuels,
        "total_km": total_km,
    }


def get_monthly_stats(db_session, months: int = 6) -> list[dict]:
    """Возвращает статистику по месяцам за последние N месяцев."""
    from app.models.refuel import Refuel
    from sqlalchemy import func

    # Группируем по году-месяцу, сортируем по убыванию, берём последние months
    results = (
        db_session.query(
            func.strftime("%Y-%m", Refuel.created_at).label("month"),
            func.sum(Refuel.total_cost).label("total_spent"),
            func.sum(Refuel.liters).label("total_liters"),
            func.avg(Refuel.consumption).label("avg_consumption"),
            func.avg(Refuel.price).label("avg_price"),
            func.count(Refuel.id).label("refuels_count"),
        )
        .group_by("month")
        .order_by(func.strftime("%Y-%m", Refuel.created_at).desc())
        .limit(months)
        .all()
    )

    data = []
    for row in results:
        # Округление
        total_spent = round(row.total_spent, 2) if row.total_spent is not None else 0.0
        total_liters = round(row.total_liters, 2) if row.total_liters is not None else 0.0
        avg_consumption = round(row.avg_consumption, 2) if row.avg_consumption is not None else None
        avg_price = round(row.avg_price, 2) if row.avg_price is not None else 0.0
        refuels_count = row.refuels_count if row.refuels_count is not None else 0

        data.append({
            "month": row.month,
            "total_spent": total_spent,
            "total_liters": total_liters,
            "avg_consumption": avg_consumption,
            "avg_price": avg_price,
            "refuels_count": refuels_count,
        })

    # Возвращаем в порядке возрастания месяцев (сначала старые)
    data.reverse()
    return data


def get_consumption_trend(db_session, limit: int = 20) -> list[dict]:
    """Возвращает точки для графика тренда расхода (дата, consumption, odometer)."""
    from app.models.refuel import Refuel

    refuels = (
        db_session.query(Refuel)
        .filter(Refuel.consumption.isnot(None))
        .order_by(Refuel.created_at.desc())
        .limit(limit)
        .all()
    )

    points = []
    for r in refuels:
        date_str = r.created_at.strftime("%Y-%m-%d") if r.created_at else ""
        points.append({
            "date": date_str,
            "consumption": r.consumption,
            "odometer": r.odometer,
        })

    # Возвращаем в хронологическом порядке
    points.reverse()
    return points
