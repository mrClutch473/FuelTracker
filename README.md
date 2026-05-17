# ⛽ FuelTracker

> **Stack:** Kotlin · FastAPI · SQLite  
> **Version:** 1.1

Android-приложение для учёта заправок и мониторинга расхода топлива. Стилизовано под ночную приборную панель автомобиля: тёмный фон, янтарные акценты, плавные анимации.

---

## Содержание

1. [Функциональность](#1-функциональность)
2. [Дизайн-система](#2-дизайн-система)
3. [Структура проекта](#3-структура-проекта)
4. [Архитектура (Android)](#4-архитектура-android)
5. [Серверная часть](#5-серверная-часть)
6. [Бизнес-логика](#6-бизнес-логика)
7. [Запуск](#7-запуск)

---

## 1. Функциональность

| Экран | Описание |
|---|---|
| **Splash** | Lottie-анимация, название, янтарная полоса прогресса |
| **Home** | Дашборд: кастомный gauge расхода, последняя заправка, сводка за месяц |
| **Add Refuel** | Bottom Sheet: ввод литров / цены / пробега, живой расчёт суммы |
| **History** | Список всех заправок, цветная индикация расхода, swipe-to-delete |
| **Stats** | Графики тренда расхода, цен и трат; 4 сводные карточки |

**Навигация:**
```
SplashFragment → HomeFragment ↔ AddRefuelFragment (Bottom Sheet)
HomeFragment ←→ HistoryFragment ←→ StatsFragment  (BottomNavigation)
```

| Переход | Анимация | Длительность |
|---|---|---|
| Splash → Home | Fade | 400 ms |
| Home → AddRefuel | Slide up | 350 ms |
| AddRefuel → Home | Slide down | 300 ms |
| BottomNav | Crossfade | 250 ms |

---

## 2. Дизайн-система

### Палитра

| Роль | HEX | Применение |
|---|---|---|
| Background | `#0A0D14` | Основной фон |
| Surface | `#12161F` | Карточки, Bottom Sheet |
| Surface Elevated | `#1A1F2E` | Приподнятые карточки |
| Primary (Amber) | `#F5A623` | Кнопки, иконки, акценты |
| Secondary (Blue) | `#4FC3F7` | Графики |
| Success | `#00E676` | Хороший расход |
| Warning | `#FF6B6B` | Повышенный расход |
| Text Primary | `#FFFFFF` | Основной текст |
| Text Secondary | `#8892A4` | Подписи, метки |

### Типографика

| Стиль | Шрифт | Размер | Вес |
|---|---|---|---|
| Display | Orbitron | 32sp | Bold |
| Heading 1 | Inter | 24sp | SemiBold |
| Heading 2 | Inter | 18sp | SemiBold |
| Body | Inter | 14sp | Regular |
| Числа | JetBrains Mono | 16sp | Medium |

Шрифты подключены через `res/font/`: `orbitron_bold.ttf`, `inter_regular.ttf`, `inter_semibold.ttf`, `jetbrains_mono_medium.ttf`.

### Параметры форм

```
Card Corner Radius:    16dp
Button Corner Radius:  12dp
Input Corner Radius:   12dp
Bottom Sheet Corner:   24dp (top only)
```

---

## 3. Структура проекта

```
FuelTracker/
├── fueltracker-backend/
│   └── app/
│       ├── main.py              # Точка входа FastAPI, CORS, роутеры
│       ├── database.py          # SQLite, SessionLocal
│       ├── models/refuel.py     # SQLAlchemy модель
│       ├── schemas/refuel.py    # Pydantic схемы
│       ├── routers/
│       │   ├── refuel.py        # CRUD эндпоинты
│       │   └── stats.py         # Эндпоинты статистики
│       └── services/
│           └── calculator.py   # Расчёт расхода и статистики
│
└── fueltracker-android/
    └── app/src/main/java/.../
        ├── data/
        │   ├── ApiService.kt        # Retrofit интерфейс
        │   ├── FuelRepository.kt    # Репозиторий, маппинг DTO → Domain
        │   └── dto/                 # RefuelDto, StatsDto
        ├── domain/model/            # Refuel.kt, Stats.kt
        └── ui/
            ├── MainActivity.kt      # Host Activity, NavController
            ├── UiState.kt           # sealed class Loading / Success / Error
            ├── screens/
            │   ├── splash/SplashFragment.kt
            │   ├── home/            # HomeFragment, HomeViewModel
            │   ├── add/             # AddRefuelFragment, AddRefuelViewModel
            │   ├── history/         # HistoryFragment, ViewModel, Adapter
            │   └── stats/           # StatsFragment, StatsViewModel
            └── components/
                ├── FuelGaugeView.kt     # Custom Canvas View
                ├── StatCardView.kt      # Карточка статистики
                └── AnimatedCounter.kt   # Анимированный счётчик
```

---

## 4. Архитектура (Android)

**Паттерн:** MVVM + Repository. Каждый экран — отдельный Fragment со своим ViewModel.

```
Fragment  ──observes StateFlow──▶  ViewModel
                                       │ suspend functions
                                   FuelRepository
                                       │
                                   ApiService (Retrofit)
                                       │ HTTP
                                   FastAPI Server
```

### UiState

```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

### ViewModel Factory

```kotlin
companion object {
    fun factory(repository: FuelRepository) = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
    }
}

// В Fragment:
private val viewModel: HomeViewModel by viewModels {
    HomeViewModel.factory(FuelRepository(ApiService.create()))
}
```

### Конфигурация сети

```kotlin
fun create(): ApiService = Retrofit.Builder()
    .baseUrl("http://10.0.2.2:8000/api/v1/")   // 10.0.2.2 = localhost для эмулятора
    .addConverterFactory(GsonConverterFactory.create())
    .client(OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build())
    .build()
    .create(ApiService::class.java)
```

### Зависимости (app/build.gradle.kts)

```kotlin
dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.airbnb.android:lottie:6.4.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.android.material:material:1.12.0")
}
```

### Анимации

| Элемент | Механизм | Длительность |
|---|---|---|
| FuelGaugeView | `ValueAnimator` 0 → значение | 1200 ms |
| Числа (AnimatedCounter) | `ValueAnimator` | 300 ms |
| Карточки в списке | `alpha` + `translationY`, stagger 60 ms | 400 ms |
| Swipe-to-delete collapse | `ItemTouchHelper` | 300 ms |
| Графики | `animateX` / `animateY` (MPAndroidChart) | 800–1000 ms |
| Кнопка "Заправился" | `ObjectAnimator alpha` INFINITE REVERSE | 1500 ms |

---

## 5. Серверная часть

**Стек:** FastAPI · SQLAlchemy · SQLite · Pydantic v2 · Uvicorn

### Схема БД

```sql
CREATE TABLE refuels (
    id          INTEGER  PRIMARY KEY AUTOINCREMENT,
    liters      REAL     NOT NULL,
    price       REAL     NOT NULL,
    total_cost  REAL     NOT NULL,
    odometer    INTEGER  NOT NULL,
    consumption REAL,               -- NULL для первой записи
    note        TEXT,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### API

**Base URL:** `http://10.0.2.2:8000/api/v1` (эмулятор) / `http://<device-ip>:8000/api/v1` (устройство)

| Метод | Endpoint | Описание |
|---|---|---|
| `POST` | `/refuels` | Добавить заправку |
| `GET` | `/refuels` | Список (`limit`, `offset`, `month`) |
| `DELETE` | `/refuels/{id}` | Удалить запись |
| `GET` | `/stats/summary` | Сводка (средний расход, итого) |
| `GET` | `/stats/monthly` | Данные по месяцам (`months=6`) |
| `GET` | `/stats/consumption-trend` | Тренд расхода (`limit=20`) |

---

## 6. Бизнес-логика

### Расчёт расхода (сервер)

```python
consumption = round((liters / (new_odometer - prev_odometer)) * 100, 2)
# Для первой записи: consumption = NULL
```

### Цветовая индикация расхода (клиент)

```kotlin
fun getConsumptionColor(consumption: Float, avgConsumption: Float): Int {
    return when {
        consumption <= avgConsumption * 0.95 -> Color.parseColor("#00E676")  // норма
        consumption <= avgConsumption * 1.10 -> Color.parseColor("#F5A623")  // выше нормы
        else                                 -> Color.parseColor("#FF6B6B")  // критично
    }
}
```

---

## 7. Запуск

### Сервер

```bash
cd fueltracker-backend
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Swagger UI доступен по адресу `http://localhost:8000/docs`.

### Android

Открыть `fueltracker-android` в Android Studio, дождаться синхронизации Gradle, запустить на эмуляторе (API 26+) или реальном устройстве. Убедиться, что сервер запущен и доступен по сети.
