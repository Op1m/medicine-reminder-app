package com.op1m.medrem.android.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.op1m.medrem.android.R
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.TemporalAdjusters
import java.util.Locale

class DiaryFragment : Fragment(), CalendarDialogFragment.Listener {

    private lateinit var weekContainer: LinearLayout
    private lateinit var hsvWeek: HorizontalScrollView
    private val locale = Locale("ru")
    private var selectedDate: LocalDate? = null
    private val today: LocalDate = LocalDate.now()

    companion object {
        fun newInstance(): DiaryFragment = DiaryFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvMonthYear = view.findViewById<TextView>(R.id.tv_month_year)
        val ivArrow = view.findViewById<ImageView>(R.id.iv_month_arrow)
        hsvWeek = view.findViewById(R.id.hsv_week)
        weekContainer = view.findViewById(R.id.week_container)

        if (savedInstanceState != null) {
            val s = savedInstanceState.getString("selected_date")
            if (s != null) selectedDate = LocalDate.parse(s)
        } else {
            selectedDate = today
        }

        val todayDate = LocalDate.now()
        setWeekForDate(todayDate)

        val rawInit = MONTHS_NOMINATIVE[todayDate.monthValue - 1]
        tvMonthYear.text = "$rawInit, ${todayDate.year}:"

        val openCalendar = View.OnClickListener {
            val anchor = ivArrow
            val popup = CalendarPopup(requireContext(), YearMonth.now().year, YearMonth.now().monthValue, selectedDate, object : CalendarDialogFragment.Listener {
                override fun onDateSelected(date: LocalDate) {
                    selectedDate = date
                    val raw = MONTHS_NOMINATIVE[date.monthValue - 1]
                    tvMonthYear.text = "$raw, ${date.year}"
                    setWeekForDate(date)
                }
            })
            popup.show(anchor)
        }
        view.findViewById<View>(R.id.tv_month_year).setOnClickListener(openCalendar)
        ivArrow.setOnClickListener(openCalendar)
    }

    private fun setWeekForDate(date: LocalDate) {
        weekContainer.removeAllViews()
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val inflater = LayoutInflater.from(requireContext())
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
        hsvWeek.post { hsvWeek.fullScroll(HorizontalScrollView.FOCUS_LEFT) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedDate?.let { outState.putString("selected_date", it.toString()) }
    }

    override fun onDateSelected(date: LocalDate) {
        selectedDate = date
        setWeekForDate(date)
    }

    private val MONTHS_NOMINATIVE = arrayOf(
        "Январь","Февраль","Март","Апрель","Май","Июнь",
        "Июль","Август","Сентябрь","Октябрь","Ноябрь","Декабрь"
    )
}
