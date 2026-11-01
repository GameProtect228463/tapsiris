# Архитектурная схема — «Тапсырыс» (Clean Architecture + MVI/MVVM)
> Обновлено: 2026-11-01 | Версия приложения: v0.0.1-concept

---

## 1. Обзор: слои и поток данных (Unidirectional Data Flow)

```mermaid
flowchart TD
  subgraph APP ["App Entry Point"]
    MA["MainActivity.kt\nAppCompatDelegate · LanguageManager\nNavigation: AUTH → SCHEDULE → SETTINGS"]
  end

  subgraph Presentation ["Presentation Layer (UI — Jetpack Compose + MVI)"]
    direction TB

    subgraph Auth ["auth/"]
      AS["AuthScreen.kt\nВход · Регистрация · Гостевой вход\nLanguageSelectDialog"]
    end

    subgraph Schedule ["schedule/"]
      SS["ScheduleScreen.kt\nAnimatedContent: Loading / Error / Success"]
      VM["ScheduleViewModel.kt\nStateFlow · appContext для строк"]
      UIS["ScheduleUiState\nloadState: ScheduleLoadState\nisDemoMode · is1CConnected"]
      EVT["ScheduleUiEvent\nSelectGroup · SelectWeekType · Refresh\nUpdateServerConfig · ToggleSearch …"]
      LS["ScheduleLoadState\n● Loading(message)\n● Error(title, hint, showSettingsButton)\n● Success"]
    end

    subgraph Settings ["settings/"]
      SET["SettingsScreen.kt\nПрофиль · Язык · Уведомления · 1С · О приложении\nEasterEgg (15 кликов)"]
      LSD["LanguageSelectDialog.kt\nRadioButton · currentCode"]
    end

    subgraph Components ["components/"]
      LC["LessonCard.kt\ni18n типов: Лекция/Дәріс/Lecture\nbadge «Идёт сейчас» · блок замен"]
      DS["DaySelector.kt\ni18n «Сегодня/Бүгін/Today»\nиндикаторы замен · анимация"]
      GS["GroupSelectSheet.kt\nModalBottomSheet · поиск\ni18n формата курса"]
      BD["BellScheduleDialog.kt\ni18n заголовка и перерывов"]
      SD["ServerSettingsDialog.kt\nping · URL · логин · пароль · демо-режим"]
    end
  end

  subgraph Domain ["Domain Layer (Бизнес-логика — без зависимостей Android)"]
    MDL["ScheduleModels.kt\nLesson · DaySchedule · StudentGroup\nBellTime · WeekType · LessonType"]
    RI["ScheduleRepository.kt\ninterface: getGroups / getSchedule\ngetBells / pingServer"]
  end

  subgraph Data ["Data Layer (Источники данных)"]
    RIMPL["ScheduleRepositoryImpl.kt\nSingle Source of Truth\nонлайн → 1С · офлайн → Mock"]
    API["OneCApiClient.kt\nHTTP GET · Basic Auth (Base64)\nJSON → Domain Models\nDispatchers.IO · timeout 7s"]
    CFG["ServerConfig.kt\nserverUrl · username1C\npassword1C · isDemoMode"]
    SM["OneCSettingsManager.kt\nSharedPreferences: one_c_config_prefs\ngetConfig / saveConfig"]
    UM["UserManager.kt\nSharedPreferences: user_auth_prefs\nUserProfile · isGuest · logout"]
    MK["MockScheduleData.kt\nОфлайн-кэш · Compose Preview\nДемо-данные при isDemoMode=true"]
  end

  subgraph i18n ["Локализация (res/)"]
    STR["strings.xml (ru)\nstrings.xml (kk — Қазақша)\nstrings.xml (en — English)"]
    LC2["locale_config.xml\nru · kk · en"]
    LM["LanguageManager.kt\nAppCompatDelegate.setApplicationLocales\nSharedPrefs: settings_prefs"]
  end

  subgraph Server1C ["Сервер 1С:Предприятие (HTTP-сервис)"]
    HS["HTTP-сервис /hs/schedule\nGET /ping\nGET /groups\nGET /schedule?groupId=&week=odd|even\nGET /bells"]
  end

  %% ── Поток UI событий ──────────────────────────────────────
  AS & SS & SET -->|"генерируют события"| EVT
  EVT -->|"onEvent()"| VM
  VM -->|"StateFlow<ScheduleUiState>"| UIS
  UIS -->|"collectAsState()"| SS
  UIS --> LS

  %% ── Поток данных ──────────────────────────────────────────
  VM -->|"вызывает"| RI
  RI -. "реализует" .-> RIMPL
  RIMPL -->|"онлайн"| API
  API -->|"HTTP GET + Basic Auth"| HS
  HS -->|"JSON"| API
  RIMPL -->|"офлайн / demo"| MK
  RIMPL -->|"настройки"| SM
  SM --- CFG

  %% ── Навигация ─────────────────────────────────────────────
  MA -->|"AUTH / SCHEDULE / SETTINGS"| AS & SS & SET
  MA -->|"ViewModel Factory + appContext"| VM
  MA -->|"UserManager"| UM

  %% ── Локализация ───────────────────────────────────────────
  LM -->|"applySavedLanguage (до super.onCreate)"| MA
  SET -->|"setLanguage(context, code)"| LM
  AS -->|"LanguageSelectDialog"| LSD
  LSD -->|"setLanguage"| LM
  STR & LC2 --- LM

  %% ── Компоненты ────────────────────────────────────────────
  SS --- LC & DS & GS & BD & SD
  SET --- LSD
```

