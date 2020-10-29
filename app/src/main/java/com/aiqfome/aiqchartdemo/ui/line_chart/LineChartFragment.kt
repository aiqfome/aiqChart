package com.aiqfome.aiqchartdemo.ui.line_chart

import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import com.aiqfome.aiqchart.animation.Animation
import com.aiqfome.aiqchart.model.LineSet
import com.aiqfome.aiqchartdemo.R
import kotlinx.android.synthetic.main.layout_line_chart.*
import java.text.DecimalFormat

class LineChart : Fragment(R.layout.fragment_line_chart) {
    private val lineSet = linkedMapOf(
            "label1" to 5f,
            "label2" to 4.5f,
            "label3" to 4.7f,
            "label4" to 3.5f,
            "label5" to 3.6f,
            "label6" to 7.5f,
            "label7" to 7.5f
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val labels = lineSet.keys
        val values = lineSet.values
        val lineSet = LineSet(labels.toTypedArray(), values.toFloatArray())
        lineSet.color = resources.getColor(R.color.colorPrimary)
        lineChart.addData(lineSet)
        lineChart.setLabelsFormat(DecimalFormat("#"))
        lineChart.show(Animation().setInterpolator(AccelerateDecelerateInterpolator()))
    }
}