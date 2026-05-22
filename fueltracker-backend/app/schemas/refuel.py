from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime


# --- Запросы ---

class RefuelCreate(BaseModel):
    """Создание записи о заправке для текущего пользователя."""
    liters: float = Field(..., gt=0)
    price: float = Field(..., gt=0)
    odometer: int = Field(..., gt=0)
    note: Optional[str] = None


class RefuelUpdate(BaseModel):
    """Частичное обновление записи о заправке."""
    liters: Optional[float] = Field(None, gt=0)
    price: Optional[float] = Field(None, gt=0)
    odometer: Optional[int] = Field(None, gt=0)
    note: Optional[str] = None


# --- Ответы ---

class RefuelResponse(BaseModel):
    """Полные данные записи о заправке."""
    id: int
    user_id: int
    liters: float
    price: float
    total_cost: float
    odometer: int
    consumption: Optional[float]
    note: Optional[str]
    created_at: datetime

    class Config:
        from_attributes = True
