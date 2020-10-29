package com.aiqfome.aiqchartdemo.ui.bar_chart;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aiqfome.aiqchart.animation.Animation;
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
        loadBarChartTwo();

        return binding.getRoot();
    }

    private void loadBarChartOne() {
        final String[] mLabels = {"A", "B", "C", "D"};
        final float[] mValues = {6.5f, 8.5f, 2.5f, 10f};

        BarSet barSet = new BarSet(mLabels, mValues);
        barSet.setColor(this.getResources().getColor(R.color.colorPrimary));

        binding.layoutBarChartOne.barChartOne.reset();
        binding.layoutBarChartOne.barChartOne.addData(barSet);

        binding.layoutBarChartOne.barChartOne.setLabelsFormat(new DecimalFormat("#"));

        Animation anim = new Animation(750);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        binding.layoutBarChartOne.barChartOne.show(anim);

        binding.executePendingBindings();
    }

    @SuppressLint("ResourceAsColor")
    private void loadBarChartTwo() {
        final String[] mLabels = {"", "", "", "", "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};

        final float[] mValues = {2.5f, 3.7f, 4f, 8f, 4.5f, 4f, 5f, 7f, 10f, 14f, 12f, 6f, 7f, 8f,
                9f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 11f, 12f, 14, 13f, 10f, 9f, 8f, 7f, 6f};

        BarSet barSet = new BarSet(mLabels, mValues);
        barSet.setColor(this.getResources().getColor(R.color.colorPrimary));
        binding.layoutBarChartTwo.barChartTwo.addData(barSet);
        binding.layoutBarChartTwo.barChartTwo.setLabelsFormat(new DecimalFormat("#"));

        binding.layoutBarChartTwo.barChartTwo.show(new Animation().
                setInterpolator(new AccelerateDecelerateInterpolator()));

        binding.executePendingBindings();
    }
}
