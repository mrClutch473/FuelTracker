from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.models.user import User
from app.schemas.stats import SummaryResponse, MonthlyResponse, MonthlyData, TrendResponse, TrendPoint
from app.services.auth import get_db, get_current_user
from app.services.calculator import get_summary_stats, get_monthly_stats, get_consumption_trend

router = APIRouter(prefix="/stats", tags=["stats"])


@router.get("/summary", response_model=SummaryResponse)
async def summary(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    stats = get_summary_stats(db, user_id=current_user.id)
    return SummaryResponse(**stats)


@router.get("/monthly", response_model=MonthlyResponse)
async def monthly(
    months: int = Query(6, ge=1, le=60),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    data = get_monthly_stats(db, user_id=current_user.id, months=months)
    return MonthlyResponse(user_id=current_user.id, data=[MonthlyData(**d) for d in data])


@router.get("/consumption-trend", response_model=TrendResponse)
async def consumption_trend(
    limit: int = Query(20, ge=1, le=100),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    points = get_consumption_trend(db, user_id=current_user.id, limit=limit)
    return TrendResponse(user_id=current_user.id, points=[TrendPoint(**p) for p in points])
