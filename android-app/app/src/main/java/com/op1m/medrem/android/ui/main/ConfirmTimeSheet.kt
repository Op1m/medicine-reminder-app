package com.op1m.medrem.android.ui.main

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.op1m.medrem.android.R

class ConfirmTimeSheet : BottomSheetDialogFragment() {

    interface Listener {
        fun onConfirmNow(time: String)
        fun onConfirmBySchedule(time: String)
        fun onConfirmTimeCustom(time: String, chosen: String)
    }

    private var listener: Listener? = null

    fun setListener(l: Listener) { listener = l }

    companion object {
        fun newInstance(count: Int): ConfirmTimeSheet {
            val f = ConfirmTimeSheet()
            val b = Bundle()
            b.putInt("count", count)
            f.arguments = b
            return f
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val d = super.onCreateDialog(savedInstanceState)
        d.window?.setDimAmount(0.3f)
        return d
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.sheet_confirm_time, container, false)
        val tvTitle = v.findViewById<TextView>(R.id.sheet_title)
        val btnNow = v.findViewById<Button>(R.id.sheet_btn_now)
        val btnSchedule = v.findViewById<Button>(R.id.sheet_btn_schedule)
        val btnTime = v.findViewById<Button>(R.id.sheet_btn_time)
        val btnCancel = v.findViewById<Button>(R.id.sheet_btn_cancel)
        val count = arguments?.getInt("count", 0) ?: 0
        val time = arguments?.getString("time") ?: ""
        tvTitle.text = "Когда вы приняли препараты?\nОтметить $count лекарств на время"
        btnNow.setOnClickListener {
            listener?.onConfirmNow(time)
            dismiss()
        }
        btnSchedule.setOnClickListener {
            listener?.onConfirmBySchedule(time)
            dismiss()
        }
        btnTime.setOnClickListener {
            listener?.onConfirmTimeCustom(time, "09:00")
            dismiss()
        }
        btnCancel.setOnClickListener { dismiss() }
        return v
    }
}
