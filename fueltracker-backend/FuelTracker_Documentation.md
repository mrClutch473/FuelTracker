# ⛽ FuelTracker — Полная Проектная Документация

> **Версия:** 1.0  
> **Стек:** Kotlin (Android Studio) · FastAPI · SQLite  
> **Уровень:** Учебный / Портфолио  
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

> **Orbitron** используется только для больших цифровых дисплеев (пробег, сумма) — создаёт ощущение спидометра.  
> **Inter** — для всего остального текста.  
> **JetBrains Mono** — для числовых значений в карточках.

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
│   │   │   ├── __init__.py
│   │   │   └── refuel.py            # SQLAlchemy модель Refuel
│   │   ├── schemas/
│   │   │   ├── __init__.py
│   │   │   └── refuel.py            # Pydantic схемы (Request/Response)
│   │   ├── routers/
│   │   │   ├── __init__.py
│   │   │   ├── refuel.py            # CRUD эндпоинты заправок
│   │   │   └── stats.py             # Эндпоинты статистики
│   │   └── services/
│   │       ├── __init__.py
│   │       └── calculator.py        # Логика расчёта расхода, статистики
│   ├── fuel_tracker.db              # SQLite файл (создаётся автоматически)
│   ├── requirements.txt
│   └── README.md
│
└── fueltracker-android/
    ├── app/
    │   ├── src/main/
    │   │   ├── AndroidManifest.xml
    │   │   ├── java/com/fueltracker/
    │   │   │   ├── FuelTrackerApp.kt        # Application класс, Hilt
    │   │   │   ├── di/
    │   │   │   │   └── NetworkModule.kt      # Hilt модуль, Retrofit
    │   │   │   ├── data/
    │   │   │   │   ├── remote/
    │   │   │   │   │   ├── ApiService.kt     # Retrofit интерфейс
    │   │   │   │   │   └── dto/              # Data Transfer Objects
    │   │   │   │   │       ├── RefuelDto.kt
    │   │   │   │   │       └── StatsDto.kt
    │   │   │   │   └── repository/
    │   │   │   │       └── FuelRepository.kt # Репозиторий, маппинг DTO→Domain
    │   │   │   ├── domain/
    │   │   │   │   └── model/
    │   │   │   │       ├── Refuel.kt         # Domain модель
    │   │   │   │       └── Stats.kt          # Domain модель статистики
    │   │   │   └── ui/
    │   │   │       ├── MainActivity.kt       # Host Activity, NavController
    │   │   │       ├── theme/
    │   │   │       │   ├── Color.kt
    │   │   │       │   ├── Theme.kt
    │   │   │       │   └── Type.kt
    │   │   │       ├── screens/
    │   │   │       │   ├── splash/
    │   │   │       │   │   └── SplashFragment.kt
    │   │   │       │   ├── home/
    │   │   │       │   │   ├── HomeFragment.kt
    │   │   │       │   │   └── HomeViewModel.kt
    │   │   │       │   ├── add/
    │   │   │       │   │   ├── AddRefuelFragment.kt
    │   │   │       │   │   └── AddRefuelViewModel.kt
    │   │   │       │   ├── history/
    │   │   │       │   │   ├── HistoryFragment.kt
    │   │   │       │   │   ├── HistoryViewModel.kt
    │   │   │       │   │   └── RefuelAdapter.kt
    │   │   │       │   └── stats/
    │   │   │       │       ├── StatsFragment.kt
    │   │   │       │       └── StatsViewModel.kt
    │   │   │       └── components/
    │   │   │           ├── FuelGaugeView.kt  # Custom View бака
    │   │   │           ├── StatCardView.kt   # Карточка статистики
    │   │   │           └── AnimatedCounter.kt# Анимированный счётчик цифр
    │   │   └── res/
    │   │       ├── layout/               # XML layout файлы
    │   │       ├── drawable/             # Векторные иконки, фоны
    │   │       ├── raw/                  # Lottie JSON анимации
    │   │       ├── values/
    │   │       │   ├── colors.xml
    │   │       │   ├── strings.xml
    │   │       │   ├── themes.xml
    │   │       │   └── dimens.xml
    │   │       └── navigation/
    │   │           └── nav_graph.xml     # Navigation Component граф
    │   ├── build.gradle
    │   └── proguard-rules.pro
    ├── build.gradle
    └── settings.gradle
