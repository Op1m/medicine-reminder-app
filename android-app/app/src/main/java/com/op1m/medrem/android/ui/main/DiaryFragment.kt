package com.op1m.medrem.android.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.RotateAnimation
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.op1m.medrem.android.R
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.TemporalAdjusters
import java.util.*
import kotlin.collections.LinkedHashMap

class DiaryFragment : Fragment(), CalendarDialogFragment.Listener {

    private lateinit var tvMonthYear: TextView
    private lateinit var ivMonthArrow: ImageView
    private lateinit var hsvWeek: HorizontalScrollView
    private lateinit var weekContainer: LinearLayout

    private lateinit var scheduleScroll: NestedScrollView
    private lateinit var scheduleContainer: LinearLayout
    private lateinit var emptyOverlay: LinearLayout
    private lateinit var btnAddInside: Button
    private var btnAddEmpty: Button? = null

    private var selectedDate: LocalDate = LocalDate.now()
    private val locale = Locale("ru")

    private val groups: LinkedHashMap<String, MutableList<Medicine>> = LinkedHashMap()
    private var nextId = 1

    data class Medicine(val id: Int, val name: String, var accepted: Boolean = false)

    companion object {
        fun newInstance(): DiaryFragment = DiaryFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvMonthYear = view.findViewById(R.id.tv_month_year)
        ivMonthArrow = view.findViewById(R.id.iv_month_arrow)
        hsvWeek = view.findViewById(R.id.hsv_week)
        weekContainer = view.findViewById(R.id.week_container)

        scheduleScroll = view.findViewById(R.id.schedule_scroll)
        scheduleContainer = view.findViewById(R.id.schedule_container)
        emptyOverlay = view.findViewById(R.id.empty_state_overlay)
        btnAddInside = view.findViewById(R.id.btn_add_prep_inside_scroll)
        btnAddEmpty = view.findViewById<View?>(R.id.btn_add_prep_empty) as? Button

        btnAddInside.isAllCaps = false
        btnAddEmpty?.isAllCaps = false

        btnAddInside.setOnClickListener { addMedicineToDefaultTime() }
        btnAddEmpty?.setOnClickListener { addMedicineToDefaultTime() }

        activity?.findViewById<View?>(R.id.btn_right_plus)?.setOnClickListener { addMedicineToDefaultTime() }

        tvMonthYear.setOnClickListener { showCalendar() }
        ivMonthArrow.setOnClickListener { showCalendar() }

        updateMonthYearText(selectedDate)
        setWeekForDate(selectedDate)
        refreshUI()
    }

    private fun showCalendar() {
        val popup = CalendarPopup(requireContext(), selectedDate.year, selectedDate.monthValue, selectedDate, object : CalendarDialogFragment.Listener {
            override fun onDateSelected(date: LocalDate) {
                selectedDate = date
                updateMonthYearText(date)
                setWeekForDate(date)
                ivMonthArrow.rotation = 0f
            }
        })

        ivMonthArrow.rotation = 180f
        popup.show(tvMonthYear)
    }

    private fun updateMonthYearText(date: LocalDate) {
        val monthName = date.month.getDisplayName(TextStyle.FULL_STANDALONE, locale).replaceFirstChar { it.uppercase(locale) }
        tvMonthYear.text = "$monthName, ${date.year}:"
    }

