package com.op1m.medrem.android.ui.main

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.op1m.medrem.android.R
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.collections.LinkedHashMap

class DiaryFragment : Fragment(),
    CalendarDialogFragment.Listener,
    ConfirmOptionsSheet.Listener,
    ConfirmTimeSheet.Listener,
    AddOptionsSheet.Listener,
    PillBottomSheet.Listener {

    private lateinit var tvMonthYear: TextView
    private lateinit var ivMonthArrow: ImageView
    private lateinit var hsvWeek: HorizontalScrollView
    private lateinit var weekContainer: LinearLayout
    private lateinit var scheduleScroll: NestedScrollView
    private lateinit var scheduleContainer: LinearLayout
    private lateinit var emptyOverlay: LinearLayout
    private lateinit var btnAddInside: Button
    private lateinit var flFloatingAddBtn: FrameLayout
    private var bottomSpacer: View? = null

    private var selectedDate: LocalDate = LocalDate.now()
    private val locale = Locale("ru")
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    enum class MedStatus { PENDING, ACCEPTED, MISSED }
    data class Medicine(val id: Int, val name: String, var status: MedStatus = MedStatus.PENDING, var acceptedAt: String? = null)

    private var groups: LinkedHashMap<String, MutableList<Medicine>> = LinkedHashMap()
    private var nextId = 1
    private val expandedTimes: MutableSet<String> = mutableSetOf()
    private val pendingActionExpanded: MutableMap<String, Boolean> = mutableMapOf()
    private var desiredBtnWidth: Int = 0

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
        hsvWeek.clipToPadding = false
        hsvWeek.setPadding(0, 0, 0, 0)

        btnAddInside.isAllCaps = false
        btnAddInside.transformationMethod = null
        btnAddInside.minWidth = dpToPx(200)
        btnAddInside.setPadding(dpToPx(16), 0, dpToPx(16), 0)
        btnAddInside.setOnClickListener { addMedicineToDefaultTime() }

        activity?.findViewById<View?>(R.id.btn_right_plus)?.setOnClickListener {
            val sheet = AddOptionsSheet()
            sheet.setListener(this)
            sheet.show(childFragmentManager, "add_opts")
        }

        tvMonthYear.setOnClickListener { showCalendarPopup() }
        ivMonthArrow.setOnClickListener { showCalendarPopup() }

        desiredBtnWidth = resources.displayMetrics.widthPixels - dpToPx(70) * 2

        updateMonthYearText(selectedDate)
        setWeekForDate(selectedDate)
        placeButtonFloating()
        refreshUI()
    }

    private fun showCalendarPopup() {
        val popup = CalendarPopup(requireContext(), selectedDate.year, selectedDate.monthValue, selectedDate, object : CalendarDialogFragment.Listener {
            override fun onDateSelected(date: LocalDate) {
                selectedDate = date
                updateMonthYearText(date)
                setWeekForDate(date)
                ivMonthArrow.animate().rotation(0f).setDuration(200).start()
            }
        })
        ivMonthArrow.animate().rotation(180f).setDuration(200).start()
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
                refreshUI()
            }
            weekContainer.addView(item)
        }
        hsvWeek.post { hsvWeek.fullScroll(HorizontalScrollView.FOCUS_LEFT) }
    }

    private fun addMedicineToDefaultTime() {
        val time = "08:30"
        val med = Medicine(nextId, "Препарат $nextId", MedStatus.PENDING, null)
        nextId++
        val list = groups.getOrPut(time) { mutableListOf() }
        list.add(med)
        reorderGroups()
        expandedTimes.add(time)
        refreshUI(scrollToBottom = true, expandTime = time)
    }

    private fun dpToPx(dp: Int): Int {
        return (resources.displayMetrics.density * dp).toInt()
    }

    private fun placeButtonFloating() {
        val parent = btnAddInside.parent
        if (parent !== flFloatingAddBtn) {
            (parent as? ViewGroup)?.removeView(btnAddInside)
            val lp = FrameLayout.LayoutParams(desiredBtnWidth, dpToPx(44))
            lp.marginStart = dpToPx(70)
            lp.marginEnd = dpToPx(70)
            lp.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            lp.bottomMargin = dpToPx(16)
            btnAddInside.layoutParams = lp
            flFloatingAddBtn.addView(btnAddInside)
            flFloatingAddBtn.visibility = View.VISIBLE
            btnAddInside.isAllCaps = false
            btnAddInside.transformationMethod = null
        }
    }

    private fun placeButtonInScrollAfterHeaders() {
        val parent = btnAddInside.parent
        if (parent === scheduleContainer) return
        (parent as? ViewGroup)?.removeView(btnAddInside)
        val wrapper = FrameLayout(requireContext())
        wrapper.tag = "BTN_WRAPPER"
        val wrapLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        wrapLp.topMargin = dpToPx(8)
        wrapper.layoutParams = wrapLp
        val innerLp = FrameLayout.LayoutParams(desiredBtnWidth, dpToPx(44))
        innerLp.gravity = Gravity.CENTER_HORIZONTAL
        btnAddInside.layoutParams = innerLp
        wrapper.addView(btnAddInside)
        var insertIndex = -1
        for (i in scheduleContainer.childCount - 1 downTo 0) {
            val child = scheduleContainer.getChildAt(i)
            val tag = child.tag
            if (tag == "HEADER_WRAPPER") {
                insertIndex = i + 1
                break
            }
        }
        val spacer = bottomSpacer ?: createBottomSpacer()
        if (insertIndex >= 0) {
            scheduleContainer.addView(wrapper, insertIndex)
        } else {
            scheduleContainer.addView(wrapper)
        }
        if (scheduleContainer.indexOfChild(spacer) == -1) scheduleContainer.addView(spacer)
        flFloatingAddBtn.visibility = View.GONE
        btnAddInside.isAllCaps = false
        btnAddInside.transformationMethod = null
    }

    private fun createBottomSpacer(): View {
        val v = View(requireContext())
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(80))
        v.layoutParams = lp
        bottomSpacer = v
        return v
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
        val times = groups.keys.toList()
        for (time in times) {
            val list = groups[time] ?: continue
            if (list.isEmpty()) continue

            val wrapper = LinearLayout(requireContext())
            wrapper.orientation = LinearLayout.VERTICAL
            wrapper.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            wrapper.tag = "HEADER_WRAPPER"

            val header = inflater.inflate(R.layout.simple_time_header, wrapper, false)
            header.tag = "HEADER"
            val timeText = header.findViewById<TextView>(R.id.header_time_text)
            val btnAcceptAll = header.findViewById<Button>(R.id.header_accept_all)
            val headerArrow = header.findViewById<ImageView>(R.id.header_arrow)
            timeText.text = time
            btnAcceptAll.isAllCaps = false

            val wasExpandedBeforeAction = expandedTimes.contains(time)

            btnAcceptAll.setOnClickListener {
                pendingActionExpanded[time] = wasExpandedBeforeAction
                val sheet = ConfirmOptionsSheet.newInstance(list.size)
                sheet.setListener(this)
                sheet.arguments = (sheet.arguments ?: Bundle()).apply { putString("time", time) }
                sheet.show(childFragmentManager, "confirm_opts_$time")
            }

            val medsContainer = LinearLayout(requireContext())
            medsContainer.orientation = LinearLayout.VERTICAL
            medsContainer.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            medsContainer.setPadding(6, 6, 6, 6)
            medsContainer.tag = "MEDS_CONTAINER_$time"

            for (med in list) {
                val medView = inflater.inflate(R.layout.item_med, medsContainer, false)
                val medCard = medView.findViewById<LinearLayout>(R.id.med_card)
                val medStatus = medView.findViewById<TextView>(R.id.med_status)
                val medDesc = medView.findViewById<TextView>(R.id.med_desc)
                medView.findViewById<TextView>(R.id.med_name).text = med.name
                medDesc.text = "1 таб. во время еды"
                updateMedViewVisual(med, medCard, medStatus)
                medView.tag = med.id
                medView.setOnClickListener {
                    val sheet = PillBottomSheet.newInstance(med.id, med.name, time, med.status.name, med.acceptedAt)
                    sheet.setListener(this)
                    sheet.show(childFragmentManager, "pill_${med.id}")
                }
                medView.setOnLongClickListener {
                    med.status = MedStatus.MISSED
                    refreshUI(scrollToBottom = false, expandTime = time)
                    true
                }
                medsContainer.addView(medView)
            }

            val startVisible = (expandTime != null && expandTime == time) || expandedTimes.contains(time)
            medsContainer.visibility = if (startVisible) View.VISIBLE else View.GONE
            headerArrow.rotation = if (medsContainer.visibility == View.VISIBLE) 180f else 0f

            header.setOnClickListener {
                val visibleNow = medsContainer.visibility == View.VISIBLE
                if (visibleNow) {
                    medsContainer.visibility = View.GONE
                    expandedTimes.remove(time)
                } else {
                    medsContainer.visibility = View.VISIBLE
                    expandedTimes.add(time)
                }
                scheduleScroll.post { evaluateButtonPlacement() }
                val to = if (medsContainer.visibility == View.VISIBLE) 180f else 0f
                headerArrow.animate().rotation(to).setDuration(200).start()
            }

            wrapper.addView(header)
            wrapper.addView(medsContainer)
            scheduleContainer.addView(wrapper)
        }

        val spacer = bottomSpacer ?: createBottomSpacer()
        if (scheduleContainer.indexOfChild(spacer) == -1) scheduleContainer.addView(spacer)

        scheduleScroll.post {
            evaluateButtonPlacement()
            if (scrollToBottom) scheduleScroll.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun evaluateButtonPlacement() {
        if (groups.isEmpty()) {
            placeButtonFloating()
            return
        }
        placeButtonInScrollAfterHeaders()
    }

    private fun updateMedViewVisual(med: Medicine, medCard: LinearLayout, medStatus: TextView) {
        when (med.status) {
            MedStatus.ACCEPTED -> {
                medCard.isSelected = true
                medCard.isActivated = false
                val timeText = med.acceptedAt ?: ""
                medStatus.text = if (timeText.isNotEmpty()) "Принято в $timeText, сегодня" else "Принято"
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
        medStatus.compoundDrawablePadding = dpToPx(8)
        medStatus.isVisible = true
    }

    override fun onDateSelected(date: LocalDate) {
        selectedDate = date
        updateMonthYearText(date)
        setWeekForDate(date)
        refreshUI()
    }

    override fun onAcceptAllRequested(time: String) {
        val list = groups[time] ?: return
        pendingActionExpanded[time] = expandedTimes.contains(time)
        val sheet = ConfirmTimeSheet.newInstance(list.size)
        sheet.setListener(this)
        sheet.arguments = (sheet.arguments ?: Bundle()).apply { putString("time", time) }
        sheet.show(childFragmentManager, "confirm_time_$time")
    }

    override fun onSkipAllRequested(time: String) {
        val list = groups[time] ?: return
        list.forEach {
            it.status = MedStatus.MISSED
            it.acceptedAt = null
        }
        val prev = pendingActionExpanded.remove(time)
        if (prev != null) {
            if (prev) expandedTimes.add(time) else expandedTimes.remove(time)
        }
        refreshUI(expandTime = time)
    }

    override fun onMoveAllRequested(time: String) {
        val list = groups.remove(time) ?: return
        val dest = "09:00"
        val destList = groups.getOrPut(dest) { mutableListOf() }
        destList.addAll(list)
        reorderGroups()
        val prev = pendingActionExpanded.remove(time)
        if (prev == true) expandedTimes.add(dest)
        expandedTimes.remove(time)
        refreshUI(scrollToBottom = true, expandTime = dest)
    }

    override fun onConfirmNow(time: String) {
        val list = groups[time] ?: return
        val now = LocalTime.now().format(timeFormatter)
        list.forEach {
            it.status = MedStatus.ACCEPTED
            it.acceptedAt = now
        }
        val prev = pendingActionExpanded.remove(time)
        if (prev != null) {
            if (prev) expandedTimes.add(time) else expandedTimes.remove(time)
        }
        refreshUI(expandTime = time)
    }

    override fun onConfirmBySchedule(time: String) {
        val list = groups[time] ?: return
        list.forEach {
            it.status = MedStatus.ACCEPTED
            it.acceptedAt = time
        }
        val prev = pendingActionExpanded.remove(time)
        if (prev != null) {
            if (prev) expandedTimes.add(time) else expandedTimes.remove(time)
        }
        refreshUI(expandTime = time)
    }

    override fun onConfirmTimeCustom(time: String, chosen: String) {
        val list = groups[time] ?: return
        list.forEach {
            it.status = MedStatus.ACCEPTED
            it.acceptedAt = chosen
        }
        val prev = pendingActionExpanded.remove(time)
        if (prev != null) {
            if (prev) expandedTimes.add(time) else expandedTimes.remove(time)
        }
        refreshUI(expandTime = time)
    }

    override fun onNewMedicineRequested() {
        val sheet = SimpleInfoSheet.newInstance("Новый препарат", "Здесь будет форма добавления препарата")
        sheet.show(childFragmentManager, "new_med")
    }

    override fun onNewCourseRequested() {
        val sheet = SimpleInfoSheet.newInstance("Новый курс", "Здесь будет форма добавления курса")
        sheet.show(childFragmentManager, "new_course")
    }

    override fun onCancelAddOptions() {
    }

    override fun onPillAccept(medId: Int, time: String) {
        val list = groups[time] ?: return
        val med = list.find { it.id == medId } ?: return
        med.status = MedStatus.ACCEPTED
        med.acceptedAt = LocalTime.now().format(timeFormatter)
        refreshUI(expandTime = time)
    }

    override fun onPillSkip(medId: Int, time: String) {
        val list = groups[time] ?: return
        val med = list.find { it.id == medId } ?: return
        med.status = MedStatus.MISSED
        med.acceptedAt = null
        refreshUI(expandTime = time)
    }

    override fun onPillDelete(medId: Int, time: String) {
        val list = groups[time] ?: return
        val removed = list.removeAll { it.id == medId }
        if (list.isEmpty()) {
            groups.remove(time)
            expandedTimes.remove(time)
        }
        if (removed) refreshUI()
    }

    override fun onPillMove(medId: Int, time: String) {
        val src = groups[time] ?: return
        val med = src.find { it.id == medId } ?: return
        src.remove(med)
        if (src.isEmpty()) {
            groups.remove(time)
            expandedTimes.remove(time)
        }
        val dest = "09:00"
        val destList = groups.getOrPut(dest) { mutableListOf() }
        destList.add(med)
        reorderGroups()
        expandedTimes.add(dest)
        refreshUI(scrollToBottom = true, expandTime = dest)
    }

    override fun onPillBack() {
    }

    private fun reorderGroups() {
        try {
            val entries = groups.entries.toList().sortedWith(compareBy { entry ->
                try {
                    LocalTime.parse(entry.key)
                } catch (e: DateTimeParseException) {
                    LocalTime.MIDNIGHT
                }
            })
            val newMap = LinkedHashMap<String, MutableList<Medicine>>()
            for (e in entries) newMap[e.key] = e.value
            groups = newMap
        } catch (e: Exception) {
        }
    }
}
