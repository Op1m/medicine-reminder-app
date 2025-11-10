package com.op1m.medrem.android.ui.main

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        weekContainer = findViewById(R.id.week_container)
        val tvMonthYear = findViewById<TextView>(R.id.tv_month_year)
        val ivArrow = findViewById<ImageView>(R.id.iv_month_arrow)

        val today = LocalDate.now()
        setWeekForDate(today)
        tvMonthYear.text = today.month.getDisplayName(TextStyle.FULL_STANDALONE, locale) + " " + today.year.toString()

        val openCalendar = View.OnClickListener {
            val dlg = CalendarDialogFragment.newInstance(YearMonth.now().year, YearMonth.now().monthValue)
            dlg.show(supportFragmentManager, "calendar")
        }
        findViewById<View>(R.id.month_year_container).setOnClickListener(openCalendar)
        ivArrow.setOnClickListener(openCalendar)
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
            item.setOnClickListener {
                for (j in 0 until weekContainer.childCount) {
                    weekContainer.getChildAt(j).findViewById<View>(R.id.tv_day_num).isSelected = false
                }
                tvNum.isSelected = true
            }
            weekContainer.addView(item)
        }
    }

    override fun onDateSelected(date: LocalDate) {
        val tvMonthYear = findViewById<TextView>(R.id.tv_month_year)
        tvMonthYear.text = date.month.getDisplayName(TextStyle.FULL_STANDALONE, locale) + " " + date.year.toString()
        setWeekForDate(date)
    }
}
