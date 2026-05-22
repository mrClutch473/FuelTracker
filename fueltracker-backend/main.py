import os
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from starlette.middleware.sessions import SessionMiddleware

from app.database import engine, Base

# Импорт моделей, чтобы SQLAlchemy увидел все таблицы перед create_all
from app.models import user, refuel  # noqa: F401

from app.routers.auth import router as auth_router
from app.routers.refuel import router as refuel_router
from app.routers.stats import router as stats_router

# Секретный ключ для подписи сессионного cookie.
# В продакшне задайте через переменную окружения SESSION_SECRET.
SESSION_SECRET = os.getenv("SESSION_SECRET", "change-me-in-production")

app = FastAPI(
    title="FuelTracker API",
    description="Backend for FuelTracker — vehicle refuel tracking application",
    version="2.0.0",
)

# --- Middleware ---

# SessionMiddleware должен быть добавлен до CORSMiddleware
app.add_middleware(
    SessionMiddleware,
    secret_key=SESSION_SECRET,
    session_cookie="ft_session",
    max_age=60 * 60 * 24 * 30,   # 30 дней
    https_only=False,             # поставьте True на продакшне
    same_site="lax",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],          # уточните под свой фронтенд
    allow_credentials=True,       # нужно для передачи cookie
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- Таблицы ---
Base.metadata.create_all(bind=engine)

# --- Роутеры ---
app.include_router(auth_router,   prefix="/api/v1")
app.include_router(refuel_router, prefix="/api/v1")
app.include_router(stats_router,  prefix="/api/v1")


@app.get("/health", tags=["system"])
async def health_check():
    return {"status": "ok", "service": "fueltracker-api", "version": "2.0.0"}
