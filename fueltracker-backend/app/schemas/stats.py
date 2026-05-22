from pydantic import BaseModel
from typing import Optional


# --- Сводная статистика пользователя ---

class SummaryResponse(BaseModel):
    """Общая статистика по всем заправкам текущего пользователя."""
    user_id: int
    total_spent: float
    total_liters: float
    avg_consumption: Optional[float]
    best_consumption: Optional[float]
    worst_consumption: Optional[float]
    avg_price_per_liter: float
    refuels_count: int
    total_km: int


# --- Месячная статистика ---

class MonthlyData(BaseModel):
    """Данные одного месяца."""
    month: str
    total_spent: float
    total_liters: float
    avg_consumption: Optional[float]
    avg_price: float
    refuels_count: int


class MonthlyResponse(BaseModel):
    """Месячная статистика текущего пользователя."""
    user_id: int
    data: list[MonthlyData]


# --- Тренды расхода топлива ---

class TrendPoint(BaseModel):
    """Одна точка на графике расхода топлива."""
    date: str
    consumption: Optional[float]
    odometer: int


class TrendResponse(BaseModel):
    """Тренд расхода топлива текущего пользователя."""
    user_id: int
    points: list[TrendPoint]