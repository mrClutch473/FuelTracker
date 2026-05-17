# ⛽ FuelTracker — Проектная Документация

> **Версия:** 1.1  
> **Стек:** Kotlin (Android Studio) · FastAPI · SQLite  
> **Уровень:** Учебный  
> **Срок реализации:** 4–5 дней

---

## Содержание

1. [Концепция проекта](#1-концепция-проекта)
2. [Цветовая палитра и дизайн-система](#2-цветовая-палитра-и-дизайн-система)
3. [Структура проекта](#3-структура-проекта)
4. [Экраны приложения](#4-экраны-приложения)
5. [Навигация и переходы между экранами](#5-навигация-и-переходы-между-экранами)
6. [Анимации](#6-анимации)
7. [Серверная часть (fueltracker-backend)](#7-серверная-часть-fueltracker-backend)
8. [Android-приложение (fueltracker-android)](#8-android-приложение-fueltracker-android)
9. [Бизнес-логика и расчёты](#9-бизнес-логика-и-расчёты)
10. [Финальный результат](#10-финальный-результат)

---

## 1. Концепция проекта

**FuelTracker** — мобильное приложение для владельцев автомобилей, позволяющее вести учёт каждой заправки и отслеживать расход топлива в динамике. Приложение визуально стилизовано под ночную приборную панель автомобиля: тёмный фон, янтарные акценты, плавные анимации.

### Что делает пользователь

- Вносит данные после каждой заправки (литры, цена, пробег)
- Видит мгновенный расчёт стоимости и расхода
- Наблюдает за трендами расхода на красивых графиках
- Просматривает историю всех заправок
- Получает итоговую статистику: сколько потрачено, средний расход, динамика цен

### Ключевые принципы

- **Visual-first** — каждый экран визуально самодостаточен и красив
- **Минимум ввода** — пользователь вводит только 3 обязательных поля
- **Мгновенный фидбек** — все расчёты происходят на лету, с анимацией
- **Тёмная тема** — единственная тема, стилизованная под HUD автомобиля

---

## 2. Цветовая палитра и дизайн-система

### Основная палитра

| Роль | Название | HEX | Применение |
|---|---|---|---|
| Background | Deep Space | `#0A0D14` | Основной фон всех экранов |
| Surface | Midnight | `#12161F` | Фон карточек, bottom sheet |
| Surface Elevated | Carbon | `#1A1F2E` | Приподнятые карточки, диалоги |
| Primary | Amber | `#F5A623` | Главный акцент, кнопки, иконки |
| Primary Dim | Amber Glow | `#F5A62333` | Полупрозрачный акцент, свечение |
| Secondary | Electric Blue | `#4FC3F7` | Графики, подсветка данных |
| Success | Neon Green | `#00E676` | Дешёвая заправка, хороший расход |
| Warning | Coral | `#FF6B6B` | Дорогая заправка, плохой расход |
| Text Primary | White | `#FFFFFF` | Основной текст |
| Text Secondary | Silver | `#8892A4` | Подписи, метки |
| Text Hint | Slate | `#3D4455` | Placeholder, неактивные элементы |
| Divider | — | `#1E2435` | Разделители |

### Градиенты

```
Amber Gradient:   #F5A623 → #FF6B35   (кнопка "Заправился", акценты)
Blue Gradient:    #4FC3F7 → #0288D1   (графики)
Card Gradient:    #12161F → #0F1219   (фон карточек, сверху вниз)
Background Glow:  radial от #F5A62310 в центре → #0A0D14 на краях
```

### Типографика

| Стиль | Шрифт | Размер | Вес |
|---|---|---|---|
| Display | Orbitron | 32sp | Bold |
| Heading 1 | Inter | 24sp | SemiBold |
| Heading 2 | Inter | 18sp | SemiBold |
| Body | Inter | 14sp | Regular |
| Caption | Inter | 12sp | Regular |
| Mono (числа) | JetBrains Mono | 16sp | Medium |

Шрифты подключены через `res/font/`:
- `orbitron_bold.ttf`
- `inter_regular.ttf`
- `inter_semibold.ttf`
- `jetbrains_mono_medium.ttf`

Используются через `android:fontFamily="@font/orbitron_bold"` и т.д.

### Формы и скругления

```
Card Corner Radius:       16dp
Button Corner Radius:     12dp
Input Corner Radius:      12dp
Chip Corner Radius:       8dp
FAB Corner Radius:        18dp
Bottom Sheet Corner:      24dp (top only)
```

### Тени и свечение

- Карточки: `elevation = 0dp` — вместо тени используется граница `1dp` цвета `#1E2435`
- Кнопка "Заправился": `box-shadow` янтарного свечения `0 0 24dp #F5A62366`
- Активные элементы: внутренняя подсветка через полупрозрачный слой `#F5A62315`

---

## 3. Структура проекта

```
FuelTracker/
├── fueltracker-backend/
│   ├── app/
│   │   ├── __init__.py
│   │   ├── main.py                  # Точка входа FastAPI, CORS, роутеры
│   │   ├── database.py              # Подключение SQLite, SessionLocal
│   │   ├── models/
│   │   │   └── refuel.py            # SQLAlchemy модель Refuel
│   │   ├── schemas/
│   │   │   └── refuel.py            # Pydantic схемы (Request/Response)
│   │   ├── routers/
│   │   │   ├── refuel.py            # CRUD эндпоинты заправок
│   │   │   └── stats.py             # Эндпоинты статистики
│   │   └── services/
│   │       └── calculator.py        # Логика расчёта расхода, статистики
│   ├── fuel_tracker.db
│   ├── requirements.txt
│   └── README.md
│
└── fueltracker-android/
    └── app/src/main/
        ├── AndroidManifest.xml
        ├── java/com/example/fueltracker_android/
        │   ├── data/
        │   │   ├── ApiService.kt         # Retrofit интерфейс
        │   │   ├── FuelRepository.kt     # Репозиторий, маппинг DTO→Domain
        │   │   └── dto/
        │   │       ├── RefuelDto.kt
        │   │       └── StatsDto.kt
        │   │       
        │   ├── domain/
        │   │   └── model/
        │   │       ├── Refuel.kt             # Domain модель
        │   │       └── Stats.kt              # Domain модель статистики
        │   └── ui/
        │       ├── MainActivity.kt           # Host Activity, NavController
        │       ├── UiState.kt                # sealed class Loading/Success/Error
        │       ├── screens/
        │       │   ├── splash/
        │       │   │   └── SplashFragment.kt
        │       │   ├── home/
        │       │   │   ├── HomeFragment.kt
        │       │   │   └── HomeViewModel.kt
        │       │   ├── add/
        │       │   │   ├── AddRefuelFragment.kt
        │       │   │   └── AddRefuelViewModel.kt
        │       │   ├── history/
        │       │   │   ├── HistoryFragment.kt
        │       │   │   ├── HistoryViewModel.kt
        │       │   │   └── RefuelAdapter.kt
        │       │   └── stats/
        │       │       ├── StatsFragment.kt
        │       │       └── StatsViewModel.kt
        │       └── components/
        │           ├── FuelGaugeView.kt      # Custom View индикатора бака
        │           ├── StatCardView.kt       # Карточка статистики
        │           └── AnimatedCounter.kt    # Анимированный счётчик цифр
        └── res/
            ├── layout/
            ├── drawable/
            ├── font/
            ├── raw/                          # Lottie JSON анимации
            ├── anim/                         # XML анимации
            ├── navigation/
            │   └── nav_graph.xml
            └── values/
                ├── colors.xml
                ├── strings.xml
                ├── themes.xml
                └── dimens.xml
```

---

## 4. Экраны приложения

### 4.1 Splash Screen

**Назначение:** Первый экран при запуске, брендинг, инициализация.

**Визуальный состав:**
- Полностью тёмный фон `#0A0D14`
- По центру: Lottie-анимация заправочного пистолета (`res/raw/splash_fuel_pulse.json`)
- Под иконкой: название `FUELTRACKER` шрифтом Orbitron
- Снизу: тонкая янтарная линия-прогресс, заполняющаяся за 1.5 секунды

**Поведение:**
- Показывается 2 секунды
- После завершения: автоматический переход на Home Screen

---

### 4.2 Home Screen (Главный экран)

**Назначение:** Дашборд — первое что видит пользователь после Splash.

**Визуальный состав (сверху вниз):**

```
┌─────────────────────────────────────┐
│  ≡  FuelTracker          🔔         │  ← TopAppBar
├─────────────────────────────────────┤
│      [  ИНДИКАТОР БАКА  ]           │  ← Custom FuelGaugeView
│     Анимированная дуга Canvas       │
│      Последний расход: 8.4 л/100    │
├─────────────────────────────────────┤
│  📅 Последняя заправка              │
│  ┌───────────────────────────────┐  │
│  │  14 мая · 45.5 л · 2 645 ₽  │  │
│  │  Пробег: 87 450 км           │  │
│  └───────────────────────────────┘  │
├─────────────────────────────────────┤
│  📊 Этот месяц                      │
│  ┌──────────┐  ┌──────────┐        │
│  │ 8 623 ₽  │  │ 8.4 л   │        │
│  │ Потрачено│  │ Расход   │        │
│  └──────────┘  └──────────┘        │
├─────────────────────────────────────┤
│   ╔═══════════════════════════╗     │
│   ║   ⛽  Я заправился!       ║     │
│   ╚═══════════════════════════╝     │
└─────────────────────────────────────┘
│  🏠 Главная  📋 История  📊 Статистика │
```

---

### 4.3 Add Refuel Screen (Добавление заправки)

Bottom Sheet поверх Home. Поля: литры, цена за литр, пробег, заметка (опционально).
Живой расчёт суммы при вводе. Кнопка "Сохранить" неактивна пока поля пусты.

---

### 4.4 History Screen (История)

Список всех заправок с фильтром по месяцу и сортировкой. Swipe влево — удаление.
Каждая карточка имеет цветную полоску: зелёная/янтарная/красная в зависимости от расхода.

---

### 4.5 Stats Screen (Статистика)

Графики MPAndroidChart: LineChart расхода, BarChart цен, BarChart трат по месяцам.
4 summary-карточки: средний расход, лучший, всего потрачено, всего литров.

---

## 5. Навигация и переходы между экранами

### Граф навигации

```
SplashFragment → HomeFragment ↔ AddRefuelFragment (Bottom Sheet)
HomeFragment ←→ HistoryFragment ←→ StatsFragment (через BottomNav)
```

### Анимации переходов

| Переход | Анимация | Длительность |
|---|---|---|
| Splash → Home | Fade | 400ms |
| Home → AddRefuel | Slide up | 350ms |
| AddRefuel → Home | Slide down | 300ms |
| BottomNav переключение | Crossfade | 250ms |

---

## 6. Анимации

### 6.1 FuelGaugeView
Кастомный `View` на `Canvas`. Дуга 240°, янтарный градиент, анимация 0→значение за 1200ms.

### 6.2 AnimatedCounter
При изменении суммы цифры плавно прокручиваются через `ValueAnimator` за 300ms.

### 6.3 Карточки History
Появление при скролле: `alpha 0→1` + `translationY 40dp→0`, stagger 60ms между карточками.

### 6.4 Swipe-to-Delete
`ItemTouchHelper` — красный фон с иконкой корзины, collapse по высоте за 300ms.

### 6.5 Графики
```kotlin
lineChart.animateX(1000, Easing.EaseInOutCubic)
barChart.animateY(800, Easing.EaseInOutQuart)
```

### 6.6 Breathe Effect (кнопка)
```kotlin
ObjectAnimator.ofFloat(btnRefuel, "alpha", 1f, 0.75f).apply {
    duration = 1500
    repeatMode = ValueAnimator.REVERSE
    repeatCount = ValueAnimator.INFINITE
    start()
}
```

### 6.7 Lottie анимации

| Файл | Применение |
|---|---|
| `splash_fuel_pulse.json` | Иконка на сплэш-экране |
| `empty_state.json` | Пустой список в истории |

---

## 7. Серверная часть (fueltracker-backend)

### Технологии

| Компонент | Технология |
|---|---|
| Framework | FastAPI |
| ORM | SQLAlchemy |
| База данных | SQLite |
| Валидация | Pydantic v2 |
| Сервер | Uvicorn |

### Модель базы данных

```sql
CREATE TABLE refuels (
    id          INTEGER  PRIMARY KEY AUTOINCREMENT,
    liters      REAL     NOT NULL,
    price       REAL     NOT NULL,
    total_cost  REAL     NOT NULL,
    odometer    INTEGER  NOT NULL,
    consumption REAL,
    note        TEXT,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### API Эндпоинты

**Base URL:** `http://10.0.2.2:8000/api/v1` (эмулятор) / `http://<IP>:8000/api/v1` (устройство)

| Метод | URL | Описание |
|---|---|---|
| POST | `/refuels` | Добавить заправку |
| GET | `/refuels` | Список (limit, offset, month) |
| DELETE | `/refuels/{id}` | Удалить заправку |
| GET | `/stats/summary` | Общая сводка |
| GET | `/stats/monthly` | По месяцам (months=6) |
| GET | `/stats/consumption-trend` | Тренд расхода (limit=20) |

### Запуск сервера

```bash
cd fueltracker-backend
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

---

## 8. Android-приложение (fueltracker-android)

### Технологии

| Компонент | Библиотека | Версия |
|---|---|---|
| Язык | Kotlin | 2.1.0 |
| Архитектура | MVVM + Repository | — |
| Сеть | Retrofit2 + OkHttp | 2.11.0 |
| JSON | Gson | — |
| Графики | MPAndroidChart | 3.1.0 |
| Анимации | Lottie-Android | 6.4.0 |
| Coroutines | Kotlin Coroutines | 1.8.0 |
| Lifecycle | ViewModel + StateFlow | 2.7.0 |
| Navigation | Navigation Component | 2.7.7 |
| UI | Material Design 3 | 1.12.0 |

### Архитектура (MVVM)

```
Fragment (UI)
    │ observes StateFlow
    ▼
ViewModel
    │ calls suspend functions
    ▼
FuelRepository
    │ calls
    ▼
ApiService (Retrofit)
    │ HTTP
    ▼
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

### Создание ViewModel (без DI)

ViewModel-ы создаются через `ViewModelProvider.Factory`:

```kotlin
class HomeViewModel(private val repository: FuelRepository) : ViewModel() {
    // ...

    companion object {
        fun factory(repository: FuelRepository) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(repository) as T
            }
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
// ApiService.kt
companion object {
    fun create(): ApiService {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}
```

### Gradle зависимости (app/build.gradle.kts)

```kotlin
dependencies {
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Lottie
    implementation("com.airbnb.android:lottie:6.4.0")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Material 3
    implementation("com.google.android.material:material:1.12.0")
}
```

---

## 9. Бизнес-логика и расчёты

### Расчёт расхода (сервер)

```python
consumption = round((liters / (new_odometer - prev_odometer)) * 100, 2)
```

Для первой записи — `consumption = NULL`.

### Цветовая индикация расхода (клиент)

```kotlin
fun getConsumptionColor(consumption: Float, avgConsumption: Float): Int {
    return when {
        consumption <= avgConsumption * 0.95 -> Color.parseColor("#00E676")  // Зелёный
        consumption <= avgConsumption * 1.10 -> Color.parseColor("#F5A623")  // Янтарный
        else                                 -> Color.parseColor("#FF6B6B")  // Красный
    }
}
```

---

## 10. Финальный результат

Приложение выглядит как **тёмная приборная панель спортивного автомобиля**:

- Главный экран с живым янтарным gauge
- Плавные переходы и анимации на каждое действие
- Графики, которые рисуются при первом просмотре
- Карточки с анимацией появления при скролле
- Интерактивная форма с живым расчётом суммы
- Swipe-to-delete с collapse анимацией
- Числа всегда анимируются плавно