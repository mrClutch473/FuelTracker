from sqlalchemy import Column, Integer, Float, String, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.database import Base


class Refuel(Base):
    __tablename__ = "refuels"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    liters = Column(Float, nullable=False)
    price = Column(Float, nullable=False)
    total_cost = Column(Float, nullable=False)
    odometer = Column(Integer, nullable=False)
    consumption = Column(Float, nullable=True)
    note = Column(String, nullable=True)
    created_at = Column(DateTime, server_default=func.now())

    owner = relationship("User", back_populates="refuels")