```

---

## 4. Экраны приложения

### 4.1 Splash Screen

**Назначение:** Первый экран при запуске, брендинг, инициализация.

**Визуальный состав:**
- Полностью тёмный фон `#0A0D14`
- По центру: иконка капли топлива (SVG, белая), анимация пульсации
- Под иконкой: название `FUELTRACKER` шрифтом Orbitron
- Снизу: тонкая янтарная линия-прогресс, заполняющаяся за 1.5 секунды

**Поведение:**
- Показывается 2 секунды
- Параллельно: приложение делает первый запрос к серверу (prefetch истории)
- После завершения: автоматический переход на Home Screen

---

### 4.2 Home Screen (Главный экран)

**Назначение:** Дашборд — первое что видит пользователь после Splash.

**Визуальный состав (сверху вниз):**

```
┌─────────────────────────────────────┐
│  ≡  FuelTracker          🔔         │  ← TopAppBar
├─────────────────────────────────────┤
│                                     │
│      [  ИНДИКАТОР БАКА  ]           │  ← Custom FuelGaugeView
│     Анимированная SVG-заливка       │
│      Последний расход: 8.4 л/100    │
│                                     │
├─────────────────────────────────────┤
│  📅 Последняя заправка              │
│  ┌───────────────────────────────┐  │
│  │  14 мая · 45.5 л · 2 645 ₽  │  │  ← Карточка последней заправки
│  │  Пробег: 87 450 км           │  │
│  └───────────────────────────────┘  │
├─────────────────────────────────────┤
│  📊 Этот месяц                      │
│  ┌──────────┐  ┌──────────┐        │
│  │ 8 623 ₽  │  │ 8.4 л   │        │  ← 2 мини-карточки статистики
│  │ Потрачено│  │ Расход   │        │
│  └──────────┘  └──────────┘        │
├─────────────────────────────────────┤
│                                     │
│   ╔═══════════════════════════╗     │
│   ║   ⛽  Я заправился!       ║     │  ← Главная CTA кнопка
│   ╚═══════════════════════════╝     │
│                                     │
└─────────────────────────────────────┘
│  🏠 Главная  📋 История  📊 Статистика │  ← Bottom Navigation
```

**Детали компонентов:**

**FuelGaugeView (Индикатор бака):**
- Круговой gauge диаметром ~200dp
- Дуга от 7 часов до 5 часов по часовой (270°)
- Фон дуги: `#1A1F2E`, толщина 12dp
- Заполненная часть: янтарный градиент `#F5A623 → #FF6B35`
- В центре круга: большая цифра расхода шрифтом Orbitron
- Под цифрой: подпись "л / 100 км" серым цветом
- Анимация заполнения при открытии экрана: 0 → значение за 1200ms, easing = DecelerateInterpolator
- Слабое свечение янтарного цвета вокруг заполненной части (shadow layer)

**Кнопка "Я заправился!":**
- Полная ширина минус padding 24dp с каждой стороны
- Высота 56dp, corner radius 16dp
- Янтарный градиент фон
- Иконка бензоколонки слева
- Жирный белый текст по центру
- Постоянная лёгкая анимация пульсации свечения (breathe effect)
- При нажатии: пружинящий scale 0.95 → 1.05 → 1.0

---

### 4.3 Add Refuel Screen (Экран добавления заправки)

**Назначение:** Ввод данных о новой заправке. Открывается как Bottom Sheet поверх Home Screen.

**Визуальный состав:**

