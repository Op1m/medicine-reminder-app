package com.op1m.medrem.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.op1m.medrem.android.R
import com.op1m.medrem.android.data.api.RetrofitClient
import com.op1m.medrem.android.data.model.UserRegistrationRequest
import com.op1m.medrem.android.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnAction: Button
    private lateinit var tvSwitchForm: TextView
    private lateinit var tvFormTitle: TextView
    private lateinit var progressBar: ProgressBar

    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
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
            if (isLoginMode) {
                performLogin()
            } else {
                performRegistration()
            }
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

            etEmail.visibility = View.GONE
            etConfirmPassword.visibility = View.GONE
        } else {
            tvFormTitle.text = "Регистрация"
            btnAction.text = "Зарегистрироваться"
            tvSwitchForm.text = "Уже есть аккаунт? Войти"
            etEmail.visibility = View.VISIBLE
            etConfirmPassword.visibility = View.VISIBLE
        }

        etEmail.text.clear()
        etUsername.text.clear()
        etPassword.text.clear()
        etConfirmPassword.text.clear()
    }

    private fun performLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnAction.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.setAuthCredentials(username, password)

                val response = RetrofitClient.getApiService().getCurrentUser()

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnAction.isEnabled = true

                    if (response.isSuccessful) {
                        Toast.makeText(this@LoginActivity, "Вход выполнен успешно!", Toast.LENGTH_SHORT).show()

                        val intent = android.content.Intent(this@LoginActivity, com.op1m.medrem.android.ui.main.MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Ошибка входа: неверные данные", Toast.LENGTH_SHORT).show()
                        RetrofitClient.clearAuth()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnAction.isEnabled = true
                    Toast.makeText(this@LoginActivity, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
                    RetrofitClient.clearAuth()
                }
            }
        }
    }

    private fun performRegistration() {
        val email = etEmail.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnAction.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val registrationRequest = UserRegistrationRequest(
                    username = username,
                    password = password,
                    email = email
                )

                val response = RetrofitClient.getApiService().register(registrationRequest)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnAction.isEnabled = true

                    if (response.isSuccessful) {
                        Toast.makeText(this@LoginActivity, "Регистрация успешна! Выполните вход", Toast.LENGTH_SHORT).show()

                        isLoginMode = true
                        toggleFormMode()
                        etUsername.setText(username)
                        etPassword.setText(password)
                    } else {
                        // Ошибка регистрации
                        val errorMessage = when (response.code()) {
                            409 -> "Пользователь с таким именем или email уже существует"
                            400 -> "Некорректные данные"
                            else -> "Ошибка регистрации: ${response.code()}"
                        }
                        Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnAction.isEnabled = true
                    Toast.makeText(this@LoginActivity, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}