    private fun setWeekForDate(date: LocalDate) {
        weekContainer.removeAllViews()
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val inflater = LayoutInflater.from(requireContext())

        for (i in 0..6) {
            val d = monday.plusDays(i.toLong())
            val item = inflater.inflate(R.layout.item_week_day, weekContainer, false)
            val tvNum = item.findViewById<TextView>(R.id.tv_day_num)
            val tvAbbr = item.findViewById<TextView>(R.id.tv_day_abbr)
            tvNum.text = d.dayOfMonth.toString()
            tvAbbr.text = d.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).lowercase(locale)

            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            item.layoutParams = lp

            when {
                d == selectedDate -> {
                    tvNum.setBackgroundResource(R.drawable.day_bg_selected)
                    tvNum.setTextColor(Color.WHITE)
                }
                d == LocalDate.now() -> {
                    tvNum.setBackgroundResource(R.drawable.day_bg_today_outline)
                    tvNum.setTextColor(resources.getColor(R.color.design_default_color_primary_dark, null))
                }
                else -> {
                    tvNum.setBackgroundResource(R.drawable.week_day_bg_selector)
                    tvNum.setTextColor(resources.getColor(R.color.design_default_color_primary_dark, null))
                }
            }

            item.setOnClickListener {
                selectedDate = d
                setWeekForDate(d)
            }

            weekContainer.addView(item)
        }
        hsvWeek.post { hsvWeek.fullScroll(HorizontalScrollView.FOCUS_LEFT) }
    }

    private fun addMedicineToDefaultTime() {
        val time = "08:30"
        val m = Medicine(nextId, "Препарат $nextId")
        nextId++
        val list = groups.getOrPut(time) { mutableListOf() }
        list.add(m)
        refreshUI(scrollToBottom = true, expandTime = time)
    }

    private fun refreshUI(scrollToBottom: Boolean = false, expandTime: String? = null) {
        val hasMeds = groups.values.any { it.isNotEmpty() }
        emptyOverlay.isVisible = !hasMeds
        scheduleScroll.isVisible = hasMeds

        if (!hasMeds) {
            btnAddInside.visibility = View.GONE
            btnAddEmpty?.visibility = View.VISIBLE
            scheduleContainer.removeAllViews()
            return
        } else {
            btnAddInside.visibility = View.VISIBLE
            btnAddEmpty?.visibility = View.GONE
        }

        scheduleContainer.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        for ((time, list) in groups) {
            if (list.isEmpty()) continue

            val header = inflater.inflate(R.layout.simple_time_header, scheduleContainer, false)
            val timeText = header.findViewById<TextView>(R.id.header_time_text)
            val btnAcceptAll = header.findViewById<Button>(R.id.header_accept_all)
            val headerArrow = header.findViewById<ImageView>(R.id.header_arrow)

            timeText.text = time
            btnAcceptAll.isAllCaps = false
            btnAcceptAll.setOnClickListener {
                list.forEach { it.accepted = true }
                refreshUI()
            }

            val medsContainer = LinearLayout(requireContext())
            medsContainer.orientation = LinearLayout.VERTICAL
            medsContainer.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            medsContainer.setPadding(6, 6, 6, 6)

            for (med in list) {
                val medView = inflater.inflate(R.layout.item_med, medsContainer, false)
                val name = medView.findViewById<TextView>(R.id.med_name)
                val desc = medView.findViewById<TextView>(R.id.med_desc)
                val status = medView.findViewById<TextView>(R.id.med_status)
                name.text = med.name
                desc.text = "1 таб. во время еды"
                status.isVisible = med.accepted
                medView.setOnClickListener {
                    med.accepted = !med.accepted
                    status.isVisible = med.accepted
                }
                medsContainer.addView(medView)
            }

            medsContainer.visibility = if (expandTime != null && expandTime == time) View.VISIBLE else View.GONE
            headerArrow.rotation = if (medsContainer.visibility == View.VISIBLE) 0f else 180f

            header.setOnClickListener {
                val visibleNow = medsContainer.visibility == View.VISIBLE
                medsContainer.visibility = if (visibleNow) View.GONE else View.VISIBLE
                val from = if (visibleNow) 0f else 180f
                val to = if (visibleNow) 180f else 0f
                val rot = RotateAnimation(from, to, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f)
                rot.duration = 200
                rot.fillAfter = true
                headerArrow.startAnimation(rot)
            }

            scheduleContainer.addView(header)
            scheduleContainer.addView(medsContainer)
        }

        if (btnAddInside.parent != scheduleContainer) {
            (btnAddInside.parent as? ViewGroup)?.removeView(btnAddInside)
            val btnLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (resources.displayMetrics.density * 44).toInt())
            btnLp.topMargin = (resources.displayMetrics.density * 6).toInt()
            btnAddInside.layoutParams = btnLp
            scheduleContainer.addView(btnAddInside)
        }

        if (scrollToBottom) {
            scheduleScroll.post { scheduleScroll.fullScroll(View.FOCUS_DOWN) }
        }
    }

    override fun onDateSelected(date: LocalDate) {
        selectedDate = date
        updateMonthYearText(date)
        setWeekForDate(date)
    }
}
