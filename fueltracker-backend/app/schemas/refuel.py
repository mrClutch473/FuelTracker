from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime


class RefuelCreate(BaseModel):
    liters: float = Field(..., gt=0)
    price: float = Field(..., gt=0)
    odometer: int = Field(..., gt=0)
    note: Optional[str] = None


class RefuelResponse(BaseModel):
    id: int
    liters: float
    price: float
    total_cost: float
    odometer: int
    consumption: Optional[float]
    note: Optional[str]
    created_at: datetime

    class Config:
        from_attributes = True
