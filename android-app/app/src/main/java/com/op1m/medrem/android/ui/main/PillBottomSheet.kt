package com.op1m.medrem.android.ui.main

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.op1m.medrem.android.R

class PillBottomSheet : BottomSheetDialogFragment() {

    interface Listener {
        fun onPillAccept(medId: Int, time: String)
        fun onPillSkip(medId: Int, time: String)
        fun onPillDelete(medId: Int, time: String)
        fun onPillMove(medId: Int, time: String)
        fun onPillBack()
    }

    private var listener: Listener? = null
    private var medId: Int = -1
    private var medName: String? = null
    private var time: String = ""
    private var medStatus: String? = null
    private var acceptedAt: String? = null

    companion object {
        fun newInstance(
            medId: Int,
            medName: String,
            time: String,
            medStatus: String?,
            acceptedAt: String?
        ): PillBottomSheet {
            val s = PillBottomSheet()
            val b = Bundle()
            b.putInt("medId", medId)
            b.putString("medName", medName)
            b.putString("time", time)
            b.putString("medStatus", medStatus)
            b.putString("acceptedAt", acceptedAt)
            s.arguments = b
            return s
        }
    }

    fun setListener(l: Listener) {
        listener = l
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            medId = it.getInt("medId", -1)
            medName = it.getString("medName")
            time = it.getString("time", "")
            medStatus = it.getString("medStatus")
            acceptedAt = it.getString("acceptedAt")
        }
        isCancelable = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.isDraggable = true
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.pill_bottom_sheet, container, false)

        val tvTitle = v.findViewById<TextView>(R.id.tv_title)
        val tvSub = v.findViewById<TextView>(R.id.tv_sub)
        val btnBack = v.findViewById<ImageButton>(R.id.btn_back)
        val btnInfo = v.findViewById<ImageButton>(R.id.btn_info)
        val btnAccept = v.findViewById<AppCompatButton>(R.id.btn_accept)
        val btnSkip = v.findViewById<AppCompatButton>(R.id.btn_skip)
        val btnPostpone = v.findViewById<LinearLayout>(R.id.btn_postpone)
        val btnDelete = v.findViewById<LinearLayout>(R.id.btn_delete)
        val ivMain = v.findViewById<ImageView>(R.id.iv_pill_main)

        tvTitle.text = medName ?: "Препарат"
        tvSub.text = "Напоминание: $time"

        btnBack.setOnClickListener {
            listener?.onPillBack()
            dismiss()
        }

        btnInfo.setOnClickListener {}

        btnAccept.setOnClickListener {
            if (medId >= 0) listener?.onPillAccept(medId, time)
            dismiss()
        }

        btnSkip.setOnClickListener {
            if (medId >= 0) listener?.onPillSkip(medId, time)
            dismiss()
        }

        btnDelete.setOnClickListener {
            if (medId >= 0) listener?.onPillDelete(medId, time)
            dismiss()
        }

        btnPostpone.setOnClickListener {
            if (medId >= 0) listener?.onPillMove(medId, time)
            dismiss()
        }

        return v
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? com.google.android.material.bottomsheet.BottomSheetDialog ?: return
        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) ?: return

        bottomSheet.setBackgroundResource(R.drawable.bg_sheet)
        val screenH = resources.displayMetrics.heightPixels
        val targetHeight = (screenH * 0.78).toInt()

        bottomSheet.layoutParams.height = targetHeight
        bottomSheet.requestLayout()

        val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet)
        behavior.isFitToContents = true
        behavior.peekHeight = targetHeight
        behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = true
    }
}
