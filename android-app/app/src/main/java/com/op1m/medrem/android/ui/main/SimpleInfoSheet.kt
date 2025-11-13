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

class SimpleInfoSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(title: String, msg: String): SimpleInfoSheet {
            val f = SimpleInfoSheet()
            val b = Bundle()
            b.putString("title", title)
            b.putString("msg", msg)
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
        val v = inflater.inflate(R.layout.sheet_simple_info, container, false)
        val tvTitle = v.findViewById<TextView>(R.id.sheet_title)
        val tvMsg = v.findViewById<TextView>(R.id.sheet_msg)
        val btnOk = v.findViewById<Button>(R.id.sheet_btn_cancel)
        tvTitle.text = arguments?.getString("title") ?: ""
        tvMsg.text = arguments?.getString("msg") ?: ""
        btnOk.setOnClickListener { dismiss() }
        return v
    }
}
