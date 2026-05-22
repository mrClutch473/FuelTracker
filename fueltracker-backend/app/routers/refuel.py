from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import Optional
from sqlalchemy import func

from app.models.refuel import Refuel
from app.models.user import User
from app.schemas.refuel import RefuelCreate, RefuelResponse
from app.services.auth import get_db, get_current_user
from app.services.calculator import (
    calculate_consumption,
    calculate_total_cost,
    get_previous_refuel,
    validate_new_odometer,
    recalc_consumption_for_next_refuel,
)

router = APIRouter(prefix="/refuels", tags=["refuels"])


def _refuel_to_response(refuel: Refuel) -> RefuelResponse:
    return RefuelResponse(
        id=refuel.id,
        user_id=refuel.user_id,
        liters=refuel.liters,
        price=refuel.price,
        total_cost=refuel.total_cost,
        odometer=refuel.odometer,
        consumption=refuel.consumption,
        note=refuel.note,
        created_at=refuel.created_at,
    )


@router.post("", status_code=201, response_model=RefuelResponse)
async def create_refuel(
    payload: RefuelCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    # 1. Последняя заправка этого пользователя с odometer < текущего
    prev_refuel = get_previous_refuel(db, current_user.id, payload.odometer)

    # 2. Проверка монотонности одометра
    if prev_refuel is not None:
        validate_new_odometer(prev_refuel.odometer, payload.odometer)

    # 3. Стоимость заправки
    total_cost = calculate_total_cost(payload.liters, payload.price)

    # 4. Расход топлива
    consumption = None
    if prev_refuel is not None:
        try:
            consumption = calculate_consumption(
                payload.liters,
                prev_refuel.odometer,
                payload.odometer,
            )
        except ValueError:
            consumption = None

    # 5. Сохранить
    new_refuel = Refuel(
        user_id=current_user.id,
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
    current_user: User = Depends(get_current_user),
):
    # Базовый запрос только по заправкам текущего пользователя
    query = db.query(Refuel).filter(Refuel.user_id == current_user.id)

    if month:
        query = query.filter(
            func.strftime("%Y-%m", Refuel.created_at) == month
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
async def delete_refuel(
    refuel_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    # Ищем заправку строго по user_id — нельзя удалить чужую запись
    refuel = (
        db.query(Refuel)
        .filter(Refuel.id == refuel_id, Refuel.user_id == current_user.id)
        .first()
    )
    if refuel is None:
        raise HTTPException(
            status_code=404,
            detail=f"Refuel with id {refuel_id} not found",
        )

    deleted_id = refuel.id
    deleted_odometer = refuel.odometer
    db.delete(refuel)
    db.commit()

    recalc_consumption_for_next_refuel(db, current_user.id, deleted_id, deleted_odometer)
    return {"message": "Refuel deleted successfully", "id": refuel_id}
