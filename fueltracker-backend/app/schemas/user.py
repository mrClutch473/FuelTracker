from pydantic import BaseModel, EmailStr
from datetime import datetime


# --- Запросы ---

class UserCreate(BaseModel):
    """Регистрация нового пользователя."""
    email: EmailStr
    password: str


class UserLogin(BaseModel):
    """Авторизация пользователя."""
    email: EmailStr
    password: str


class UserChangePassword(BaseModel):
    """Смена пароля."""
    current_password: str
    new_password: str


# --- Ответы ---

class UserResponse(BaseModel):
    """Публичные данные пользователя (без пароля)."""
    id: int
    email: str
    created_at: datetime

    class Config:
        from_attributes = True
