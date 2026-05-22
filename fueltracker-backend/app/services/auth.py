from passlib.context import CryptContext
from fastapi import Depends, HTTPException, Request
from sqlalchemy.orm import Session
from app.database import SessionLocal

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


# ----- Пароли -----

def hash_password(plain_password: str) -> str:
    """Хэшировать пароль через bcrypt."""
    return pwd_context.hash(plain_password)


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Проверить пароль против bcrypt-хэша."""
    return pwd_context.verify(plain_password, hashed_password)


# ----- DB dependency (единое место, чтобы не дублировать в каждом роутере) -----

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


# ----- Текущий пользователь из сессии -----

def get_current_user(request: Request, db: Session = Depends(get_db)):
    """
    Читает user_id из cookie-сессии и возвращает объект User.
    Бросает 401, если пользователь не авторизован или не найден.
    """
    from app.models.user import User

    user_id = request.session.get("user_id")
    if not user_id:
        raise HTTPException(status_code=401, detail="Not authenticated")

    user = db.query(User).filter(User.id == user_id).first()
    if user is None:
        raise HTTPException(status_code=401, detail="User not found")

    return user
