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

class ConfirmOptionsSheet : BottomSheetDialogFragment() {

    interface Listener {
        fun onAcceptAllRequested(time: String)
        fun onSkipAllRequested(time: String)
        fun onMoveAllRequested(time: String)
    }

    private var listener: Listener? = null

    fun setListener(l: Listener) {
        listener = l
    }

    companion object {
        fun newInstance(count: Int): ConfirmOptionsSheet {
            val f = ConfirmOptionsSheet()
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
        val v = inflater.inflate(R.layout.sheet_confirm_options, container, false)
        val tvTitle = v.findViewById<TextView>(R.id.sheet_title)
        val btnAccept = v.findViewById<Button>(R.id.sheet_btn_accept)
        val btnSkip = v.findViewById<Button>(R.id.sheet_btn_skip)
        val btnMove = v.findViewById<Button>(R.id.sheet_btn_move)
        val btnCancel = v.findViewById<Button>(R.id.sheet_btn_cancel)
        val count = arguments?.getInt("count", 0) ?: 0
        val time = arguments?.getString("time") ?: ""
        tvTitle.text = "Принять все препараты\nОтметить $count лекарства на 09:00"
        btnAccept.setOnClickListener {
            listener?.onAcceptAllRequested(time)
            dismiss()
        }
        btnSkip.setOnClickListener {
            listener?.onSkipAllRequested(time)
            dismiss()
        }
        btnMove.setOnClickListener {
            listener?.onMoveAllRequested(time)
            dismiss()
        }
        btnCancel.setOnClickListener { dismiss() }
        return v
    }
}