```
┌─────────────────────────────────────┐
│  ────────                           │  ← Drag handle
│  Новая заправка             ✕       │
├─────────────────────────────────────┤
│                                     │
│  ┌──────────────────────────────┐   │
│  │  ⛽  Литры                   │   │
│  │      45.5                    │   │  ← Input поле 1
│  └──────────────────────────────┘   │
│                                     │
│  ┌──────────────────────────────┐   │
│  │  💰  Цена за литр (₽)        │   │
│  │      58.90                   │   │  ← Input поле 2
│  └──────────────────────────────┘   │
│                                     │
│  ┌──────────────────────────────┐   │
│  │  🚗  Пробег (км)             │   │
│  │      87 450                  │   │  ← Input поле 3
│  └──────────────────────────────┘   │
│                                     │
│  ┌──────────────────────────────┐   │
│  │  📝  Заметка (необязательно) │   │
│  │      Трасса М4, ТНК          │   │  ← Input поле 4
│  └──────────────────────────────┘   │
│                                     │
│  ╔══════════════════════════════╗   │
│  ║  ИТОГО: 2 679 ₽              ║   │  ← Итоговая сумма (живая)
│  ╚══════════════════════════════╝   │
│                                     │
│   [Отмена]    [  Сохранить ✓  ]    │
└─────────────────────────────────────┘
```

**Детали:**

- Bottom Sheet с `peekHeight = MATCH_PARENT`, `cornerRadius = 24dp`
- Фон sheet: `#12161F`
- Все поля: тёмный фон `#1A1F2E`, граница `1dp #2A3040`, corner `12dp`
- При фокусе на поле: граница меняется на янтарную `#F5A623` с плавным transition 200ms
- **Живой расчёт суммы:** при каждом изменении литров или цены — сумма мгновенно пересчитывается, цифры прокручиваются как на механическом табло (slot machine animation, 300ms)
- Кнопка "Сохранить" неактивна пока не заполнены обязательные поля (opacity 0.4)
- При успешном сохранении: sheet закрывается, на Home появляется snackbar "✅ Заправка сохранена"

---

### 4.4 History Screen (История заправок)

**Назначение:** Полный список всех заправок в обратном хронологическом порядке.

**Визуальный состав:**

```
┌─────────────────────────────────────┐
│  История заправок                   │  ← TopAppBar
├─────────────────────────────────────┤
│  [Фильтр: Всё время ▼]  [↕ Сорт.]  │  ← Строка фильтров
├─────────────────────────────────────┤
│                                     │
│  MAY 2025                           │  ← Sticky header месяца
│  ┌───────────────────────────────┐  │
│  │ ⛽ 45.5 л    🗓 14 мая        │  │
│  │ 💰 2 645 ₽   📍 87 450 км    │  │  ← Карточка заправки
│  │ 📊 Расход: 8.2 л/100 ·  ТНК  │  │
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │ ⛽ 50.0 л    🗓 3 мая         │  │
│  │ 💰 2 900 ₽   📍 86 800 км    │  │
│  │ 📊 Расход: 8.7 л/100          │  │
│  └───────────────────────────────┘  │
│                                     │
│  APRIL 2025                         │
│  ...                                │
└─────────────────────────────────────┘
│  🏠 Главная  📋 История  📊 Статистика │
```

**Детали карточки:**

- Corner radius 16dp, фон `#12161F`, граница `1dp #1E2435`
- Левая цветная полоска 4dp: зелёная если расход ниже среднего, красная если выше
- Анимация появления: каждая карточка приходит снизу с fade при скролле (RecyclerView item animator)
- **Swipe влево:** появляется красный фон с иконкой корзины, карточка сжимается и исчезает (collapse animation 300ms)
- Tap на карточку: раскрывается детальный вид (expand animation) — показывает заметку и точный расчёт расхода

---

### 4.5 Stats Screen (Статистика)

**Назначение:** Аналитика и визуализация данных за выбранный период.

**Визуальный состав:**

