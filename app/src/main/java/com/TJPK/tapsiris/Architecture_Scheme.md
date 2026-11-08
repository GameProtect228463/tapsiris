# Архитектурная схема — «Тапсырыс» (Clean Architecture + MVI/MVVM)
> Обновлено: 2026-11-07 | Версия приложения: v0.0.1-concept.4

---

## 1. Обзор: слои и поток данных (Unidirectional Data Flow)

```mermaid
flowchart TD
  subgraph APP ["App Entry Point"]
    MA["MainActivity.kt\nAppCompatDelegate · LanguageManager · ThemeManager · NotificationHelper\nNavigation: AUTH → SCHEDULE → SETTINGS\nПередача контекста пользователя в ViewModel"]
  end

  subgraph Presentation ["Presentation Layer (UI — Jetpack Compose + MVI)"]
    direction TB

    subgraph Auth ["auth/"]
      AS["AuthScreen.kt\n3 типа аккаунтов (Студент / Преподаватель / Админ)\nАвторизация через 1С · Подгрузка групп из БД\nДемо-кнопки · Гостевой вход"]
    end

    subgraph Schedule ["schedule/"]
      SS["ScheduleScreen.kt\nAnimatedContent: Loading / Error / Success\nИконка Lock + 'Моя группа' для студентов\nСвободный выбор для Преподавателя / Админа"]
      VM["ScheduleViewModel.kt\nStateFlow · canChangeGroup · currentUserRole\nИзоляция групп для студентов · Уведомления о заменах"]
      UIS["ScheduleUiState\nloadState: ScheduleLoadState\ncanChangeGroup: Boolean\ncurrentUserRole: UserRole\nassignedGroupName: String?"]
      EVT["ScheduleUiEvent\nSelectGroup · SelectWeekType · Refresh\nUpdateUserContext · UpdateServerConfig …"]
      LS["ScheduleLoadState\n● Loading(message)\n● Error(title, hint, showSettingsButton)\n● Success"]
    end

    subgraph Settings ["settings/"]
      SET["SettingsScreen.kt\nПрофиль (роль 1С) · Смена темы · Язык · Уведомления · 1С · О приложении\nEasterEgg (15 кликов)"]
      TSD["ThemeSelectDialog.kt\nРежимы: SYSTEM / LIGHT / DARK\nПалитры: Classic / Mint / Pine / Ocean"]
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
    RI["ScheduleRepository.kt\ninterface: getGroups / getSchedule / getBells\npingServer / authenticate(login, password, role)"]
  end

  subgraph Data ["Data Layer (Источники данных)"]
    RIMPL["ScheduleRepositoryImpl.kt\nSingle Source of Truth\n1С Сервер + SQLite DB + Fallback MockScheduleData"]
    DB["ScheduleDatabaseHelper.kt (local/db)\nSQLiteOpenHelper · Таблица student_groups\ngetAllGroups · saveGroups · findGroupByName"]
    API["OneCApiClient.kt\nHTTP GET / POST /auth\nBasic Auth · Dispatchers.IO · timeout 7s"]
    CFG["ServerConfig.kt\nserverUrl · username1C\npassword1C · isDemoMode"]
    SM["OneCSettingsManager.kt\nSharedPreferences: one_c_config_prefs\ngetConfig / saveConfig"]
    UM["UserManager.kt\nSharedPreferences: user_auth_prefs\nUserProfile · UserRole (STUDENT, TEACHER, ADMIN) · isGuest"]
    NM["NotificationSettingsManager.kt\nSharedPreferences: notification_prefs\nisRemindersEnabled · isReplacementsEnabled · minutesBefore"]
    MK["MockScheduleData.kt\nОфлайн-кэш и аккаунты 1С: Студент, Преподаватель, Админ"]
  end

  subgraph Utils ["Utils & Notifications Layer"]
    TM["ThemeManager.kt\nStateFlow<ThemeMode> · StateFlow<ThemePalette>\nSharedPreferences: theme_prefs\nМгновенная смена без recreate()"]
    NH["NotificationHelper.kt\nКанал channel_schedule_notifications (API 26+)\nPOST_NOTIFICATIONS (API 33+) · Тест · Замены · Пары"]
    LRR["LessonReminderReceiver.kt\nBroadcastReceiver для сигналов AlarmManager"]
    LM["LanguageManager.kt\nAppCompatDelegate.setApplicationLocales\nSharedPrefs: settings_prefs"]
  end

  subgraph UITheme ["ui/theme"]
    TH["Theme.kt\nTapsirizTheme(darkTheme, palette)"]
    COL["Color.kt\nПалитры Classic, Mint, Pine, Ocean\nБез конфликтов сигнатур JVM"]
  end

  subgraph Server1C ["Сервер 1С:Предприятие (HTTP-сервис)"]
    HS["HTTP-сервис /hs/schedule\nGET /ping\nPOST /auth\nGET /groups\nGET /schedule?groupId=&week=odd|even\nGET /bells"]
  end

  %% ── Поток UI событий ──────────────────────────────────────
  AS & SS & SET -->|"генерируют события"| EVT
  EVT -->|"onEvent()"| VM
  VM -->|"StateFlow<ScheduleUiState>"| UIS
  UIS -->|"collectAsState()"| SS
  UIS --> LS

  %% ── Авторизация в 1С и загрузка групп ───────────────────────
  AS -->|"authenticate(login, pass, role)"| RI
  AS -->|"onLoadGroups() подгрузка групп из БД"| RI
  RI -. "реализует" .-> RIMPL
  RIMPL -->|"онлайн POST /auth"| API
  API -->|"JSON"| HS
  RIMPL -->|"демо/офлайн fallback"| MK
  RIMPL --> UM

  %% ── Поток данных расписания и БД ───────────────────────────
  VM -->|"вызывает"| RI
  RIMPL -->|"кэш и выборка групп"| DB
  RIMPL -->|"онлайн GET"| API
  RIMPL -->|"офлайн / demo"| MK
  RIMPL -->|"настройки"| SM
  SM --- CFG

  %% ── Тема и Уведомления ─────────────────────────────────────
  SET -->|"смена темы и палитры"| TSD
  TSD -->|"setThemeMode / setThemePalette"| TM
  TM -->|"collectAsState()"| MA
  MA --> TH
  SET -->|"тест / настройки уведомлений"| NH
  NH --- NM
  VM -->|"при обнаружении замен"| NH
  LRR -->|"по будильнику"| NH

  %% ── Навигация ─────────────────────────────────────────────
  MA -->|"AUTH / SCHEDULE / SETTINGS"| AS & SS & SET
  MA -->|"ViewModel Factory + userManager + appContext"| VM
  MA -->|"UserManager"| UM

  %% ── Локализация ───────────────────────────────────────────
  LM -->|"applySavedLanguage (до super.onCreate)"| MA
  SET -->|"setLanguage(context, code)"| LM
  AS -->|"LanguageSelectDialog"| LSD
  LSD -->|"setLanguage"| LM
```

