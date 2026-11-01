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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.TJPK.tapsiris.data.mock.MockScheduleData
import com.TJPK.tapsiris.domain.model.StudentGroup
import com.TJPK.tapsiris.presentation.components.GroupSelectSheet
import com.TJPK.tapsiris.presentation.settings.LanguageSelectDialog
import com.TJPK.tapsiris.utils.LanguageManager

/**
 * =========================================================================================
 * СЛОЙ PRESENTATION / AUTH: Экран входа и регистрации студента.
 * Отображается при первом запуске приложения или после выхода из аккаунта.
 * =========================================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    groups: List<StudentGroup> = MockScheduleData.groups,
    onAuthSuccess: (UserProfile) -> Unit,
    onGuestLogin: (String) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // 0 = Вход, 1 = Регистрация
    var selectedTab by remember { mutableIntStateOf(0) }

    // Поля формы
    var loginInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var fullNameInput by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf(groups.firstOrNull() ?: MockScheduleData.groups.first()) }
    var passwordVisible by remember { mutableStateOf(false) }

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
        onAuthSuccess(
            UserProfile(
                fullName = if (fullNameInput.isNotBlank()) fullNameInput else "$defaultStudentPrefix ${loginInput.trim()}",
                login = loginInput.trim(),
                groupName = selectedGroup.name,
                isGuest = false
            )
        )
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
        onAuthSuccess(
            UserProfile(
                fullName = fullNameInput.trim(),
                login = loginInput.trim(),
                groupName = selectedGroup.name,
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
                    Text(text = stringResource(R.string.settings_language))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Логотип и брендинг колледжа
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
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

            Spacer(modifier = Modifier.height(28.dp))

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

                    // Поле ФИО (только при регистрации)
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

                    // Поле выбора учебной группы
                    val courseText = stringResource(R.string.course_suffix)
                    OutlinedTextField(
                        value = "${selectedGroup.name} (${selectedGroup.course} $courseText)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.label_study_group)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Badge, contentDescription = null)
                        },
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showGroupSheet = true },
                        enabled = false,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Поле Логин / Номер билета
                    OutlinedTextField(
                        value = loginInput,
                        onValueChange = { loginInput = it },
                        label = { Text(if (selectedTab == 0) stringResource(R.string.label_login_id) else stringResource(R.string.label_create_login)) },
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
                            if (selectedTab == 0) handleLogin() else handleRegister()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (selectedTab == 0) stringResource(R.string.btn_sign_in) else stringResource(R.string.btn_register),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Кнопка Быстрый гостевой вход
                    OutlinedButton(
                        onClick = {
                            onGuestLogin(selectedGroup.name)
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
            groups = groups,
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