```
┌─────────────────────────────────────┐
│  Статистика                         │
├─────────────────────────────────────┤
│  [1 мес] [3 мес] [6 мес] [Всё]     │  ← Period selector (chips)
├─────────────────────────────────────┤
│                                     │
│  РАСХОД Л/100 КМ                    │
│  ┌───────────────────────────────┐  │
│  │      📈  Линейный график      │  │
│  │  ╱╲    ╱╲  ╱                 │  │
│  │ ╱  ╲╱╱  ╲╱                  │  │
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────┐  ┌───────────┐      │
│  │  8.4      │  │  7.9      │      │
│  │  Средний  │  │  Лучший   │      │
│  │  расход   │  │  месяц    │      │
│  └───────────┘  └───────────┘      │
│                                     │
│  ЦЕНА ЗА ЛИТР                       │
│  ┌───────────────────────────────┐  │
│  │      📊  Барный график        │  │
│  └───────────────────────────────┘  │
│                                     │
│  ИТОГО ПОТРАЧЕНО                    │
│  ┌───────────────────────────────┐  │
│  │  Янв  Фев  Мар  Апр  Май     │  │
│  │  ▓▓   ▓▓▓  ▓▓   ▓▓▓▓  ▓▓▓   │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
│  🏠 Главная  📋 История  📊 Статистика │
```

**Детали графиков:**

- **График расхода:** LineChart (MPAndroidChart), линия Electric Blue `#4FC3F7`, толщина 2dp, заливка под линией прозрачным градиентом blue→transparent, точки данных в виде кружков 6dp
- **График цен:** BarChart, столбики янтарного цвета, при tap на столбик — показывается tooltip с датой и ценой
- **График трат:** BarChart горизонтальный, по месяцам
- Все графики анимированы при первом появлении: рисуются слева направо за 1000ms

**Summary карточки:**

- 4 карточки в сетке 2×2
- Каждая: иконка + большая цифра (Orbitron) + подпись (Inter)
- Данные: Средний расход / Лучший расход / Всего потрачено / Всего литров

---

## 5. Навигация и переходы между экранами

### Граф навигации

```
SplashFragment
      │
      │ (auto, 2s)
      ▼
HomeFragment ◄────────────────────────┐
      │                               │
      │ [FAB "Заправился"]            │ [Back / ✕]
      ▼                               │
AddRefuelBottomSheet ─────────────────┘
      │
      │ [Сохранить]
      ▼
HomeFragment (обновлённый)

HomeFragment ──── BottomNav ──── HistoryFragment
                                        │
                                        │ [Tap на карточку]
                                        ▼
                                 DetailExpanded (inline)

HomeFragment ──── BottomNav ──── StatsFragment
```

### Типы переходов

| Переход | Тип анимации | Длительность |
|---|---|---|
| Splash → Home | Fade + Scale-up (Home появляется из центра) | 400ms |
| Home → AddRefuel (Bottom Sheet) | Slide up снизу | 350ms |
| AddRefuel → Home (закрытие) | Slide down | 300ms |
| Home → History (BottomNav) | Crossfade | 250ms |
| Home → Stats (BottomNav) | Crossfade | 250ms |
| History → Stats (BottomNav) | Crossfade | 250ms |
| Карточка History: collapse | Shrink + fade | 300ms |
| Карточка History: expand | Grow + fade | 300ms |

### Bottom Navigation поведение

- Иконка активной вкладки подсвечивается янтарным `#F5A623`
- Неактивные иконки: `#3D4455`
- При переключении: иконка активной вкладки делает лёгкий scale 1.0 → 1.15 → 1.0 (150ms)
- Фон NavBar: `#0F1219`, верхняя граница `1dp #1E2435`

---

## 6. Анимации

### 6.1 FuelGaugeView — Индикатор бака

**Реализация:** Кастомный `View` на `Canvas`

