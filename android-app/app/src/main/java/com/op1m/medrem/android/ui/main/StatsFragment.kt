package com.op1m.medrem.android.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.op1m.medrem.android.R

class StatsFragment : Fragment() {

    companion object {
        fun newInstance(): StatsFragment = StatsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_stats, container, false)
        v.findViewById<TextView>(R.id.stats_label).text = "Статистика"
        return v
    }
}
