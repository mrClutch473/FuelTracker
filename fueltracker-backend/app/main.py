from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.database import engine, Base
from app.routers.refuel import router as refuel_router
from app.routers.stats import router as stats_router

app = FastAPI(
    title="FuelTracker API",
    description="Backend for FuelTracker — vehicle refuel tracking application",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Создание таблиц (будет выполнено при запуске)
Base.metadata.create_all(bind=engine)

# Роутеры уже включают свои префиксы (/api/v1 добавлен здесь)
app.include_router(refuel_router, prefix="/api/v1")
app.include_router(stats_router, prefix="/api/v1")


@app.get("/health")
async def health_check():
    return {"status": "ok", "service": "fueltracker-api"}