```kotlin
// Псевдокод логики
class FuelGaugeView : View {
    var fillPercent: Float = 0f  // 0.0 .. 1.0

    fun animateTo(value: Float) {
        ValueAnimator.ofFloat(0f, value).apply {
            duration = 1200
            interpolator = DecelerateInterpolator()
            addUpdateListener { fillPercent = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        // 1. Рисуем фоновую дугу (серая)
        // 2. Рисуем заполненную дугу (янтарный градиент) до fillPercent
        // 3. Рисуем свечение через Paint.setShadowLayer()
        // 4. Рисуем текст по центру (расход)
    }
}
```

**Параметры:**
- Угол начала: 150° (левый нижний)
- Угол конца: 390° (правый нижний)
- Общий угол дуги: 240°
- Shadow layer: radius=20dp, color=#F5A62388

---

### 6.2 AnimatedCounter — Счётчик суммы

**Реализация:** `TextSwitcher` + `ValueAnimator`

При изменении суммы (при вводе литров/цены):
1. Текущее значение анимируется к новому за 300ms
2. Цифры "прокручиваются" вверх (как механическое табло)
3. Interpolator: `AccelerateDecelerateInterpolator`

Реализуется через кастомный `AnimatedTextView`, который при каждом изменении значения запускает `ObjectAnimator` на `translationY` старого значения (уходит вверх) и нового (приходит снизу).

---

### 6.3 Карточки History — Появление при скролле

**Реализация:** `RecyclerView.ItemAnimator` + `RecyclerView.OnScrollListener`

- Каждая карточка при появлении в viewport: `alpha 0 → 1` + `translationY 40dp → 0`
- Длительность: 350ms, задержка между карточками: 60ms × позиция (stagger)
- Интерполятор: `DecelerateInterpolator`

---

### 6.4 Swipe-to-Delete карточки

**Реализация:** `ItemTouchHelper.SimpleCallback`

1. Пользователь начинает свайп влево
2. Под карточкой появляется красный фон `#FF6B6B` с иконкой корзины
3. При отпускании после порога 50% ширины: карточка "летит" вправо за экран (200ms)
4. Карточка схлопывается по высоте с `ValueAnimator` (itemView.layoutParams.height → 0, 300ms)
5. Остальные карточки "съезжаются" (обрабатывается RecyclerView автоматически)

---

### 6.5 График — Анимация отрисовки

**Реализация:** MPAndroidChart встроенная анимация

```kotlin
lineChart.animateX(1000, Easing.EaseInOutCubic)
barChart.animateY(800, Easing.EaseInOutQuart)
```

Линия графика рисуется слева направо, будто карандаш проводит черту.

---

### 6.6 Кнопка "Заправился" — Breathe Effect

**Реализация:** `ObjectAnimator` с бесконечным повтором

```kotlin
ObjectAnimator.ofFloat(btnRefuel, "alpha", 1f, 0.75f).apply {
    duration = 1500
    repeatMode = ValueAnimator.REVERSE
    repeatCount = ValueAnimator.INFINITE
    interpolator = AccelerateDecelerateInterpolator()
    start()
}
// Параллельно: пульсация тени (через кастомный drawable)
```

---

### 6.7 Lottie анимации

Используются готовые Lottie JSON с [lottiefiles.com](https://lottiefiles.com) (бесплатные, MIT):

| Сцена | Lottie файл | Применение |
|---|---|---|
| Splash иконка | fuel-drop-pulse.json | Пульсирующая капля топлива |
| Успех сохранения | success-checkmark.json | После сохранения заправки |
| Пустой список | empty-state.json | Когда нет заправок в истории |
| Загрузка | loading-dots.json | Пока данные загружаются с сервера |

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
    id          INTEGER     PRIMARY KEY AUTOINCREMENT,
    liters      REAL        NOT NULL,          -- Количество литров
    price       REAL        NOT NULL,          -- Цена за 1 литр
    total_cost  REAL        NOT NULL,          -- Итоговая сумма (liters * price)
    odometer    INTEGER     NOT NULL,          -- Показание одометра (км)
    consumption REAL,                          -- Расход л/100км (NULL для первой записи)
    note        TEXT,                          -- Заметка (опционально)
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP
);
```

**Расход вычисляется сервером** при каждом добавлении новой заправки:

```python
consumption = (liters / (new_odometer - prev_odometer)) * 100
```

Для первой записи — `consumption = NULL`.

---

### API Эндпоинты

**Base URL:** `http://localhost:8000/api/v1`

