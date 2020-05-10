package com.aiqfome.aiqchart.model;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Size;

import static com.aiqfome.aiqchart.util.Preconditions.checkNotNull;

/**
 * Data model containing a set of {@link Bar} to be used by {@link com.aiqfome.aiqchart.view.BaseBarChartView}.
 */
public class BarSet extends ChartSet {

    private static final String TAG = "chart.model.BarSet";


    public BarSet() {

        super();
    }


    public BarSet(@NonNull String[] labels, @NonNull float[] values) {

        super();

        if (labels.length != values.length)
            throw new IllegalArgumentException("Arrays size doesn't match.");

        checkNotNull(labels);
        checkNotNull(values);

        int nEntries = labels.length;
        for (int i = 0; i < nEntries; i++)
            addBar(labels[i], values[i]);
    }


    /**
     * Add new {@link com.aiqfome.aiqchart.model.Bar} from a string and a float.
     *
     * @param label new {@link com.aiqfome.aiqchart.model.Bar}'s label
     * @param value new {@link com.aiqfome.aiqchart.model.Bar}'s value
     */
    public void addBar(String label, float value) {

        this.addBar(new Bar(label, value));
    }


    /**
     * Add new {@link com.aiqfome.aiqchart.model.Bar}.
     *
     * @param bar new nonnull {@link com.aiqfome.aiqchart.model.Bar}
     */
    public void addBar(@NonNull Bar bar) {

        this.addEntry(checkNotNull(bar));
    }


    /*
     * --------
     * Getters
     * --------
     */

    /**
     * Retrieve line's color.
     *
     * @return {@link com.aiqfome.aiqchart.model.BarSet} color.
     */
    public int getColor() {

        return this.getEntry(0).getColor();
    }


    /*
     * -------------
     * Setters
     * -------------
     */

    /**
     * Define the color of bars. Previously defined colors will be overridden.
     *
     * @param color Color to be assigned to every bar in this set.
     * @return {@link com.aiqfome.aiqchart.model.BarSet} self-reference.
     */
    public BarSet setColor(@ColorInt int color) {

        for (ChartEntry e : getEntries())
            e.setColor(color);
        return this;
    }


    /**
     * Define a gradient color to the bars. Previously defined colors will be overridden.
     *
     * @param colors    The colors to be distributed among gradient
     * @param positions Position/order from which the colors will be place
     * @return {@link com.aiqfome.aiqchart.model.BarSet} self-reference.
     */
    public BarSet setGradientColor(@NonNull @Size(min = 1) int colors[], float[] positions) {

        if (colors.length == 0)
            throw new IllegalArgumentException("Colors argument can't be null or empty.");
        checkNotNull(colors);

        for (ChartEntry e : getEntries())
            ((Bar) e).setGradientColor(colors, positions);
        return this;
    }

}
