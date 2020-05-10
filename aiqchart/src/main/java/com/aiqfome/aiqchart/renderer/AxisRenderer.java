package com.aiqfome.aiqchart.renderer;

import android.graphics.Canvas;

import com.aiqfome.aiqchart.model.ChartEntry;
import com.aiqfome.aiqchart.model.ChartSet;
import com.aiqfome.aiqchart.view.ChartView.Style;

import java.text.DecimalFormat;
import java.util.ArrayList;


/**
 * Class responsible to control vertical measures, positions, yadda yadda.
 * If the drawing is requested it will also take care of it.
 */
public abstract class AxisRenderer {

    private static final float DEFAULT_STEPS_NUMBER = 3;

    /**
     * Label's values formatted
     */
    ArrayList<String> labels;

    /**
     * Label's values
     */
    ArrayList<Float> labelsValues;

    /**
     * Labels position
     */
    ArrayList<Float> labelsPos;

    /**
     * Refers to the coordinate X in case of Axis Y and coordinate Y in case of Axis X
     */
    float labelsStaticPos;

    /**
     * Minimum value of labels
     */
    float minLabelValue;

    /**
     * Screen step between labels
     */
    float screenStep;

    /**
     * Starting X point of the axis
     */
    float axisPosition;

    /**
     * Mandatory horizontal border when necessary (ex: BarCharts)
     */
    float mandatoryBorderSpacing;

    /**
     * Define whether labels must be taken from data or calculated from values
     */
    boolean handleValues;

    /**
     * Inner chart borders (inner chart means the chart's area where datasets are drawn)
     */
    float mInnerChartLeft;
    float mInnerChartTop;
    float mInnerChartRight;
    float mInnerChartBottom;

    /**
     * Object containing style attributes of chart
     */
    Style style;

    /**
     * Maximum value of labels
     */
    private float maxLabelValue;

    /**
     * Step between labels
     */
    private float step;


    AxisRenderer() {

        reset();
    }


    /**
     * Define labels/values of axis.
     *
     * @param data  Chart data
     * @param style Chart style
     */
    public void init(ArrayList<ChartSet> data, Style style) {

        if (handleValues) {
            if (minLabelValue == 0 && maxLabelValue == 0) {
                float[] borders;
                if (hasStep()) borders = findBorders(data, step); // no borders, step
                else borders = findBorders(data); // no borders, no step
                minLabelValue = borders[0];
                maxLabelValue = borders[1];
            }
            if (!hasStep()) setBorderValues(minLabelValue, maxLabelValue);
            labelsValues = calculateValues(minLabelValue, maxLabelValue, step);
            labels = convertToLabelsFormat(labelsValues, style.getLabelsFormat());
        } else {
            labels = extractLabels(data);
        }
        this.style = style;
    }


    /**
     * Dispose the various axis elements in their positions.
     */
    void dispose() {

        axisPosition = defineAxisPosition();
        labelsStaticPos = defineStaticLabelsPosition(axisPosition, style.getAxisLabelsSpacing());
    }


    /**
     * Measure inner bounds required in order to have enough space
     * to display all axis elements based.
     *
     * @param left   left position of chart
     * @param top    top position of chart
     * @param right  right position of chart
     * @param bottom bottom position of chart
     */
    public void measure(int left, int top, int right, int bottom) {

        mInnerChartLeft = measureInnerChartLeft(left);
        mInnerChartTop = measureInnerChartTop(top);
        mInnerChartRight = measureInnerChartRight(right);
        mInnerChartBottom = measureInnerChartBottom(bottom);
    }


    /**
     * Define position of axis.
     *
     * @return Coordinate indication position of axis.
     */
    protected abstract float defineAxisPosition();


    /**
     * Define static position of labels.
     * If X axis, static position means vertical labels position,
     * otherwise, if Y it means horizontal labels position.
     *
     * @param axisCoordinate Coordinate indicating axis position.
     * @param distanceToAxis Distance to be set between labels and axis.
     * @return Labels position.
     */
    protected abstract float defineStaticLabelsPosition(float axisCoordinate, int distanceToAxis);


    /**
     * Method called from onDraw method to draw AxisController data.
     *
     * @param canvas {@link Canvas} to use while drawing the data
     */
    protected abstract void draw(Canvas canvas);


    /**
     * Based in a (real) value returns the associated screen point.
     *
     * @param index Index of label.
     * @param value Value to be parsed in display coordinate.
     * @return Display's coordinate
     */
    public abstract float parsePos(int index, double value);


    /**
     * Measure the necessary padding from the chart left border defining the
     * coordinate of the inner chart left border. Inner Chart refers only to the
     * area where chart data will be draw, excluding labels, axis, etc.
     *
     * @param left Left position of chart area
     * @return Coordinate of the inner left side of the chart
     */
    protected abstract float measureInnerChartLeft(int left);


