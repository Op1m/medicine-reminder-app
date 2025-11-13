package com.op1m.medrem.android.ui.main

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.op1m.medrem.android.R

class AddOptionsSheet : BottomSheetDialogFragment() {

    interface Listener {
        fun onNewMedicineRequested()
        fun onNewCourseRequested()
        fun onCancelAddOptions()
    }

    private var listener: Listener? = null

    fun setListener(l: Listener) { listener = l }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val d = super.onCreateDialog(savedInstanceState)
        d.window?.setDimAmount(0.3f)
        return d
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.sheet_add_options, container, false)
        val btnNewMed = v.findViewById<Button>(R.id.sheet_btn_new_med)
        val btnNewCourse = v.findViewById<Button>(R.id.sheet_btn_new_course)
        val btnCancel = v.findViewById<Button>(R.id.sheet_btn_cancel)
        btnNewMed.setOnClickListener {
            listener?.onNewMedicineRequested()
            dismiss()
        }
        btnNewCourse.setOnClickListener {
            listener?.onNewCourseRequested()
            dismiss()
        }
        btnCancel.setOnClickListener {
            listener?.onCancelAddOptions()
            dismiss()
        }
        return v
    }
}
