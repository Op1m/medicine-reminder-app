package com.op1m.medrem.android.ui.main

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.RotateAnimation
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
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
import kotlin.jvm.functions.Function0

class DiaryFragment : Fragment(), CalendarDialogFragment.Listener {

    private lateinit var tvMonthYear: TextView
    private lateinit var ivMonthArrow: ImageView
    private lateinit var hsvWeek: HorizontalScrollView
    private lateinit var weekContainer: LinearLayout
    private lateinit var scheduleScroll: NestedScrollView
    private lateinit var scheduleContainer: LinearLayout
    private lateinit var emptyOverlay: LinearLayout
    private lateinit var btnAddInside: Button
    private lateinit var flFloatingAddBtn: FrameLayout

    private var selectedDate: LocalDate = LocalDate.now()
    private val locale = Locale("ru")

    enum class MedStatus { PENDING, ACCEPTED, MISSED }
    data class Medicine(val id: Int, val name: String, var status: MedStatus = MedStatus.PENDING)

    private val groups: LinkedHashMap<String, MutableList<Medicine>> = LinkedHashMap()
    private var nextId = 1

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
        flFloatingAddBtn = view.findViewById(R.id.fl_floating_add_btn)

        hsvWeek.isFillViewport = true
        hsvWeek.setPadding(0, 0, 0, 0)
        hsvWeek.clipToPadding = false
        hsvWeek.layoutDirection = View.LAYOUT_DIRECTION_LTR

        weekContainer.setPadding(0, 0, 0, 0)
        val wcLp = weekContainer.layoutParams
        if (wcLp is ViewGroup.LayoutParams) {
            wcLp.width = ViewGroup.LayoutParams.MATCH_PARENT
            weekContainer.layoutParams = wcLp
        }

        btnAddInside.isAllCaps = false
        btnAddInside.transformationMethod = null
        btnAddInside.minWidth = dpToPx(200)
        btnAddInside.setPadding(dpToPx(16), 0, dpToPx(16), 0)

        btnAddInside.setOnClickListener { addMedicineToDefaultTime() }
        activity?.findViewById<View?>(R.id.btn_right_plus)?.setOnClickListener { addMedicineToDefaultTime() }

        tvMonthYear.setOnClickListener { showCalendarPopup() }
        ivMonthArrow.setOnClickListener { showCalendarPopup() }