    /**
     * Measure the necessary padding from the chart left border defining the
     * coordinate of the inner chart top border. Inner Chart refers only to the
     * area where chart data will be draw, excluding labels, axis, etc.
     *
     * @param top Top position of chart area
     * @return Coordinate of the inner top side of the chart
     */
    protected abstract float measureInnerChartTop(int top);


    /**
     * Measure the necessary padding from the chart left border defining the
     * coordinate of the inner chart right border. Inner Chart refers only to the
     * area where chart data will be draw, excluding labels, axis, etc.
     *
     * @param right Right position of chart area
     * @return Coordinate of the inner right side of the chart
     */
    protected abstract float measureInnerChartRight(int right);


    /**
     * Measure the necessary padding from the chart left border defining the
     * coordinate of the inner chart bottom border. Inner Chart refers only to the
     * area where chart data will be draw, excluding labels, axis, etc.
     *
     * @param bottom Bottom position of chart area
     * @return Coordinate of the inner bottom side of the chart
     */
    protected abstract float measureInnerChartBottom(int bottom);


    /**
     * Reset renderer attributes to defaults.
     */
    public void reset() {

        mandatoryBorderSpacing = 0;
        step = -1;
        labelsStaticPos = 0;
        axisPosition = 0;
        minLabelValue = 0;
        maxLabelValue = 0;
        handleValues = false;
    }


    /**
     * In case of a Chart that requires a mandatory border spacing (ex. BarChart).
     *
     * @param innerStart Inner chart start
     * @param innerEnd   Inner chart end
     */
    void defineMandatoryBorderSpacing(float innerStart, float innerEnd) {

        if (mandatoryBorderSpacing == 1)
            mandatoryBorderSpacing = (innerEnd - innerStart - style.getAxisBorderSpacing() * 2)
                    / labels.size() / 2;
    }


    /**
     * Calculates the position of each label along the axis.
     *
     * @param innerStart Start inner position the chart
     * @param innerEnd   End inned position of chart
     */
    void defineLabelsPosition(float innerStart, float innerEnd) {

        int nLabels = labels.size();
        screenStep = (innerEnd
                - innerStart
                - style.getAxisTopSpacing()
                - style.getAxisBorderSpacing() * 2
                - mandatoryBorderSpacing * 2) / (nLabels - 1);

        labelsPos = new ArrayList<>(nLabels);
        float currPos = innerStart + style.getAxisBorderSpacing() + mandatoryBorderSpacing;
        for (int i = 0; i < nLabels; i++) {
            labelsPos.add(currPos);
            currPos += screenStep;
        }
    }


    /**
     * Generate and format strings out of axis values.
     *
     * @param values Axis values
     * @param format Format to be applied to string results
     * @return An {@link ArrayList} containing the set of strings generated
     * from axis values and to be displayed along the axis.
     */
    ArrayList<String> convertToLabelsFormat(ArrayList<Float> values, DecimalFormat format) {

        int size = values.size();
        ArrayList<String> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            result.add(format.format(values.get(i)));
        return result;
    }


    /**
     * Extract labels from chart data.
     *
     * @param sets {@link ArrayList} containing all {@link ChartSet} elements of chart
     * @return Extracted labels which are common among all {@link ChartSet} elements.
     */
    ArrayList<String> extractLabels(ArrayList<ChartSet> sets) {

        int size = sets.get(0).size();
        ArrayList<String> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            result.add(sets.get(0).getLabel(i));
        return result;
    }


    /**
     * Find out what are the minimum and maximum values of the
     * axis based on {@link ChartSet} values.
     *
     * @param sets {@link ArrayList} containing {@link ChartSet} elements of chart
     * @return Float vector containing both minimum and maximum value to be used.
     */
    float[] findBorders(ArrayList<ChartSet> sets) {

        float max = Integer.MIN_VALUE;
        float min = Integer.MAX_VALUE;

        for (ChartSet set : sets) {  // Find minimum and maximum value out of all chart entries
            for (ChartEntry e : set.getEntries()) {
                if (e.getValue() >= max) max = e.getValue();
                if (e.getValue() <= min) min = e.getValue();
            }
        }

        if (max < 0) max = 0;
        if (min > 0) min = 0;

        if (min == max) max += 1;  // All given set values are equal

        return new float[]{min, max};
    }


    /**
     * Find out what are the minimum and maximum values of the
     * axis based on {@link ChartSet} values.
     *
     * @param sets {@link ArrayList} containing {@link ChartSet} elements of chart
     * @param step Step to be used between axis values
     * @return Float vector containing both minimum and maximum value to be used.
     */
    float[] findBorders(ArrayList<ChartSet> sets, float step) {

        float[] borders = findBorders(sets);
        while ((borders[1] - borders[0]) % step != 0) borders[1] += 1; // Assure border fit step

        return borders;
    }


