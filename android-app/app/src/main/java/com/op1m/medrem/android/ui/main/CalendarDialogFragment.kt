package com.op1m.medrem.android.ui.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.op1m.medrem.android.R
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.TextStyle
import java.util.Locale

class CalendarDialogFragment : DialogFragment() {

    interface Listener {
        fun onDateSelected(date: LocalDate)
    }

    private var listener: Listener? = null
    private var currentMonth: YearMonth = YearMonth.now()
    private val locale = Locale("ru")

    companion object {
        fun newInstance(year: Int, month: Int): CalendarDialogFragment {
            val f = CalendarDialogFragment()
            f.currentMonth = YearMonth.of(year, month)
            return f
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) listener = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.popup_calendar, container, false)
        val tvMonthYear = v.findViewById<TextView>(R.id.popup_month_year)
        val btnPrev = v.findViewById<ImageView>(R.id.btn_prev_month)
        val btnNext = v.findViewById<ImageView>(R.id.btn_next_month)
        val grid = v.findViewById<GridLayout>(R.id.grid_dates)
        tvMonthYear.text = currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, locale) + " " + currentMonth.year.toString()
        populateGrid(grid, currentMonth)
        btnPrev.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            tvMonthYear.text = currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, locale) + " " + currentMonth.year.toString()
            populateGrid(grid, currentMonth)
        }
        btnNext.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            tvMonthYear.text = currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, locale) + " " + currentMonth.year.toString()
            populateGrid(grid, currentMonth)
        }
        return v
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun populateGrid(grid: GridLayout, ym: YearMonth) {
        grid.removeAllViews()
        grid.columnCount = 7
        val first = ym.atDay(1)
        val startIndex = if (first.dayOfWeek.value == 7) 6 else first.dayOfWeek.value - 1
        val days = ym.lengthOfMonth()
        val totalCells = startIndex + days
        for (i in 0 until totalCells) {
            val inflater = LayoutInflater.from(context)
            val cell = inflater.inflate(R.layout.day_cell, grid, false)
            val tv = cell.findViewById<TextView>(R.id.tv_day_cell)
            if (i < startIndex) {
                tv.text = ""
                tv.background = resources.getDrawable(R.drawable.week_day_bg_default, null)
            } else {
                val day = i - startIndex + 1
                val date = ym.atDay(day)
                tv.text = day.toString()
                tv.setOnClickListener {
                    listener?.onDateSelected(date)
                    dismiss()
                }
            }
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            cell.layoutParams = params
            grid.addView(cell)
        }
        val rows = ((totalCells + 6) / 7)
        grid.rowCount = rows
    }
}
