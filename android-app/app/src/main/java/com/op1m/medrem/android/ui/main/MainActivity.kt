package com.op1m.medrem.android.ui.main

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.op1m.medrem.android.R
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var leftNavPanel: LinearLayout
    private lateinit var itemHome: FrameLayout
    private lateinit var itemCourses: FrameLayout
    private lateinit var itemProfile: FrameLayout
    private lateinit var contentHome: LinearLayout
    private lateinit var contentCourses: LinearLayout
    private lateinit var contentProfile: LinearLayout
    private lateinit var textHome: TextView
    private lateinit var textCourses: TextView
    private lateinit var textProfile: TextView
    private lateinit var iconHome: ImageView
    private lateinit var iconCourses: ImageView
    private lateinit var iconProfile: ImageView

    private var currentSelected = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupLeftNav()

        if (savedInstanceState == null) {
            showDiary()
            currentSelected = 1
        } else {
            currentSelected = savedInstanceState.getInt("selected_tab", 1)
            when (currentSelected) {
                1 -> { setSelectedVisual(1); showDiary() }
                2 -> { setSelectedVisual(2); showStats() }
                3 -> { setSelectedVisual(3); showProfile() }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selected_tab", currentSelected)
    }

    private fun setupLeftNav() {
        leftNavPanel = findViewById(R.id.left_nav_panel)
        itemHome = findViewById(R.id.item_home)
        itemCourses = findViewById(R.id.item_courses)
        itemProfile = findViewById(R.id.item_profile)
        contentHome = findViewById(R.id.content_home)
        contentCourses = findViewById(R.id.content_courses)
        contentProfile = findViewById(R.id.content_profile)
        textHome = findViewById(R.id.text_home)
        textCourses = findViewById(R.id.text_courses)
        textProfile = findViewById(R.id.text_profile)
        iconHome = findViewById(R.id.icon_home)
        iconCourses = findViewById(R.id.icon_courses)
        iconProfile = findViewById(R.id.icon_profile)

        val dm = resources.displayMetrics
        val screenW = dm.widthPixels
        val panelWidth = (screenW * 0.60).roundToInt()
        val expandedWidth = (panelWidth * 0.60).roundToInt() 
        val collapsedWidth = (panelWidth * 0.20).roundToInt()

        val buttonHeightPx = (resources.displayMetrics.density * 48).roundToInt()
        listOf(itemHome, itemCourses, itemProfile).forEach { item ->
            val lp = item.layoutParams
            lp.height = buttonHeightPx
            item.layoutParams = lp
        }

        val iconSizePx = (resources.displayMetrics.density * 30).roundToInt()
        listOf(iconHome, iconCourses, iconProfile).forEach { icon ->
            val lp = icon.layoutParams
            lp.width = iconSizePx
            lp.height = iconSizePx
            icon.layoutParams = lp
        }

        setItemWidthsInstant(expandedWidth, collapsedWidth, collapsedWidth)
        textHome.visibility = View.VISIBLE
        textCourses.visibility = View.GONE
        textProfile.visibility = View.GONE
        contentHome.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        contentCourses.gravity = Gravity.CENTER
        contentProfile.gravity = Gravity.CENTER
        setSelectedVisual(1)

        itemHome.setOnClickListener { switchNav(1, expandedWidth, collapsedWidth) }
        itemCourses.setOnClickListener { switchNav(2, expandedWidth, collapsedWidth) }
        itemProfile.setOnClickListener { switchNav(3, expandedWidth, collapsedWidth) }
    }

    private fun setItemWidthsInstant(w1: Int, w2: Int, w3: Int) {
        val lp1 = itemHome.layoutParams
        lp1.width = w1
        itemHome.layoutParams = lp1

        val lp2 = itemCourses.layoutParams
        lp2.width = w2
        itemCourses.layoutParams = lp2

        val lp3 = itemProfile.layoutParams
        lp3.width = w3
        itemProfile.layoutParams = lp3
    }

    private fun animateWidth(view: ViewGroup, to: Int, duration: Long = 220, onEnd: (() -> Unit)? = null) {
        val from = view.width
        val anim = ValueAnimator.ofInt(from, to)
        anim.duration = duration
        anim.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            val lp = view.layoutParams
            lp.width = value
            view.layoutParams = lp
        }
        anim.addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationEnd(animation: android.animation.Animator) { onEnd?.invoke() }
            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })
        anim.start()
    }

    private fun setSelectedVisual(index: Int) {
        when (index) {
            1 -> {
                contentHome.setBackgroundResource(R.drawable.nav_item_bg_selected)
                iconHome.setColorFilter(Color.WHITE)
                contentCourses.setBackgroundResource(R.drawable.nav_item_bg_default)
                iconCourses.setColorFilter(Color.parseColor("#2E86FF"))
                contentProfile.setBackgroundResource(R.drawable.nav_item_bg_default)
                iconProfile.setColorFilter(Color.parseColor("#2E86FF"))
            }
            2 -> {
                contentCourses.setBackgroundResource(R.drawable.nav_item_bg_selected)
                iconCourses.setColorFilter(Color.WHITE)
                contentHome.setBackgroundResource(R.drawable.nav_item_bg_default)
                iconHome.setColorFilter(Color.parseColor("#2E86FF"))
                contentProfile.setBackgroundResource(R.drawable.nav_item_bg_default)
                iconProfile.setColorFilter(Color.parseColor("#2E86FF"))
            }
            3 -> {
                contentProfile.setBackgroundResource(R.drawable.nav_item_bg_selected)
                iconProfile.setColorFilter(Color.WHITE)
                contentHome.setBackgroundResource(R.drawable.nav_item_bg_default)
                iconHome.setColorFilter(Color.parseColor("#2E86FF"))
                contentCourses.setBackgroundResource(R.drawable.nav_item_bg_default)
                iconCourses.setColorFilter(Color.parseColor("#2E86FF"))
            }
        }
    }

    private fun switchNav(target: Int, expandedWidth: Int, collapsedWidth: Int) {
        if (target == currentSelected) return

        val oldContent = when (currentSelected) {
            1 -> contentHome
            2 -> contentCourses
            else -> contentProfile
        }
        val newContent = when (target) {
            1 -> contentHome
            2 -> contentCourses
            else -> contentProfile
        }
        val oldText = when (currentSelected) {
            1 -> textHome
            2 -> textCourses
            else -> textProfile
        }
        val newText = when (target) {
            1 -> textHome
            2 -> textCourses
            else -> textProfile
        }

        newText.visibility = View.GONE
        oldContent.gravity = (Gravity.CENTER_VERTICAL or Gravity.START)

        when (currentSelected) {
            1 -> animateWidth(itemHome, collapsedWidth) {
                oldText.visibility = View.GONE
                contentHome.gravity = Gravity.CENTER
            }
            2 -> animateWidth(itemCourses, collapsedWidth) {
                oldText.visibility = View.GONE
                contentCourses.gravity = Gravity.CENTER
            }
            3 -> animateWidth(itemProfile, collapsedWidth) {
                oldText.visibility = View.GONE
                contentProfile.gravity = Gravity.CENTER
            }
        }

        when (target) {
            1 -> animateWidth(itemHome, expandedWidth) {
                newText.visibility = View.VISIBLE
                contentHome.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            }
            2 -> animateWidth(itemCourses, expandedWidth) {
                newText.visibility = View.VISIBLE
                contentCourses.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            }
            3 -> animateWidth(itemProfile, expandedWidth) {
                newText.visibility = View.VISIBLE
                contentProfile.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            }
        }

        currentSelected = target
        setSelectedVisual(target)

        when (target) {
            1 -> showDiary()
            2 -> showStats()
            3 -> showProfile()
        }
    }

    private fun showDiary() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_container, DiaryFragment.newInstance())
            .commit()
    }

    private fun showStats() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_container, StatsFragment.newInstance())
            .commit()
    }

    private fun showProfile() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_container, ProfileFragment.newInstance())
            .commit()
    }
}