---

#### `POST /refuels` — Добавить заправку

**Request Body:**
```json
{
    "liters": 45.5,
    "price": 58.90,
    "odometer": 87450,
    "note": "Трасса М4, ТНК"
}
```

**Response 201:**
```json
{
    "id": 12,
    "liters": 45.5,
    "price": 58.90,
    "total_cost": 2679.95,
    "odometer": 87450,
    "consumption": 8.24,
    "note": "Трасса М4, ТНК",
    "created_at": "2025-05-14T15:30:00"
}
```

**Логика:**
1. Валидация: `liters > 0`, `price > 0`, `odometer > 0`
2. Получить последнюю запись из БД
3. Проверить `new_odometer > prev_odometer` (иначе 422 ошибка)
4. Вычислить `total_cost = liters * price`
5. Вычислить `consumption` (если есть предыдущая запись)
6. Сохранить в БД, вернуть созданный объект

---

#### `GET /refuels` — Список заправок

**Query params:**
- `limit` (default: 50) — кол-во записей
- `offset` (default: 0) — смещение для пагинации
- `month` (optional) — фильтр по месяцу, формат `YYYY-MM`

**Response 200:**
```json
{
    "items": [
        {
            "id": 12,
            "liters": 45.5,
            "price": 58.90,
            "total_cost": 2679.95,
            "odometer": 87450,
            "consumption": 8.24,
            "note": "Трасса М4, ТНК",
            "created_at": "2025-05-14T15:30:00"
        }
    ],
    "total": 1,
    "limit": 50,
    "offset": 0
}
```

---

#### `DELETE /refuels/{id}` — Удалить заправку

**Response 200:**
```json
{
    "message": "Refuel deleted successfully",
    "id": 12
}
```

**Логика:** При удалении — пересчитать `consumption` для следующей по времени записи (она теряет "предыдущий" пробег).

---

#### `GET /stats/summary` — Общая сводка

**Response 200:**
```json
{
    "total_spent": 45230.50,
    "total_liters": 780.5,
    "avg_consumption": 8.35,
    "best_consumption": 7.20,
    "worst_consumption": 10.10,
    "avg_price_per_liter": 57.95,
    "refuels_count": 17,
    "total_km": 9340
}
```

---

#### `GET /stats/monthly` — Статистика по месяцам

**Query params:**
- `months` (default: 6) — за сколько месяцев вернуть данные

**Response 200:**
```json
{
    "data": [
        {
            "month": "2025-05",
            "total_spent": 8623.0,
            "total_liters": 150.0,
            "avg_consumption": 8.4,
            "avg_price": 57.49,
            "refuels_count": 3
        }
    ]
}
```

---

#### `GET /stats/consumption-trend` — Тренд расхода

**Query params:**
- `limit` (default: 20) — последние N заправок для графика

**Response 200:**
```json
{
    "points": [
        {
            "date": "2025-05-14",
            "consumption": 8.24,
            "odometer": 87450
        }
    ]
}
```

---

### Коды ошибок

| Код | Ситуация |
|---|---|
| 400 | Пробег новой заправки меньше предыдущего |
| 404 | Запись не найдена (при DELETE) |
| 422 | Ошибка валидации Pydantic (отрицательные значения и т.д.) |
| 500 | Внутренняя ошибка сервера |

**Формат ошибки:**
```json
{
    "detail": "New odometer value must be greater than previous (87000)"
}
```

---

### CORS настройка

```python
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)
```

### Запуск сервера

```bash
cd fueltracker-backend
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Swagger UI доступен по адресу: `http://localhost:8000/docs`

### requirements.txt

