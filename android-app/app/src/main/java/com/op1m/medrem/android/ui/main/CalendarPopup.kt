package com.op1m.medrem.android.ui.main

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import com.op1m.medrem.android.R
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import kotlin.math.max
import kotlin.math.min

class CalendarPopup(
    private val ctx: Context,
    year: Int,
    month: Int,
    private val initialSelected: LocalDate?,
    private val listener: CalendarDialogFragment.Listener
) {
    var onDismissListener: (() -> Unit)? = null
    private val MONTHS_NOMINATIVE = arrayOf(
        "Январь","Февраль","Март","Апрель","Май","Июнь",
        "Июль","Август","Сентябрь","Октябрь","Ноябрь","Декабрь"
    )
    private var currentMonth: YearMonth = initialSelected?.let { YearMonth.from(it) } ?: YearMonth.of(year, month)
    private var selectedDate: LocalDate? = initialSelected
    private val inflater = LayoutInflater.from(ctx)
    private val contentView: View = inflater.inflate(R.layout.popup_calendar, null)
    private val popup: PopupWindow
    private val colorNormal = Color.parseColor("#263238")
    private val colorMuted = Color.parseColor("#9AA7B2")
    private val today = LocalDate.now()

    init {
        popup = PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup.isOutsideTouchable = true
        popup.elevation = 12f

        val tvMonthYear = contentView.findViewById<TextView>(R.id.popup_month_year)
        val btnPrev = contentView.findViewById<ImageView>(R.id.btn_prev_month)
        val btnNext = contentView.findViewById<ImageView>(R.id.btn_next_month)
        val grid = contentView.findViewById<GridLayout>(R.id.grid_dates)

        fun updateHeader() {
            val monthName = MONTHS_NOMINATIVE[currentMonth.monthValue - 1]
            tvMonthYear.text = "$monthName, ${currentMonth.year}"
        }

        updateHeader()
        populateGrid(grid, currentMonth, 0)

        btnPrev.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            updateHeader()
            val tag = grid.tag as? Int
            if (tag != null && tag > 0) populateGrid(grid, currentMonth, tag) else populateGrid(grid, currentMonth, 0)
        }
        btnNext.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            updateHeader()
            val tag = grid.tag as? Int
            if (tag != null && tag > 0) populateGrid(grid, currentMonth, tag) else populateGrid(grid, currentMonth, 0)
        }
    }

    fun show(anchor: View) {
        contentView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        val dm: DisplayMetrics = ctx.resources.displayMetrics
        val screenW = dm.widthPixels
        val screenH = dm.heightPixels
        val marginDp = 12
        val marginPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginDp.toFloat(), dm).toInt()
        val maxWidth = screenW - marginPx * 2

        var popupWidth = (screenW * 0.35).toInt()
        popupWidth = min(popupWidth, maxWidth)
        val minWidthDp = 260
        val minWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp.toFloat(), dm).toInt()
        if (popupWidth < minWidthPx) popupWidth = min(maxWidth, minWidthPx)

        val header = contentView.findViewById<View>(R.id.header)
        val dayNames = contentView.findViewById<View>(R.id.day_names)
        val widthSpec = MeasureSpec.makeMeasureSpec(popupWidth, MeasureSpec.EXACTLY)
        header.measure(widthSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        dayNames.measure(widthSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        val headerH = header.measuredHeight
        val dayNamesH = dayNames.measuredHeight
        val usedByHeader = headerH + dayNamesH + contentView.paddingTop + contentView.paddingBottom

        val first = currentMonth.atDay(1)
        val startIndex = if (first.dayOfWeek.value == 7) 6 else first.dayOfWeek.value - 1
        val daysInMonth = currentMonth.lengthOfMonth()
        val neededCells = startIndex + daysInMonth
        val rows = if (neededCells > 35) 6 else 5

        val maxCellByWidth = popupWidth / 7
        val maxCellByScreen = max(1, (screenH - marginPx * 2 - usedByHeader) / rows)
        var cellSize = min(maxCellByWidth, maxCellByScreen)
        val minCellDp = 28
        val minCellPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minCellDp.toFloat(), dm).toInt()
        if (cellSize < minCellPx) cellSize = minCellPx

        val extraBottomDp = 12
        val extraBottomPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, extraBottomDp.toFloat(), dm).toInt()

        val popupHeight = usedByHeader + rows * cellSize + extraBottomPx
        popup.width = popupWidth
        popup.height = min(popupHeight, screenH - marginPx)

        contentView.setPadding(contentView.paddingLeft, contentView.paddingTop, contentView.paddingRight, extraBottomPx)

        val grid = contentView.findViewById<GridLayout>(R.id.grid_dates)
        populateGrid(grid, currentMonth, cellSize)
        grid.tag = cellSize

        val loc = IntArray(2)
        anchor.getLocationOnScreen(loc)
        val anchorX = loc[0]
        val anchorY = loc[1]
        val anchorW = anchor.width
        val anchorH = anchor.height
        var x = anchorX + anchorW - popup.width
        x = max(0, x)
        if (x + popup.width > screenW) x = screenW - popup.width
        var y = anchorY + anchorH
        if (y + popup.height > screenH) {
            y = anchorY - popup.height
            if (y < 0) y = 0
        }
        popup.showAtLocation(anchor.rootView, Gravity.NO_GRAVITY, x, y)
    }

    fun dismiss() {
        popup.dismiss()
        onDismissListener?.invoke()
    }

    private fun populateGrid(grid: GridLayout, ym: YearMonth, cellSize: Int) {
        grid.removeAllViews()
        grid.columnCount = 7
        val first = ym.atDay(1)
        val startIndex = if (first.dayOfWeek.value == 7) 6 else first.dayOfWeek.value - 1
        val daysInMonth = ym.lengthOfMonth()
        val prevMonth = ym.minusMonths(1)
        val nextMonth = ym.plusMonths(1)
        val daysPrev = prevMonth.lengthOfMonth()
        val totalCells = if (startIndex + daysInMonth > 35) 6 * 7 else 5 * 7

        for (i in 0 until totalCells) {
            val cell = inflater.inflate(R.layout.day_cell, grid, false)
            val tv = cell.findViewById<TextView>(R.id.tv_day_cell)
            val dayIndex = i - startIndex + 1
            val date: LocalDate
            val isFromPrev: Boolean
            val isFromNext: Boolean
            if (dayIndex <= 0) {
                val day = daysPrev + dayIndex
                date = prevMonth.atDay(day)
                isFromPrev = true
                isFromNext = false
            } else if (dayIndex > daysInMonth) {
                val day = dayIndex - daysInMonth
                date = nextMonth.atDay(day)
                isFromPrev = false
                isFromNext = true
            } else {
                date = ym.atDay(dayIndex)
                isFromPrev = false
                isFromNext = false
            }

            tv.text = date.dayOfMonth.toString()
            val isToday = date == today
            val isSelected = selectedDate != null && date == selectedDate

            if (isSelected) {
                tv.setBackgroundResource(R.drawable.day_bg_selected)
                tv.setTextColor(Color.WHITE)
            } else if (isToday) {
                tv.setBackgroundResource(R.drawable.day_bg_today_outline)
                tv.setTextColor(if (isFromPrev || isFromNext) colorMuted else colorNormal)
            } else {
                tv.setBackgroundResource(R.drawable.day_bg_default)
                tv.setTextColor(if (isFromPrev || isFromNext) colorMuted else colorNormal)
            }

            tv.setOnClickListener {
                selectedDate = date
                listener.onDateSelected(date)
                popup.dismiss()
            }

            val params = GridLayout.LayoutParams()
            if (cellSize > 0) {
                params.width = cellSize
                params.height = cellSize
            } else {
                params.width = 0
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            cell.layoutParams = params
            grid.addView(cell)
        }
        grid.rowCount = if (totalCells == 42) 6 else 5
    }

}
