package com.TJPK.tapsiris.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.TJPK.tapsiris.R
import com.TJPK.tapsiris.data.local.UserProfile
import com.TJPK.tapsiris.data.local.UserRole
import com.TJPK.tapsiris.data.mock.MockScheduleData
import com.TJPK.tapsiris.domain.model.StudentGroup
import com.TJPK.tapsiris.presentation.components.GroupSelectSheet
import com.TJPK.tapsiris.presentation.settings.LanguageSelectDialog
import com.TJPK.tapsiris.utils.LanguageManager
import kotlinx.coroutines.launch

/**
 * =========================================================================================
 * СЛОЙ PRESENTATION / AUTH: Экран входа и регистрации с 3 типами учетных записей (1С):
 * 1. Студент (STUDENT)
 * 2. Преподаватель (TEACHER)
 * 3. Администратор (ADMINISTRATOR)
 *
 * Учетные записи аутентифицируются в базе 1С:Колледж.
 * =========================================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    groups: List<StudentGroup> = MockScheduleData.groups,
    onLoadGroups: (suspend () -> Result<List<StudentGroup>>)? = null,
    onLoadGroupsFromDb: (suspend () -> Result<List<StudentGroup>>)? = null,
    onAuthenticate: (suspend (String, String, UserRole) -> Result<UserProfile>)? = null,
    onAuthSuccess: (UserProfile) -> Unit,
    // groupName и groupId — для правильного запроса расписания густя
    onGuestLogin: (groupName: String, groupId: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 0 = Вход, 1 = Регистрация
    var selectedTab by remember { mutableIntStateOf(0) }

    // Выбранная роль (Студент, Преподаватель, Администратор)
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }

    // Поля формы
    var loginInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var fullNameInput by remember { mutableStateOf("") }
    var departmentInput by remember { mutableStateOf("") }
    var currentGroups by remember(groups) { mutableStateOf(groups) }
    var selectedGroup by remember { mutableStateOf(currentGroups.firstOrNull() ?: MockScheduleData.groups.first()) }
    // Флаг загрузки групп из 1С
    var isLoadingGroups by remember { mutableStateOf(false) }

    // При открытии экрана и при переключении таба — автоматически грузим группы из 1С базы
    LaunchedEffect(selectedTab, selectedRole, onLoadGroups, onLoadGroupsFromDb) {
        val loader = onLoadGroups ?: onLoadGroupsFromDb
        if (loader != null) {
            isLoadingGroups = true
            val res = loader()
            isLoadingGroups = false
            if (res.isSuccess) {
                val fresh = res.getOrNull()
                if (!fresh.isNullOrEmpty()) {
                    currentGroups = fresh
                    if (selectedGroup !in fresh) {
                        selectedGroup = fresh.first()
                    }
                }
            }
        }
    }
    var passwordVisible by remember { mutableStateOf(false) }

    // Состояние загрузки (проверка в 1С)
    var isAuthenticating by remember { mutableStateOf(false) }

    // Ошибки валидации
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Шторка выбора группы и диалог языка
    var showGroupSheet by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    val groupSheetState = rememberModalBottomSheetState()

    val errEnterLogin = stringResource(R.string.err_enter_login)
    val errEnterPassword = stringResource(R.string.err_enter_password)
    val errEnterFullName = stringResource(R.string.err_enter_full_name)
    val errCreateLogin = stringResource(R.string.err_create_login)
    val errPasswordShort = stringResource(R.string.err_password_short)
    val defaultStudentPrefix = stringResource(R.string.student_prefix)

    fun handleLogin() {
        if (loginInput.isBlank()) {
            errorMessage = errEnterLogin
            return
        }
        if (passwordInput.isBlank()) {
            errorMessage = errEnterPassword
            return
        }
        errorMessage = null

        if (onAuthenticate != null) {
            isAuthenticating = true
            coroutineScope.launch {
                val result = onAuthenticate(loginInput, passwordInput, selectedRole)
                isAuthenticating = false
                if (result.isSuccess) {
                    val profile = result.getOrNull() ?: MockScheduleData.createDefaultProfileForRole(
                        login = loginInput,
                        role = selectedRole,
                        group = selectedGroup.name
                    )
                    onAuthSuccess(profile)
                } else {
                    val err = result.exceptionOrNull()?.localizedMessage
                        ?: context.getString(R.string.error_connection)
                    errorMessage = err
                    snackbarHostState.showSnackbar(err)
                }
            }
        } else {
            // Режим Compose Preview / прямого вызова
            val mock = MockScheduleData.authenticateMock(loginInput, selectedRole)
                ?: MockScheduleData.createDefaultProfileForRole(loginInput, selectedRole, selectedGroup.name)
            onAuthSuccess(mock)
        }
    }

    fun handleRegister() {
        if (fullNameInput.isBlank()) {
            errorMessage = errEnterFullName
            return
        }
        if (loginInput.isBlank()) {
            errorMessage = errCreateLogin
            return
        }
        if (passwordInput.length < 4) {
            errorMessage = errPasswordShort
            return
        }
        errorMessage = null

        val dept = when (selectedRole) {
            UserRole.STUDENT -> selectedGroup.department
            UserRole.TEACHER -> departmentInput.ifBlank { "Кафедра информационных технологий" }
            UserRole.ADMINISTRATOR -> "Учебная часть (1С)"
        }

        onAuthSuccess(
            UserProfile(
                fullName = fullNameInput.trim(),
                login = loginInput.trim(),
                role = selectedRole,
                // Сохраняем UUID группы из 1С — он нужен для запроса расписания
                groupName = if (selectedRole == UserRole.STUDENT) selectedGroup.name else "",
                groupId = if (selectedRole == UserRole.STUDENT) selectedGroup.id else "",
                department = dept,
                isGuest = false
            )
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Кнопка смены языка в правом верхнем углу
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { showLanguageDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = when (LanguageManager.getCurrentLanguageCode(context)) {
                            "kk" -> "Қазақша"
                            "en" -> "English"
                            else -> "Русский"
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Логотип приложения
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(76.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (selectedRole) {
                            UserRole.STUDENT -> Icons.Default.School
                            UserRole.TEACHER -> Icons.Default.Badge
                            UserRole.ADMINISTRATOR -> Icons.Default.AdminPanelSettings
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(R.string.app_system_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Селектор роли: Студент / Преподаватель / Администратор
            Text(
                text = stringResource(R.string.select_role),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val roles = listOf(
                    Triple(UserRole.STUDENT, stringResource(R.string.role_student), Icons.Default.School),
                    Triple(UserRole.TEACHER, stringResource(R.string.role_teacher), Icons.Default.Badge),
                    Triple(UserRole.ADMINISTRATOR, stringResource(R.string.role_admin), Icons.Default.AdminPanelSettings)
                )

                roles.forEach { (role, label, icon) ->
                    val isSelected = selectedRole == role
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedRole = role
                            errorMessage = null
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Карточка с формой
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Переключатель: Вход / Регистрация
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.clip(RoundedCornerShape(14.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                errorMessage = null
                            },
                            text = { Text(stringResource(R.string.tab_login), fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                errorMessage = null
                            },
                            text = { Text(stringResource(R.string.tab_register), fontWeight = FontWeight.SemiBold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Поле ФИО (при регистрации)
                    if (selectedTab == 1) {
                        OutlinedTextField(
                            value = fullNameInput,
                            onValueChange = { fullNameInput = it },
                            label = { Text(stringResource(R.string.label_full_name)) },
                            placeholder = { Text(stringResource(R.string.placeholder_full_name)) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Поле группы (для Студента)
                    if (selectedRole == UserRole.STUDENT) {
                        val courseText = stringResource(R.string.course_suffix)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showGroupSheet = true }
                        ) {
                            OutlinedTextField(
                                value = "${selectedGroup.name} (${selectedGroup.course} $courseText)",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.label_study_group)) },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.School, contentDescription = null)
                                },
                                trailingIcon = {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        // Показываем спиннер пока группы грузятся из 1С
                        if (isLoadingGroups) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Загрузка групп из 1С...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.groups_loaded_from_db),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    } else if (selectedRole == UserRole.TEACHER && selectedTab == 1) {
                        // Для преподавателя при регистрации — кафедра
                        OutlinedTextField(
                            value = departmentInput,
                            onValueChange = { departmentInput = it },
                            label = { Text(stringResource(R.string.department_label)) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Badge, contentDescription = null)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Поле Логин / Номер билета
                    OutlinedTextField(
                        value = loginInput,
                        onValueChange = { loginInput = it },
                        label = {
                            Text(
                                if (selectedTab == 0) stringResource(R.string.label_login_id)
                                else stringResource(R.string.label_create_login)
                            )
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Поле Пароль
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text(stringResource(R.string.label_password)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Вывод ошибок валидации
                    AnimatedVisibility(visible = errorMessage != null) {
                        errorMessage?.let { error ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Главная кнопка Вход / Зарегистрироваться
                    Button(
                        onClick = {
                            if (!isAuthenticating) {
                                if (selectedTab == 0) handleLogin() else handleRegister()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isAuthenticating
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.auth_verifying_1c),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Text(
                                text = if (selectedTab == 0) stringResource(R.string.btn_sign_in) else stringResource(R.string.btn_register),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Кнопка Быстрый гостевой вход (выбранная группа и её id из 1С передаются)
                    OutlinedButton(
                        onClick = {
                            onGuestLogin(selectedGroup.name, selectedGroup.id)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_continue_guest),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Панель быстрого заполнения демо-аккаунтов 1С
                    Text(
                        text = stringResource(R.string.quick_demo_login),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedRole = UserRole.STUDENT
                                loginInput = "student"
                                passwordInput = "1234"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Студент", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = {
                                selectedRole = UserRole.TEACHER
                                loginInput = "teacher"
                                passwordInput = "1234"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Преподаватель", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = {
                                selectedRole = UserRole.ADMINISTRATOR
                                loginInput = "admin"
                                passwordInput = "admin"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Админ", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.sync_info_1c),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontSize = 11.sp
            )
        }
    }

    // Выбор группы при регистрации или входе
    if (showGroupSheet) {
        GroupSelectSheet(
            groups = currentGroups,
            selectedGroup = selectedGroup,
            sheetState = groupSheetState,
            onDismiss = { showGroupSheet = false },
            onGroupSelected = {
                selectedGroup = it
            }
        )
    }

    // Диалог выбора языка
    if (showLanguageDialog) {
        LanguageSelectDialog(
            currentCode = LanguageManager.getCurrentLanguageCode(context),
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { langCode ->
                LanguageManager.setLanguage(context, langCode)
                showLanguageDialog = false
            }
        )
    }
}
