package com.op1m.medrem.android.ui.splash

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.op1m.medrem.android.R
import com.op1m.medrem.android.data.api.RetrofitClient
import com.op1m.medrem.android.ui.auth.LoginActivity
import com.op1m.medrem.android.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SplashActivity : AppCompatActivity() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvFactBody: TextView
    private lateinit var prefs: SharedPreferences

    private var isLoggedIn = false
    private var userName: String? = null

    private val healthFacts = listOf(
        "витамин A: важен для зрения и здоровья кожи",
        "регулярные физические упражнения укрепляют сердечно-сосудистую систему",
        "здоровый сон помогает улучшить память и концентрацию",
        "пить достаточное количество воды необходимо для обмена веществ",
        "прогулки на свежем воздухе укрепляют иммунитет",
        "сбалансированное питание - ключ к хорошему самочувствию",
        "медитация и релаксация снижают уровень стресса",
        "регулярные медицинские обследования помогают предотвратить заболевания"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        initViews()
        setGreetingBasedOnTime()
        showRandomFact()
        checkAuthentication()
    }

    private fun initViews() {
        tvGreeting = findViewById(R.id.tv_greeting)
        tvFactBody = findViewById(R.id.tv_fact_body)
        prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
    }

    private fun setGreetingBasedOnTime() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when {
            hour in 5..11 -> "Доброе утро"
            hour in 12..16 -> "Добрый день"
            hour in 17..22 -> "Добрый вечер"
            else -> "Доброй ночи"
        }

        tvGreeting.text = "$greeting!"
    }

    private fun showRandomFact() {
        val randomFact = healthFacts.random()
        tvFactBody.text = randomFact
    }

    private fun checkAuthentication() {
        val savedUsername = prefs.getString("username", null)
        val savedPassword = prefs.getString("password", null)

        if (!savedUsername.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    RetrofitClient.setAuthCredentials(savedUsername, savedPassword)
                    val response = RetrofitClient.getApiService().getCurrentUser()

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val user = response.body()
                            isLoggedIn = true
                            userName = user?.firstName ?: user?.username ?: savedUsername
                            updateGreetingWithName()

                            Handler(Looper.getMainLooper()).postDelayed({
                                navigateToMain()
                            }, 3000)
                        } else {
                            navigateToLoginDelayed()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        navigateToLoginDelayed()
                    }
                }
            }
        } else {
            navigateToLoginDelayed()
        }
    }

    private fun updateGreetingWithName() {
        val currentText = tvGreeting.text.toString()
        val baseGreeting = currentText.replace("!", "")
        tvGreeting.text = "$baseGreeting,\n$userName!"
    }

    private fun navigateToLoginDelayed() {
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToLogin()
        }, 3000)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun startAnimations() {
        tvGreeting.alpha = 0f
        tvGreeting.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(300)
            .start()

        findViewById<ImageView>(R.id.iv_logo).apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0f)
                .setDuration(1000)
                .setStartDelay(500)
                .start()
        }

        findViewById<TextView>(R.id.tv_fact_title).apply {
            alpha = 0f
            translationY = 50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(1000)
                .start()
        }

        tvFactBody.alpha = 0f
        tvFactBody.translationY = 30f
        tvFactBody.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(1200)
            .start()
    }
}