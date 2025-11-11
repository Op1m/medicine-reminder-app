package com.op1m.medrem.android.ui.main

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.op1m.medrem.android.R

class MainActivity : AppCompatActivity() {

    private lateinit var tab1: LinearLayout
    private lateinit var tab2: LinearLayout
    private lateinit var tab3: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tab1 = findViewById(R.id.btn_tab_1)
        tab2 = findViewById(R.id.btn_tab_2)
        tab3 = findViewById(R.id.btn_tab_3)

        tab1.setOnClickListener { showDiary() }
        tab2.setOnClickListener { showStats() }
        tab3.setOnClickListener { showProfile() }

        if (savedInstanceState == null) {
            showDiary()
        }
    }

    private fun showDiary() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_container, DiaryFragment.newInstance())
            .commit()
        setActiveTab(1)
    }

    private fun showStats() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_container, StatsFragment.newInstance())
            .commit()
        setActiveTab(2)
    }

    private fun showProfile() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_container, ProfileFragment.newInstance())
            .commit()
        setActiveTab(3)
    }

    private fun setActiveTab(index: Int) {
        tab1.alpha = if (index == 1) 1f else 0.55f
        tab2.alpha = if (index == 2) 1f else 0.55f
        tab3.alpha = if (index == 3) 1f else 0.55f
    }
}