---

## 2. Навигация между экранами

```mermaid
stateDiagram-v2
    [*] --> AUTH : первый запуск\n(UserManager.isLoggedIn = false)
    [*] --> SCHEDULE : повторный запуск\n(isLoggedIn = true)

    AUTH --> SCHEDULE : onAuthSuccess / onGuestLogin
    SCHEDULE --> SETTINGS : кнопка шестерёнки
    SETTINGS --> SCHEDULE : кнопка «Назад» / BackHandler
    SETTINGS --> AUTH : onLogout → UserManager.logout()
```

---

## 3. Состояния загрузки главного экрана (`ScheduleLoadState`)

```mermaid
stateDiagram-v2
    [*] --> Loading : init ViewModel

    Loading --> Loading : loadGroups → loadBells → loadSchedule
    Loading --> Error_NoServer : serverUrl.isBlank() && !isDemoMode
    Loading --> Error_Connection : сетевой сбой / HTTP ≠ 2xx
    Loading --> Success : данные получены\n(онлайн или демо)

    Error_NoServer --> Loading : Refresh / UpdateServerConfig
    Error_Connection --> Loading : Refresh

    Success --> Loading : Refresh / SelectGroup / SelectWeekType
    Success --> Success : SelectDay / SearchQueryChanged / ToggleSearch

    Error_NoServer : Error\ntitle: error_no_server\nshowSettingsButton = true
    Error_Connection : Error\ntitle: error_connection\nshowSettingsButton = false
```

---

## 4. Механизм локализации (смена языка)

```mermaid
sequenceDiagram
    participant User
    participant Screen as AuthScreen / SettingsScreen
    participant LSD as LanguageSelectDialog
    participant LM as LanguageManager
    participant ACD as AppCompatDelegate
    participant SP as SharedPreferences

    User->>Screen: нажимает «Язык»
    Screen->>LSD: showLanguageDialog = true\ncurrentCode = getCurrentLanguageCode()
    LSD-->>User: показывает RadioButton\nс текущим языком
    User->>LSD: выбирает "kk" / "ru" / "en"
    LSD->>LM: setLanguage(context, code)
    LM->>SP: putString(KEY_LANGUAGE, code)
    LM->>ACD: setApplicationLocales(LocaleListCompat)
    ACD-->>Screen: Activity.recreate() автоматически
    Note over Screen: Весь UI перестраивается\nс новым языком
```

---

## 5. Назначение пакетов и модулей

### 📁 `domain/` — Ядро (не зависит ни от чего Android)

| Файл | Содержимое |
|------|-----------|
| `model/ScheduleModels.kt` | `Lesson`, `DaySchedule`, `StudentGroup`, `BellTime`, `WeekType`, `LessonType` |
| `repository/ScheduleRepository.kt` | Контракт: `getGroups`, `getSchedule`, `getBells`, `pingServer` |

---

### 📁 `data/` — Источники данных

| Файл | Назначение |
|------|-----------|
| `remote/OneCApiClient.kt` | HTTP GET к 1С, `Authorization: Basic`, парсинг JSON → Domain. Timeout 7 с, `Dispatchers.IO` |
| `remote/ServerConfig.kt` | `serverUrl`, `username1C`, `password1C`, `isDemoMode` |
| `local/OneCSettingsManager.kt` | Хранение `ServerConfig` в `SharedPreferences (one_c_config_prefs)` |
| `local/UserManager.kt` | Профиль студента (`UserProfile`), флаг `isGuest`, logout |
| `mock/MockScheduleData.kt` | Офлайн-кэш и данные для Compose Preview. Используется если `isDemoMode = true` или сеть недоступна |
| `repository/ScheduleRepositoryImpl.kt` | Single Source of Truth: сначала 1С → при неудаче или `isDemoMode` → Mock |