    /**
     * Calculate labels based on the minimum and maximum value displayed
     * as well as the step used to defined both of them.
     *
     * @param min  Minimum axis value
     * @param max  Maximum axis value
     * @param step Step to be used between axis values
     * @return {@link ArrayList} containing all values to be displayed along the axis.
     */
    ArrayList<Float> calculateValues(float min, float max, float step) {

        ArrayList<Float> result = new ArrayList<>();
        float pos = min;
        while (pos <= max) {
            result.add(pos);
            pos += step;
        }

        // Set max Y axis label in case isn't already there
        if (result.get(result.size() - 1) < max) result.add(pos);

        return result;
    }


    /**
     * Get left inner chart border (inner chart means the chart's area where datasets are drawn).
     *
     * @return Inner left coordinate position.
     */
    public float getInnerChartLeft() {

        return mInnerChartLeft;
    }


    /**
     * Get top inner chart border (inner chart means the chart's area where datasets are drawn).
     *
     * @return Inner top coordinate position.
     */
    public float getInnerChartTop() {

        return mInnerChartTop;
    }


    /**
     * Get right inner chart border (inner chart means the chart's area where datasets are drawn).
     *
     * @return Inner left coordinate position.
     */
    public float getInnerChartRight() {

        return mInnerChartRight;
    }


    /**
     * Get bottom inner chart border (inner chart means the chart's area where datasets are drawn).
     *
     * @return Inner bottom coordinate position.
     */
    public float getInnerChartBottom() {

        return mInnerChartBottom;
    }


    /**
     * Get inner chart bounds.
     * Inner chart means the chart's area where datasets are drawn,
     * chart's area excluding axis area.
     *
     * @return Inner left coordinate position.
     */
    public float[] getInnerChartBounds() {

        return new float[]{mInnerChartLeft, mInnerChartTop, mInnerChartRight, mInnerChartBottom};
    }


    /**
     * Get step between axis values.
     *
     * @return Step used between axis values.
     */
    public float getStep() {

        return step;
    }

    /**
     * Set step between axis values.
     *
     * @param step Step to be used between axis values.
     */
    public void setStep(int step) {

        this.step = step;
    }

    /**
     * @return Axis maximum border value.
     */
    public float getBorderMaximumValue() {

        return maxLabelValue;
    }

    /**
     * @return Axis minimum border value.
     */
    public float getBorderMinimumValue() {

        return minLabelValue;
    }

    /**
     * If needs mandatory border spacing.
     *
     * @return True if needs mandatory border spacing, False otherwise.
     */
    public boolean hasMandatoryBorderSpacing() {

        return (mandatoryBorderSpacing == 1);
    }

    /**
     * @return True if step has been defined, False otherwise.
     */
    boolean hasStep() {
        return (step != -1);
    }

    /**
     * Set renderer to handle {@link ChartSet} values, not labels.
     *
     * @param bool True to handle {@link ChartSet} values, False otherwise.
     */
    public void setHandleValues(boolean bool) {

        handleValues = bool;
    }

    /**
     * Set if axis needs mandatory border spacing.
     *
     * @param bool True if needs mandatory border spacing, False otherwise.
     */
    public void setMandatoryBorderSpacing(boolean bool) {

        mandatoryBorderSpacing = (bool) ? 1 : 0;
    }


    /**
     * Set inner chart bounds.
     * Inner chart means the chart's area where datasets are drawn,
     * chart's area excluding axis area.
     *
     * @param left   Inner left coordinate position.
     * @param top    Inner top coordinate position.
     * @param right  Inner right coordinate position.
     * @param bottom Inner bottom coordinate position.
     */
    public void setInnerChartBounds(float left, float top, float right, float bottom) {

        mInnerChartLeft = left;
        mInnerChartTop = top;
        mInnerChartRight = right;
        mInnerChartBottom = bottom;
    }


    /**
     * Force axis range of values.
     * A step is seen as the step to be defined between 2 labels. As an
     * example a step of 2 with a maxAxisValue of 6 will end up with
     * {0, 2, 4, 6} as labels.
     *
     * @param min  The minimum value that Y axis will have as a label
     * @param max  The maximum value that Y axis will have as a label
     * @param step (real) value distance from every label
     */
    public void setBorderValues(float min, float max, float step) {

        if (min >= max) throw new IllegalArgumentException(
                "Minimum border value must be greater than maximum values");

        this.step = step;
        maxLabelValue = max;
        minLabelValue = min;
    }


    /**
     * Force axis range of values. If minimum greater than 0
     * the largest divisor between the delta maximum - minimum will be set automatically as
     * the step.
     *
     * @param min The minimum value that Y axis will have as a label
     * @param max The maximum value that Y axis will have as a label
     */
    public void setBorderValues(float min, float max) {

        if (!hasStep()) step = (max - min) / DEFAULT_STEPS_NUMBER;
        setBorderValues(min, max, step);
    }


    public enum LabelPosition {

        /**
         * No labels should be displayed.
         */
        NONE,

        /**
         * Labels must be positioned outside of inner chart.
         */
        OUTSIDE,

        /**
         * Labels must be positioned outside of inner chart.
         */
        INSIDE
    }

}