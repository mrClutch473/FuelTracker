from fastapi import APIRouter, Depends, HTTPException, Request
from sqlalchemy.orm import Session

from app.models.user import User
from app.schemas.user import UserCreate, UserLogin, UserResponse
from app.services.auth import hash_password, verify_password, get_db

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/register", response_model=UserResponse, status_code=201)
def register(payload: UserCreate, request: Request, db: Session = Depends(get_db)):
    """
    Регистрация нового пользователя.
    После успешной регистрации пользователь сразу считается авторизованным —
    его id сохраняется в сессии.
    """
    existing = db.query(User).filter(User.email == payload.email).first()
    if existing:
        raise HTTPException(status_code=409, detail="Email already registered")

    user = User(
        email=payload.email,
        hashed_password=hash_password(payload.password),
    )
    db.add(user)
    db.commit()
    db.refresh(user)

    request.session["user_id"] = user.id
    return user


@router.post("/login", response_model=UserResponse)
def login(payload: UserLogin, request: Request, db: Session = Depends(get_db)):
    """
    Авторизация пользователя по email и паролю.
    При успехе user_id записывается в сессию.
    """
    user = db.query(User).filter(User.email == payload.email).first()
    if user is None or not verify_password(payload.password, user.hashed_password):
        raise HTTPException(status_code=401, detail="Invalid email or password")

    request.session["user_id"] = user.id
    return user


@router.post("/logout")
def logout(request: Request):
    """
    Завершение сессии — очищает cookie.
    """
    request.session.clear()
    return {"message": "Logged out successfully"}


@router.get("/me", response_model=UserResponse)
def me(request: Request, db: Session = Depends(get_db)):
    """
    Возвращает данные текущего авторизованного пользователя.
    Удобно для фронтенда при инициализации приложения.
    """
    from app.services.auth import get_current_user
    user = get_current_user(request, db)
    return user
