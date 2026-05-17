from pydantic import BaseModel
from typing import Optional


class SummaryResponse(BaseModel):
    total_spent: float
    total_liters: float
    avg_consumption: Optional[float]
    best_consumption: Optional[float]
    worst_consumption: Optional[float]
    avg_price_per_liter: float
    refuels_count: int
    total_km: int


class MonthlyData(BaseModel):
    month: str
    total_spent: float
    total_liters: float
    avg_consumption: Optional[float]
    avg_price: float
    refuels_count: int


class MonthlyResponse(BaseModel):
    data: list[MonthlyData]


class TrendPoint(BaseModel):
    date: str
    consumption: Optional[float]
    odometer: int


class TrendResponse(BaseModel):
    points: list[TrendPoint]