from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import Optional
from sqlalchemy import func

from app.database import SessionLocal
from app.models.refuel import Refuel
from app.schemas.refuel import RefuelCreate, RefuelResponse
from app.services.calculator import (
    calculate_consumption,
    calculate_total_cost,
    get_previous_refuel,
    validate_new_odometer,
    recalc_consumption_for_next_refuel,
)
router = APIRouter(prefix="/refuels", tags=["refuels"])


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def _refuel_to_response(refuel: Refuel) -> RefuelResponse:
    return RefuelResponse(
        id=refuel.id,
        liters=refuel.liters,
        price=refuel.price,
        total_cost=refuel.total_cost,
        odometer=refuel.odometer,
        consumption=refuel.consumption,
        note=refuel.note,
        created_at=refuel.created_at,
    )


@router.post("", status_code=201, response_model=RefuelResponse)
async def create_refuel(payload: RefuelCreate, db: Session = Depends(get_db)):
    # 1. Получить последнюю заправку по id (с меньшим odometer)
    prev_refuel = get_previous_refuel(db, payload.odometer)

    # 2. Если есть предыдущая заправка, проверить, что odometer увеличился
    if prev_refuel is not None:
        try:
            validate_new_odometer(prev_refuel.odometer, payload.odometer)
        except HTTPException:
            raise

    # 3. Вычислить total_cost
    total_cost = calculate_total_cost(payload.liters, payload.price)

    # 4. Вычислить consumption, если есть предыдущая заправка
    consumption = None
    if prev_refuel is not None:
        try:
            consumption = calculate_consumption(
                payload.liters,
                prev_refuel.odometer,
                payload.odometer,
            )
        except ValueError:
            # Если distance = 0 (невозможно, но на всякий случай)
            consumption = None

    # 5. Создать запись
    new_refuel = Refuel(
        liters=payload.liters,
        price=payload.price,
        total_cost=total_cost,
        odometer=payload.odometer,
        consumption=consumption,
        note=payload.note,
    )

    db.add(new_refuel)
    db.commit()
    db.refresh(new_refuel)

    return _refuel_to_response(new_refuel)
@router.get("", response_model=dict)
async def list_refuels(
    limit: int = Query(50, ge=1, le=100),
    offset: int = Query(0, ge=0),
    month: Optional[str] = Query(None, pattern=r"^\d{4}-\d{2}$"),
    db: Session = Depends(get_db),
):
    query = db.query(Refuel)

    # Фильтр по месяцу (YYYY-MM)
    if month:
        query = query.filter(
            func.strftime("%Y-%m", Refuel.created_at) == month  # ✅
        )

    total = query.count()

    items = (
        query.order_by(Refuel.created_at.desc())
        .offset(offset)
        .limit(limit)
        .all()
    )

    return {
        "items": [_refuel_to_response(item) for item in items],
        "total": total,
        "limit": limit,
        "offset": offset,
    }


@router.delete("/{refuel_id}", response_model=dict)
async def delete_refuel(refuel_id: int, db: Session = Depends(get_db)):
    refuel = db.query(Refuel).filter(Refuel.id == refuel_id).first()
    if refuel is None:
        raise HTTPException(
            status_code=404,
            detail=f"Refuel with id {refuel_id} not found",
        )

    # Сохраняем refuel для пересчёта consumption следующей заправки
    deleted_id = refuel.id
    deleted_odometer = refuel.odometer
    db.delete(refuel)
    db.commit()
    recalc_consumption_for_next_refuel(db, deleted_id, deleted_odometer)
    return {"message": "Refuel deleted successfully", "id": refuel_id}
