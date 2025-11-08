package com.op1m.medrem.android.ui.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.op1m.medrem.android.R
import com.op1m.medrem.android.data.api.RetrofitClient
import com.op1m.medrem.android.data.model.LoginRequest
import com.op1m.medrem.android.data.model.UserRegistrationRequest
import com.op1m.medrem.android.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnAction: Button
    private lateinit var tvSwitchForm: TextView
    private lateinit var tvFormTitle: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var prefs: SharedPreferences

    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupClickListeners()
        prefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etFirstName = findViewById(R.id.etFirstName)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnAction = findViewById(R.id.btnAction)
        tvSwitchForm = findViewById(R.id.tvSwitchForm)
        tvFormTitle = findViewById(R.id.tvFormTitle)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        btnAction.setOnClickListener {
            // Анимация нажатия кнопки
            btnAction.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    btnAction.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()

                    if (isLoginMode) {
                        performLogin()
                    } else {
                        performRegistration()
                    }
                }
                .start()
        }

        tvSwitchForm.setOnClickListener {
            toggleFormMode()
        }
    }

    private fun toggleFormMode() {
        isLoginMode = !isLoginMode

        if (isLoginMode) {
            tvFormTitle.text = "Вход"
            btnAction.text = "Войти"
            tvSwitchForm.text = "Нет аккаунта? Зарегистрироваться"

            animateHideFields()
        } else {
            tvFormTitle.text = "Регистрация"
            btnAction.text = "Зарегистрироваться"
            tvSwitchForm.text = "Уже есть аккаунт? Войти"

            animateShowFields()
        }

        clearFields()
    }

    private fun animateHideFields() {
        etEmail.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                etEmail.visibility = View.GONE
            }
            .start()

        etFirstName.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                etFirstName.visibility = View.GONE
            }
            .start()

        etConfirmPassword.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                etConfirmPassword.visibility = View.GONE
            }
            .start()
    }

    private fun animateShowFields() {
        etEmail.visibility = View.VISIBLE
        etEmail.alpha = 0f
        etEmail.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        etFirstName.visibility = View.VISIBLE
        etFirstName.alpha = 0f
        etFirstName.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        etConfirmPassword.visibility = View.VISIBLE
        etConfirmPassword.alpha = 0f
        etConfirmPassword.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    private fun clearFields() {
        etEmail.text.clear()
        etFirstName.text.clear()
        etUsername.text.clear()
        etPassword.text.clear()
        etConfirmPassword.text.clear()
    }

    private fun performLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            showError("Заполните все поля")
            return
        }

        showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.setAuthCredentials(username, password)

                val response = RetrofitClient.getApiService().getCurrentUser()

                withContext(Dispatchers.Main) {
                    hideLoading()

                    if (response.isSuccessful) {
                        val user = response.body()
                        saveUserData(username, password, user?.firstName ?: "")

                        showSuccess("Вход выполнен успешно!")

                        navigateToMain()
                    } else {
                        showError("Ошибка входа: неверные данные")
                        RetrofitClient.clearAuth()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideLoading()
                    showError("Ошибка сети: ${e.message}")
                    RetrofitClient.clearAuth()
                }
            }
        }
    }

    private fun performRegistration() {
        val email = etEmail.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (email.isEmpty() || firstName.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Заполните все поля")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Введите корректный email")
            return
        }

        if (password != confirmPassword) {
            showError("Пароли не совпадают")
            return
        }

        if (password.length < 6) {
            showError("Пароль должен содержать минимум 6 символов")
            return
        }

        showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val registrationRequest = UserRegistrationRequest(
                    username = username,
                    password = password,
                    email = email,
                    firstName = firstName
                )

                val response = RetrofitClient.getApiService().register(registrationRequest)

                withContext(Dispatchers.Main) {
                    hideLoading()

                    if (response.isSuccessful) {
                        // Сохраняем данные пользователя
                        saveUserData(username, password, firstName)

                        // Успешная регистрация
                        showSuccess("Регистрация успешна!")

                        // Автоматически логинимся после регистрации
                        performAutoLogin(username, password)
                    } else {
                        // Ошибка регистрации
                        val errorMessage = when (response.code()) {
                            409 -> "Пользователь с таким именем или email уже существует"
                            400 -> "Некорректные данные"
                            else -> "Ошибка регистрации: ${response.code()}"
                        }
                        showError(errorMessage)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideLoading()
                    showError("Ошибка сети: ${e.message}")
                }
            }
        }
    }

    private fun performAutoLogin(username: String, password: String) {
        showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.setAuthCredentials(username, password)
                val response = RetrofitClient.getApiService().getCurrentUser()

                withContext(Dispatchers.Main) {
                    hideLoading()

                    if (response.isSuccessful) {
                        showSuccess("Автоматический вход выполнен!")
                        navigateToMain()
                    } else {
                        showSuccess("Регистрация завершена! Выполните вход вручную")
                        // Переключаем на форму входа и заполняем данные
                        switchToLoginWithCredentials(username, password)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideLoading()
                    showSuccess("Регистрация завершена! Выполните вход вручную")
                    switchToLoginWithCredentials(username, password)
                }
            }
        }
    }

    private fun switchToLoginWithCredentials(username: String, password: String) {
        isLoginMode = true
        toggleFormMode()
        etUsername.setText(username)
        etPassword.setText(password)
    }

    private fun saveUserData(username: String, password: String, firstName: String) {
        prefs.edit().apply {
            putString("username", username)
            putString("password", password)
            putString("user_first_name", firstName)
            apply()
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        btnAction.isEnabled = false
        findViewById<View>(R.id.formContainer)?.animate()?.alpha(0.7f)?.setDuration(300)?.start()
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
        btnAction.isEnabled = true
        findViewById<View>(R.id.formContainer)?.animate()?.alpha(1f)?.setDuration(300)?.start()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}