---

### 📁 `presentation/` — UI-слой (Compose + MVI)

#### `schedule/`

| Файл | Назначение |
|------|-----------|
| `ScheduleUiState.kt` | Иммутабельное состояние: `loadState: ScheduleLoadState`, `isDemoMode`, `is1CConnected`, `groups`, `weekSchedule`, `displayLessons` |
| `ScheduleLoadState` (sealed interface) | `Loading(message)` · `Error(title, hint, showSettingsButton)` · `Success` |
| `ScheduleUiEvent` (sealed interface) | Все действия пользователя: `SelectGroup`, `Refresh`, `UpdateServerConfig` … |
| `ScheduleViewModel.kt` | Принимает `appContext: Context` для строк ресурсов. Проверяет `serverUrl` перед запросом. Управляет переходами `loadState` |
| `ScheduleScreen.kt` | `AnimatedContent` по `loadState`: спиннер загрузки → экран ошибки с Retry / Open Settings → расписание. Демо-баннер |

#### `auth/`

| Файл | Назначение |
|------|-----------|
| `AuthScreen.kt` | Вход / Регистрация / Гость. Валидация через `stringResource`. Кнопка смены языка → `LanguageSelectDialog` |

#### `settings/`

| Файл | Назначение |
|------|-----------|
| `SettingsScreen.kt` | Профиль · Язык (текущий в subtitle) · Уведомления · 1С · О приложении. Пасхалка: 15 кликов на версию |
| `LanguageSelectDialog.kt` | `RadioButton` со значком текущего языка. Параметр `currentCode` |
| `EasterEggVideoDialog.kt` | Видео-пасхалка |

#### `components/`

| Файл | Что переведено / изменено |
|------|--------------------------|
| `LessonCard.kt` | Типы занятий (`lesson_type_*`), «Идёт сейчас» (`lesson_current`), «Замена» (`lesson_replacement_label`) |
| `DaySelector.kt` | «Сегодня» (`today`) |
| `GroupSelectSheet.kt` | Заголовок, поиск, формат `course_label` |
| `BellScheduleDialog.kt` | Заголовок, длительность пары, `bell_break` |
| `ServerSettingsDialog.kt` | Диалог параметров и ping-теста 1С |

---

### 📁 `utils/`

| Файл | Назначение |
|------|-----------|
| `LanguageManager.kt` | `setLanguage(context, code)` — сохраняет в `SharedPrefs` + `AppCompatDelegate.setApplicationLocales`. **Без `recreate()`** — платформа делает сама. `getCurrentLanguageCode(context)` — для отображения выбранного языка в диалоге |

---

### 📁 `res/`

| Ресурс | Назначение |
|--------|-----------|
| `values/strings.xml` | 🇷🇺 Русский — базовый язык (70+ строк) |
| `values-kk/strings.xml` | 🇰🇿 Қазақша |
| `values-en/strings.xml` | 🇬🇧 English |
| `xml/locale_config.xml` | Декларирует поддерживаемые локали для Android 13+ (`ru`, `kk`, `en`) |
| `xml/backup_rules.xml` | Правила резервного копирования |
| `raw/musor_drop_green_screen.mp4` | Видео для пасхалки |

---

## 6. Ключевые архитектурные решения

| Решение | Обоснование |
|---------|-------------|
| **Clean Architecture (3 слоя)** | Domain не зависит от Android; легко тестировать бизнес-логику |
| **MVI (Unidirectional Data Flow)** | Один иммутабельный `UiState` → нет рассинхрона между UI-элементами |
| **`ScheduleLoadState` sealed interface** | UI отображает чёткий экран для каждого состояния (загрузка / ошибка / данные) без булевых флагов |
| **`appContext` в ViewModel** | Строки сообщений (`userMessage`, `loadState.message`) берутся из ресурсов — не хардкод, автоматически локализованы |
| **`AppCompatDelegate.setApplicationLocales`** | Смена языка без `recreate()` вручную; работает на API 24+; поддерживает системный выбор языка на API 33+ |
| **`isDemoMode` в `ServerConfig`** | По умолчанию `true` — приложение работает без реального 1С сервера «из коробки» |
| **Fallback Mock → 1С** | `ScheduleRepositoryImpl` пробует сеть, при сбое — Mock; UI видит только `Result<T>` |