```
fastapi==0.111.0
uvicorn==0.29.0
sqlalchemy==2.0.30
pydantic==2.7.1
python-dotenv==1.0.1
```

---

## 8. Android-приложение (fueltracker-android)

### Технологии

| Компонент | Библиотека | Версия |
|---|---|---|
| Язык | Kotlin | 1.9+ |
| Архитектура | MVVM + Repository | — |
| DI | Hilt | 2.51 |
| Сеть | Retrofit2 + OkHttp | 2.11.0 |
| JSON | Gson | 2.10.1 |
| Графики | MPAndroidChart | 3.1.0 |
| Анимации Lottie | Lottie-Android | 6.4.0 |
| Coroutines | Kotlin Coroutines | 1.8.0 |
| Lifecycle | ViewModel + StateFlow | 2.7.0 |
| Navigation | Navigation Component | 2.7.7 |
| UI | Material Design 3 | 1.12.0 |

### Архитектура (MVVM)

```
UI Layer (Fragment/Activity)
        │
        │ observes StateFlow
        ▼
ViewModel
        │
        │ calls suspend functions
        ▼
Repository
        │
        │ calls
        ▼
ApiService (Retrofit)
        │
        │ HTTP
        ▼
FastAPI Server
```

**StateFlow** используется для передачи состояний UI:

```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

### Конфигурация сети

```kotlin
// NetworkModule.kt (Hilt)
@Provides
fun provideRetrofit(): Retrofit = Retrofit.Builder()
    .baseUrl("http://10.0.2.2:8000/api/v1/")  // 10.0.2.2 = localhost для эмулятора
    .addConverterFactory(GsonConverterFactory.create())
    .client(OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build())
    .build()
```

> **Важно:** На реальном устройстве заменить `10.0.2.2` на IP компьютера в локальной сети.

### Gradle зависимости (app/build.gradle)

```kotlin
dependencies {
    // Hilt
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-compiler:2.51")

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

    // Material 3
    implementation("com.google.android.material:material:1.12.0")
}
```

---

## 9. Бизнес-логика и расчёты

### Расчёт расхода

```python
# fueltracker-backend/app/services/calculator.py

def calculate_consumption(liters: float, prev_odometer: int, curr_odometer: int) -> float:
    """Расход в литрах на 100 км"""
    distance = curr_odometer - prev_odometer
    if distance <= 0:
        raise ValueError("Distance must be positive")
    return round((liters / distance) * 100, 2)

def calculate_total_cost(liters: float, price_per_liter: float) -> float:
    return round(liters * price_per_liter, 2)
```

### Цветовая индикация расхода

Цвет полоски на карточке истории определяется относительно среднего расхода пользователя:

```kotlin
fun getConsumptionColor(consumption: Float, avgConsumption: Float): Int {
    return when {
        consumption <= avgConsumption * 0.95 -> Color.parseColor("#00E676")  // Зелёный (экономно)
        consumption <= avgConsumption * 1.10 -> Color.parseColor("#F5A623")  // Янтарный (норма)
        else                               -> Color.parseColor("#FF6B6B")  // Красный (много)
    }
}
```

### Ценовая индикация

Цвет суммы в карточке — относительно средней цены за литр:

- Дешевле среднего на 5%+ → зелёный `#00E676`
- В пределах ±5% → нейтральный белый
- Дороже среднего на 5%+ → красный `#FF6B6B`

---

## 10. Финальный результат

### Что получается в итоге

Приложение, которое выглядит как **тёмная приборная панель спортивного автомобиля**:

- Главный экран с живым янтарным gauge, напоминающим спидометр
- Плавные переходы и отзывчивые анимации на каждое действие пользователя
- Графики, которые "рисуются" при первом просмотре
- Карточки, которые появляются при скролле как будто подъезжают снизу
- Интерактивная форма с живым расчётом суммы в реальном времени
- Элегантный swipe-to-delete с коллапсом карточки
- Числа на экране никогда не "прыгают" — всегда плавно анимируются
