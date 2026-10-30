# Архитектурная схема приложения «Тапсырыс» (Clean Architecture + MVI/MVVM)

## 1. Схема слоёв и потока данных (Unidirectional Data Flow)

```mermaid
flowchart TD
  subgraph Presentation ["Presentation Layer (UI)"]
    UI["ScheduleScreen.kt - Compose View"]
    Components["Components - LessonCard, DaySelector, GroupSheet, BellDialog, ServerDialog"]
    VM["ScheduleViewModel.kt - StateFlow, Lifecycle-aware"]
    State["ScheduleUiState.kt - Иммутабельное состояние экрана"]
    Events["ScheduleUiEvent - Пользовательские интенты: клики, ввод, выбор"]
  end

  subgraph Domain ["Domain Layer (Бизнес-логика)"]
    Models["ScheduleModels.kt - Lesson, DaySchedule, StudentGroup, BellTime, WeekType"]
    RepoInterface["ScheduleRepository.kt - Интерфейс-контракт"]
  end

  subgraph Data ["Data Layer (Источники данных)"]
    RepoImpl["ScheduleRepositoryImpl.kt - Single Source of Truth"]
    Remote["OneCApiClient.kt - HTTP REST клиент к 1С, Basic Auth, JSON"]
    Local["OneCSettingsManager.kt - SharedPreferences: URL 1С, логин, пароль"]
    Mock["MockScheduleData.kt - Офлайн-кэш и демо-данные"]
  end

  subgraph Server1C ["Сервер 1С:Предприятие"]
    HS["HTTP-сервис 1C (/hs/schedule): /ping, /groups, /schedule, /bells"]
  end

%% Поток данных
  UI -->|1. Генерирует события| Events
  Events -->|2. Передаются в onEvent| VM
  VM -->|3. Вызывает методы| RepoInterface
  RepoInterface -.->|Реализуется| RepoImpl
  RepoImpl -->|4. Запрашивает по сети| Remote
  Remote -->|5. HTTP GET (Basic Auth)| HS
  HS -->|6. JSON ответ| Remote
  RepoImpl -->|Резерв при сбое сети| Mock
  RepoImpl -->|Настройки подключения| Local
  RepoImpl -->|7. Возвращает Result| VM
  VM -->|8. Обновляет _uiState| State
  State -->|9. Рендерится декларативно| UI
  UI --- Components
```

---

## 2. Назначение пакетов и модулей (Где что находится и для чего)

### 📁 `com.TJPK.tapciriz.domain` — Слой бизнес-логики (Domain)
Ядро приложения. Полностью изолировано от UI-библиотек (Compose) и деталей сетевого протокола.

- **`model/ScheduleModels.kt`**:
  - `Lesson` — отдельное занятие (номер пары, время, предмет, преподаватель, аудитория, тип, замена).
  - `DaySchedule` — день расписания (название, дата, список пар, флаг замен).
  - `StudentGroup` — учебная группа колледжа (название, курс, специальность).
  - `BellTime` — звонок (номер пары, время начала и окончания, длительность перемены).
  - `WeekType` — перечисление чётности недели (Числитель / Знаменатель).
- **`repository/ScheduleRepository.kt`**:
  - Чистый контракт, определяющий бизнес-операции: получение групп, расписания, звонков и пинг сервера.

---

### 📁 `com.TJPK.tapciriz.data` — Слой данных (Data)
Отвечает за физическое получение, хранение и кэширование данных.

- **`remote/OneCApiClient.kt`**:
  - Сетевой клиент к 1С:Предприятие.
  - Формирует заголовок `Authorization: Basic Base64(login:password)` для авторизации в 1С.
  - Делает сетевые вызовы в `Dispatchers.IO` и парсит JSON в доменные модели.
- **`remote/ServerConfig.kt`**:
  - Модель настроек соединения (URL, пользователь, пароль, флаг демо/офлайн режима).
- **`local/OneCSettingsManager.kt`**:
  - Сохранение параметров подключения к 1С в `SharedPreferences` устройства.
- **`mock/MockScheduleData.kt`**:
  - Автономный резерв данных для офлайн-режима и Compose Preview.
- **`repository/ScheduleRepositoryImpl.kt`**:
  - Реализация `ScheduleRepository`. Выполняет запрос к 1С, а в случае недоступности сервера переключается на офлайн-кэш без сбоев.

---

### 📁 `com.TJPK.tapciriz.presentation` — Слой интерфейса (Presentation / UI)
Построен по архитектуре Unidirectional Data Flow (MVI/MVVM).

- **`schedule/ScheduleUiState.kt`**:
  - Иммутабельное состояние экрана: `groups`, `selectedGroup`, `weekType`, `selectedDayIndex`, `is1CConnected`, `isRefreshing`, строка поиска и фильтрованный список уроков `displayLessons`.
  - `ScheduleUiEvent` — список всех действий пользователя.
- **`schedule/ScheduleViewModel.kt`**:
  - Хранит состояние в `StateFlow<ScheduleUiState>`.
  - Управляет загрузкой данных и реагирует на действия пользователя через `onEvent(event)`.
- **`schedule/ScheduleScreen.kt`**:
  - Главный экран. Разделен на логический контейнер (`ScheduleScreen`) и чистый рендерер (`ScheduleScreenContent`), что позволяет легко тестировать и предпросматривать экран.
- **`components/`**:
  - `LessonCard.kt` — карточка пары с блоком замен и статусом «Идёт сейчас».
  - `DaySelector.kt` — горизонтальный календарь Пн...Сб с индикацией замен.
  - `GroupSelectSheet.kt` — шторка быстрого выбора группы с поиском.
  - `BellScheduleDialog.kt` — диалог расписания звонков и перемен.
  - `ServerSettingsDialog.kt` — диалог настройки и проверки связи с 1С.