---

## 2. Модуль учетных записей 1С (3 вида аккаунтов)

Приложение поддерживает 3 роли учетных записей, интегрированных с базой 1С:Колледж:

| Роль | `UserRole` | Описание и разграничение доступа | Тестовый логин (1С) |
|------|------------|-----------------------------------|----------------------|
| **Студент** | `STUDENT` | Просмотр расписания **строго своей группы** (жесткая привязка). Переключение чужих групп заблокировано | `student` / пароль `1234` |
| **Преподаватель** | `TEACHER` | Просмотр расписания любых групп, кафедры/отделения. Свободный выбор групп из БД | `teacher` / пароль `1234` |
| **Администратор** | `ADMINISTRATOR` | Полный доступ: управление расписанием, выбор любых групп, настройка сервера 1С | `admin` / пароль `admin` |

---

## 3. Модуль смены темы оформления

### Режимы темы (`ThemeMode`):
- `SYSTEM` — Следовать системной теме устройства (по умолчанию)
- `LIGHT` — Светлая тема
- `DARK` — Тёмная тема

### Цветовые палитры (`ThemePalette`):
- **Классическая** (`DEFAULT`) — Indigo / Deep Purple
- **Изумрудная мята** (`MINT`) — Свежая мятно-зеленая гамма с акцентом Claret
- **Лесная хвоя** (`PINE`) — Хвойный зеленый, Guerrilla Forest и Americana Blue
- **Океанический циан** (`OCEAN`) — Dark Cyan, Alabaster Grey и Deep Evergreen

---

## 4. Модуль уведомлений (Notifications)

- **Канал уведомлений**: `channel_schedule_notifications` (`IMPORTANCE_HIGH`)
- **Android 13+ (API 33+)**: runtime-запрос `POST_NOTIFICATIONS`
- **Напоминания о парах**: за 15 минут до начала занятия
- **Оповещения о заменах**: при обнаружении изменений в 1С
- **Тестовое уведомление**: мгновенная проверка прямо из экрана настроек
- **LessonReminderReceiver**: приёмник сигналов `AlarmManager`

---

## 5. Модуль локальной базы данных (SQLite) и изоляция групп студентов

1. **Автоматическая подгрузка групп из базы 1С в SQLite**:
   - При каждом запуске и по кнопке «Обновить» (`ScheduleUiEvent.Refresh`) репозиторий автоматически запрашивает актуальный список групп из HTTP-сервиса 1С:Колледж (`OneCApiClient.getGroups()`).
   - Свежие группы из 1С автоматически сохраняются и обновляются в локальной SQLite БД (`ScheduleDatabaseHelper.saveGroups()`).
   - Парсер `parseGroupsJson` поддерживает гибкие схемы 1С (`id`, `groupId`, `code`, `Код`, `Наименование`, `Курс`, `Отделение`).
   - При отсутствии сети или сбоях связи группы мгновенно отдаются из локальной БД SQLite.

2. **Подгрузка групп из БД при регистрации**:
   - На экране входа и регистрации (`AuthScreen`) при переключении на вкладку «Регистрация» группы подгружаются напрямую из локальной БД SQLite через `onLoadGroupsFromDb()` (`repository.getGroupsFromDb()`).
   - Студент выбирает свою группу из выпадающего списка `GroupSelectSheet`, получая актуальные данные без ожидания сетевых запросов.
   - Под полем выбора группы выводится статус `«Группы загружаются из базы данных»`.

3. **Изоляция групп для студентов (`canChangeGroup = false`)**:
   - Студент видит **строго и исключительно расписание своей группы** (жесткая привязка к профилю).
   - В `ScheduleUiState` список `groups` фильтруется до единственной группы студента (`groups = listOf(studentGroup)`).
   - В `ScheduleViewModel.onEvent(SelectGroup)` любые попытки переключения чужих групп блокируются (`if (!_uiState.value.canChangeGroup || currentUserRole == UserRole.STUDENT) return`).
   - В интерфейсе `ScheduleScreen` (TopAppBar):
     - Вместо стрелки выпадающего списка отображается иконка замка `Icons.Default.Lock`.
     - Заголовок отображает: `Моя группа: <Название>` (с поддержкой локализации RU, KK, EN).
     - Клик по плашке группы и вызов шторки выбора групп заблокированы.
   - Преподаватели и Администраторы сохраняют свободный доступ ко всем группам колледжа.
