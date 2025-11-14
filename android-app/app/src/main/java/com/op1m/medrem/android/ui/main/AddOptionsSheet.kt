package com.op1m.medrem.android.ui.main

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.op1m.medrem.android.R

class AddOptionsSheet : DialogFragment() {

    interface Listener {
        fun onNewMedicineRequested()
        fun onNewCourseRequested()
        fun onCancelAddOptions()
    }

    private var listener: Listener? = null

    fun setListener(l: Listener) {
        listener = l
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val d = super.onCreateDialog(savedInstanceState)
        d.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return d
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.dialog_sheet_actions, container, false)
        val title = v.findViewById<TextView>(R.id.sheet_title)
        val optionsContainer = v.findViewById<LinearLayout>(R.id.options_container)
        val btnCancel = v.findViewById<TextView>(R.id.btn_cancel_sheet)
        title.text = "Что вы хотите добавить?"

        addOption(inflater, optionsContainer, "Новый препарат") {
            listener?.onNewMedicineRequested()
            dismiss()
        }
        addDivider(optionsContainer)
        addOption(inflater, optionsContainer, "Новый курс") {
            listener?.onNewCourseRequested()
            dismiss()
        }

        btnCancel.setOnClickListener {
            listener?.onCancelAddOptions()
            dismiss()
        }
        return v
    }

    private fun addOption(inflater: LayoutInflater, parent: LinearLayout, text: String, onClick: () -> Unit) {
        val t = inflater.inflate(R.layout.sheet_action_row, parent, false) as TextView
        t.text = text
        t.setOnClickListener { onClick() }
        parent.addView(t)
    }

    private fun addDivider(parent: LinearLayout) {
        val inflater = LayoutInflater.from(context)
        val d = inflater.inflate(R.layout.sheet_divider, parent, false)
        parent.addView(d)
    }
}
