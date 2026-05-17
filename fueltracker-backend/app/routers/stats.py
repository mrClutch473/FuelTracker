from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from app.database import SessionLocal
from app.schemas.stats import SummaryResponse, MonthlyResponse, MonthlyData, TrendResponse, TrendPoint
from app.services.calculator import get_summary_stats, get_monthly_stats, get_consumption_trend

router = APIRouter(prefix="/stats", tags=["stats"])


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@router.get("/summary", response_model=SummaryResponse)
async def summary(db: Session = Depends(get_db)):
    stats = get_summary_stats(db)
    return SummaryResponse(**stats)


@router.get("/monthly", response_model=MonthlyResponse)
async def monthly(months: int = Query(6, ge=1, le=60), db: Session = Depends(get_db)):
    data = get_monthly_stats(db, months=months)
    return MonthlyResponse(data=[MonthlyData(**d) for d in data])


@router.get("/consumption-trend", response_model=TrendResponse)
async def consumption_trend(limit: int = Query(20, ge=1, le=100), db: Session = Depends(get_db)):
    points = get_consumption_trend(db, limit=limit)
    return TrendResponse(points=[TrendPoint(**p) for p in points])
