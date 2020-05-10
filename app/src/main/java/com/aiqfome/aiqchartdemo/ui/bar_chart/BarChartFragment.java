package com.aiqfome.aiqchartdemo.ui.bar_chart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.aiqfome.aiqchart.model.BarSet;
import com.aiqfome.aiqchartdemo.R;
import com.aiqfome.aiqchartdemo.databinding.FragmentBarChartBinding;

import java.text.DecimalFormat;

public class BarChartFragment extends Fragment {

    private BarChartViewModel barChartViewModel;

    FragmentBarChartBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        barChartViewModel = new ViewModelProvider(this).get(BarChartViewModel.class);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bar_chart, container, false);
        binding.setLifecycleOwner(this);

        loadBarChartOne();

        return binding.getRoot();
    }

    private void loadBarChartOne() {

        final String[] mLabels = {"A", "B", "C", "D"};
        final float[] mValues = {6.5f, 8.5f, 2.5f, 10f};

        BarSet barSet = new BarSet(mLabels, mValues);
        barSet.setColor(this.getResources().getColor(R.color.colorPrimary));

        binding.layoutCharts.barChartOne.reset();
        binding.layoutCharts.barChartOne.addData(barSet);

        binding.layoutCharts.barChartOne.setLabelsFormat(new DecimalFormat("#"));

        com.aiqfome.aiqchart.animation.Animation anim = new com.aiqfome.aiqchart.animation.Animation(750);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        binding.layoutCharts.barChartOne.show(anim);

        binding.executePendingBindings();
    }
}
