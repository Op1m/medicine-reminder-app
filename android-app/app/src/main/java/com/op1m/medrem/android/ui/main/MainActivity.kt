package com.op1m.medrem.android.ui.main

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.op1m.medrem.android.data.api.RetrofitClient
import com.op1m.medrem.android.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        loadUserData()
        setupClickListeners()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun loadUserData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.getApiService().getCurrentUser()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        user?.let {
                            tvWelcome.text = "Добро пожаловать, ${it.username}!"
                        }
                    } else {
                        tvWelcome.text = "Ошибка загрузки данных пользователя"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvWelcome.text = "Ошибка сети: ${e.message}"
                }
            }
        }
    }

    private fun setupClickListeners() {
        btnLogout.setOnClickListener {
            RetrofitClient.clearAuth()
            finish()
        }
    }
}