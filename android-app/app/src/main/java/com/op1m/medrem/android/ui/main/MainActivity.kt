package com.op1m.medrem.android.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.op1m.medrem.android.R
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.TemporalAdjusters
import java.util.Locale

class MainActivity : AppCompatActivity(), CalendarDialogFragment.Listener {

    private lateinit var weekContainer: LinearLayout
    private val locale = Locale("ru")
    private var selectedDate: LocalDate? = null
    private val today: LocalDate = LocalDate.now()

    private val MONTHS_NOMINATIVE = arrayOf(
        "Январь","Февраль","Март","Апрель","Май","Июнь",
        "Июль","Август","Сентябрь","Октябрь","Ноябрь","Декабрь"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        weekContainer = findViewById(R.id.week_container)
        val tvMonthYear = findViewById<TextView>(R.id.tv_month_year)
        val ivArrow = findViewById<ImageView>(R.id.iv_month_arrow)
        if (savedInstanceState != null) {
            val saved = savedInstanceState.getString("selected_date")
            if (saved != null) selectedDate = LocalDate.parse(saved)
        } else {
            selectedDate = today
        }
        val todayDate = LocalDate.now()
        setWeekForDate(todayDate)
        val monthNameInit = MONTHS_NOMINATIVE[todayDate.monthValue - 1]
        tvMonthYear.text = "$monthNameInit, ${todayDate.year}:"
        val openCalendar = View.OnClickListener {
            val anchor = findViewById<View>(R.id.iv_month_arrow)
            val popup = CalendarPopup(
                this,
                YearMonth.now().year,
                YearMonth.now().monthValue,
                selectedDate,
                object : CalendarDialogFragment.Listener {
                    override fun onDateSelected(date: LocalDate) {
                        selectedDate = date
                        val tvMonthYear = findViewById<TextView>(R.id.tv_month_year)
                        val monthName = MONTHS_NOMINATIVE[date.monthValue - 1]
                        tvMonthYear.text = "$monthName, ${date.year}:"
                        setWeekForDate(date)
                    }
                }
            )
            popup.show(anchor)
        }
        findViewById<View>(R.id.month_year_container).setOnClickListener(openCalendar)
        ivArrow.setOnClickListener(openCalendar)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedDate?.let { outState.putString("selected_date", it.toString()) }
    }

    private fun setWeekForDate(date: LocalDate) {
        weekContainer.removeAllViews()
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val inflater = LayoutInflater.from(this)

        for (i in 0..6) {
            val d = monday.plusDays(i.toLong())
            val item = inflater.inflate(R.layout.item_week_day, weekContainer, false) as View
            val tvNum = item.findViewById<TextView>(R.id.tv_day_num)
            val tvAbbr = item.findViewById<TextView>(R.id.tv_day_abbr)

            tvNum.text = d.dayOfMonth.toString()
            tvAbbr.text = d.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).lowercase(locale)

            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            item.layoutParams = lp
            item.isClickable = true

            val isToday = d == today
            val isSelected = selectedDate != null && d == selectedDate

            if (isSelected) {
                tvNum.setBackgroundResource(R.drawable.day_bg_selected)
                tvNum.setTextColor(Color.WHITE)
            } else if (isToday) {
                tvNum.setBackgroundResource(R.drawable.day_bg_today_outline)
                tvNum.setTextColor(Color.parseColor("#263238"))
            } else {
                tvNum.setBackgroundResource(R.drawable.week_day_bg_selector)
                tvNum.setTextColor(Color.parseColor("#263238"))
            }

            item.setOnClickListener {
                selectedDate = d
                setWeekForDate(d)
            }
            weekContainer.addView(item)
        }
    }

    override fun onDateSelected(date: LocalDate) {
        selectedDate = date
        val tvMonthYear = findViewById<TextView>(R.id.tv_month_year)
        val monthName = MONTHS_NOMINATIVE[date.monthValue - 1]
        tvMonthYear.text = "$monthName, ${date.year}:"
        setWeekForDate(date)
    }
}