        updateMonthYearText(selectedDate)
        setWeekForDate(selectedDate)
        placeButtonFloating()
        refreshUI()
    }


    private fun showCalendarPopup() {
        val popup = CalendarPopup(
            requireContext(),
            selectedDate.year,
            selectedDate.monthValue,
            selectedDate,
            object : CalendarDialogFragment.Listener {
                override fun onDateSelected(date: LocalDate) {
                    selectedDate = date
                    updateMonthYearText(date)
                    setWeekForDate(date)
                    ivMonthArrow.animate().rotation(0f).setDuration(200).start()
                }
            }
        )
        ivMonthArrow.animate().rotation(180f).setDuration(200).start()
        try {
            val field = popup.javaClass.getDeclaredField("onDismissListener")
            field.isAccessible = true
            val listenerObj = object : Function0<Unit> {
                override fun invoke() {
                    ivMonthArrow.animate().rotation(0f).setDuration(200).start()
                }
            }
            field.set(popup, listenerObj)
        } catch (ignored: Exception) {
        }
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
            lp.width = 0
            lp.weight = 1f
            item.layoutParams = lp

            when {
                d == selectedDate -> {
                    tvNum.setBackgroundResource(R.drawable.day_bg_selected)
                    tvNum.setTextColor(android.graphics.Color.WHITE)
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
                updateMonthYearText(selectedDate)
                setWeekForDate(selectedDate)
            }

            weekContainer.addView(item)
        }

        hsvWeek.post {
            hsvWeek.fullScroll(HorizontalScrollView.FOCUS_LEFT)
        }
    }

    private fun addMedicineToDefaultTime() {
        val time = "08:30"
        val med = Medicine(nextId, "Препарат $nextId", MedStatus.PENDING)
        nextId++
        val list = groups.getOrPut(time) { mutableListOf() }
        list.add(med)
        refreshUI(scrollToBottom = true, expandTime = time)
    }

    private fun dpToPx(dp: Int): Int {
        return (resources.displayMetrics.density * dp).toInt()
    }

    private fun placeButtonFloating() {
        val parent = btnAddInside.parent
        if (parent !== flFloatingAddBtn) {
            (parent as? ViewGroup)?.removeView(btnAddInside)
            val lp = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dpToPx(44)
            )
            lp.marginStart = dpToPx(24)
            lp.marginEnd = dpToPx(24)
            lp.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            lp.bottomMargin = dpToPx(8)
            btnAddInside.layoutParams = lp
            flFloatingAddBtn.addView(btnAddInside)
            flFloatingAddBtn.visibility = View.VISIBLE
            btnAddInside.isAllCaps = false
            btnAddInside.transformationMethod = null
        }
    }

    private fun placeButtonInScroll() {
        val parent = btnAddInside.parent
        if (parent !== scheduleContainer) {
            (parent as? ViewGroup)?.removeView(btnAddInside)
            val btnLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(44))
            btnLp.topMargin = dpToPx(8)
            btnLp.marginStart = dpToPx(24)
            btnLp.marginEnd = dpToPx(24)
            btnLp.gravity = Gravity.CENTER_HORIZONTAL
            btnAddInside.layoutParams = btnLp
            scheduleContainer.addView(btnAddInside)
            flFloatingAddBtn.visibility = View.GONE
            btnAddInside.isAllCaps = false
            btnAddInside.transformationMethod = null
        }
    }

    private fun refreshUI(scrollToBottom: Boolean = false, expandTime: String? = null) {
        val hasMeds = groups.values.any { it.isNotEmpty() }
        emptyOverlay.isVisible = !hasMeds
        scheduleScroll.isVisible = hasMeds

        if (!hasMeds) {
            btnAddInside.visibility = View.VISIBLE
            scheduleContainer.removeAllViews()
            placeButtonFloating()
            return
        } else {
            btnAddInside.visibility = View.VISIBLE
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
                list.forEach { it.status = MedStatus.ACCEPTED }
                refreshUI()
            }
            val medsContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(6, 6, 6, 6)
            }
            for (med in list) {
                val medView = inflater.inflate(R.layout.item_med, medsContainer, false)
                val medCard = medView.findViewById<LinearLayout>(R.id.med_card)
                val medIcon = medView.findViewById<ImageView>(R.id.med_icon)
                val medName = medView.findViewById<TextView>(R.id.med_name)
                val medDesc = medView.findViewById<TextView>(R.id.med_desc)
                val medStatus = medView.findViewById<TextView>(R.id.med_status)
                medName.text = med.name
                medDesc.text = "1 таб. во время еды"
                when (med.status) {
                    MedStatus.ACCEPTED -> {
                        medCard.isSelected = true
                        medCard.isActivated = false
                        medStatus.text = "Принято в $time, сегодня"
                        medStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_green, 0)
                    }
                    MedStatus.MISSED -> {
                        medCard.isSelected = false
                        medCard.isActivated = true
                        medStatus.text = "Пропущено"
                        medStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_cross_gray, 0)
                    }
                    MedStatus.PENDING -> {
                        medCard.isSelected = false
                        medCard.isActivated = false
                        medStatus.text = "Ожидается"
                        medStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clock, 0)
                    }
                }
                medStatus.compoundDrawablePadding = (resources.displayMetrics.density * 8).toInt()
                medStatus.isVisible = true
                ContextCompat.getColorStateList(requireContext(), R.color.med_icon_tint)?.let { medIcon.imageTintList = it }
                ContextCompat.getColorStateList(requireContext(), R.color.med_name_color)?.let { medName.setTextColor(it) }
                ContextCompat.getColorStateList(requireContext(), R.color.med_status_color)?.let { medStatus.setTextColor(it) }
                medView.setOnClickListener {
                    med.status = when (med.status) {
                        MedStatus.PENDING -> MedStatus.ACCEPTED
                        MedStatus.ACCEPTED -> MedStatus.MISSED
                        MedStatus.MISSED -> MedStatus.PENDING
                    }
                    when (med.status) {
                        MedStatus.ACCEPTED -> {
                            medCard.isSelected = true
                            medCard.isActivated = false
                            medStatus.text = "Принято в $time, сегодня"
                            medStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_green, 0)
                        }
                        MedStatus.MISSED -> {
                            medCard.isSelected = false
                            medCard.isActivated = true
                            medStatus.text = "Пропущено"
                            medStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_cross_gray, 0)
                        }
                        MedStatus.PENDING -> {
                            medCard.isSelected = false
                            medCard.isActivated = false
                            medStatus.text = "Ожидается"
                            medStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clock, 0)
                        }
                    }
                    medStatus.compoundDrawablePadding = (resources.displayMetrics.density * 8).toInt()
                }
                medView.setOnLongClickListener {
                    med.status = MedStatus.MISSED
                    medCard.isSelected = false
                    medCard.isActivated = true
                    medStatus.text = "Пропущено"
                    medStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_cross_gray, 0)
                    medStatus.compoundDrawablePadding = (resources.displayMetrics.density * 8).toInt()
                    true
                }
                medsContainer.addView(medView)
            }
            val startVisible = expandTime != null && expandTime == time
            medsContainer.visibility = if (startVisible) View.VISIBLE else View.GONE
            headerArrow.rotation = if (medsContainer.visibility == View.VISIBLE) 180f else 0f
            header.setOnClickListener {
                val visibleNow = medsContainer.visibility == View.VISIBLE
                medsContainer.visibility = if (visibleNow) View.GONE else View.VISIBLE
                val from = if (visibleNow) 180f else 0f
                val to = if (visibleNow) 0f else 180f
                val rot = RotateAnimation(from, to, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f)
                rot.duration = 200
                rot.fillAfter = true
                headerArrow.startAnimation(rot)
            }
            scheduleContainer.addView(header)
            scheduleContainer.addView(medsContainer)
        }

        val btnH = dpToPx(44)
        val bottomNavReserve = dpToPx(56)
        val spacer = View(requireContext())
        spacer.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(12))
        scheduleContainer.addView(spacer)

        scheduleScroll.post {
            val contentH = scheduleContainer.measuredHeight
            val visibleH = scheduleScroll.height
            if (contentH > visibleH - bottomNavReserve - dpToPx(8)) {
                if (btnAddInside.parent !== scheduleContainer) {
                    (btnAddInside.parent as? ViewGroup)?.removeView(btnAddInside)
                    val btnLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, btnH)
                    btnLp.topMargin = dpToPx(8)
                    btnLp.marginStart = dpToPx(24)
                    btnLp.marginEnd = dpToPx(24)
                    btnLp.gravity = Gravity.CENTER_HORIZONTAL
                    btnAddInside.layoutParams = btnLp
                    scheduleContainer.addView(btnAddInside)
                    val after = View(requireContext())
                    after.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(12))
                    scheduleContainer.addView(after)
                }
                if (scrollToBottom) scheduleScroll.post { scheduleScroll.fullScroll(View.FOCUS_DOWN) }
            } else {
                placeButtonFloating()
                if (scrollToBottom) scheduleScroll.post { scheduleScroll.fullScroll(View.FOCUS_DOWN) }
            }
        }
    }

    override fun onDateSelected(date: LocalDate) {
        selectedDate = date
        updateMonthYearText(date)
        setWeekForDate(date)
    }
}
