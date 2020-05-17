package com.aiqfome.aiqchart.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;

import com.aiqfome.aiqchart.R;
import com.aiqfome.aiqchart.model.ChartSet;

import java.util.ArrayList;


/**
 * Implements a bar chart extending {@link com.aiqfome.aiqchart.view.ChartView}
 */
public abstract class BaseBarChartView extends ChartView {


    /**
     * Style applied to Graph
     */
    final Style style;

    /**
     * Offset to control bar positions. Added due to multiset charts.
     */
    float drawingOffset;

    /**
     * Bar width
     */
    float barWidth;


    public BaseBarChartView(Context context, AttributeSet attrs) {

        super(context, attrs);

        style = new Style(
                context.getTheme().obtainStyledAttributes(attrs, R.styleable.BarChartAttrs, 0, 0));

    }


    public BaseBarChartView(Context context) {

        super(context);

        style = new Style();
    }

    @Override
    public void onAttachedToWindow() {

        super.onAttachedToWindow();
        style.init();
    }

    @Override
    public void onDetachedFromWindow() {

        super.onDetachedFromWindow();
        style.clean();
    }

    @Override
    protected void onDrawChart(Canvas canvas, ArrayList<ChartSet> data) {
    }

    @Override
    public void reset() {

        super.reset();
        setMandatoryBorderSpacing();
    }


    /**
     * Draws a bar (a chart bar btw :)).
     *
     * @param canvas {@link Canvas} used to draw the background
     * @param left   The X coordinate of the left side of the rectangle
     * @param top    The Y coordinate of the top of the rectangle
     * @param right  The X coordinate of the right side of the rectangle
     * @param bottom The Y coordinate of the bottom of the rectangle
     */
    void drawBar(Canvas canvas, float left, float top, float right, float bottom) {

        canvas.drawRoundRect(
                new RectF(Math.round(left), Math.round(top), Math.round(right), Math.round(bottom)),
                style.cornerRadius, style.cornerRadius, style.barPaint);
    }


    /**
     * Draws the background (not the fill) of a bar, the one behind the bar.
     *
     * @param canvas {@link Canvas} used to draw the background
     * @param left   The X coordinate of the left side of the rectangle
     * @param top    The Y coordinate of the top of the rectangle
     * @param right  The X coordinate of the right side of the rectangle
     * @param bottom The Y coordinate of the bottom of the rectangle
     */
    void drawBarBackground(Canvas canvas, float left, float top, float right, float bottom) {

        canvas.drawRoundRect(
                new RectF(Math.round(left), Math.round(top), Math.round(right), Math.round(bottom)),
                style.cornerRadius, style.cornerRadius, style.barBackgroundPaint);
    }


    /**
     * Calculates Bar width based on the distance of two horizontal labels.
     *
     * @param nSets Number of sets
     * @param x0    Coordinate(n)
     * @param x1    Coordinate(n+1)
     */
    void calculateBarsWidth(int nSets, float x0, float x1) {

        barWidth = ((x1 - x0) - style.barSpacing - style.setSpacing * (nSets - 1)) / nSets;
    }


    /**
     * Having calculated previously the barWidth gives the offset to know
     * where to start drawing the first bar of each group.
     *
     * @param size Size of sets
     */
    void calculatePositionOffset(int size) {

        if (size % 2 == 0)
            drawingOffset = size * barWidth / 2 + (size - 1) * (style.setSpacing / 2);
        else drawingOffset = size * barWidth / 2 + ((size - 1) / 2) * style.setSpacing;
    }

	
    /*
     * --------
	 * Setters
	 * --------
	 */


    /**
     * Define the space to use between bars.
     *
     * @param spacing Spacing between {@link com.aiqfome.aiqchart.model.Bar}
     */
    public void setBarSpacing(float spacing) {

        style.barSpacing = spacing;
    }


    /**
     * When multiset, it defines the space to use set.
     *
     * @param spacing Spacing between {@link com.aiqfome.aiqchart.model.BarSet}
     */
    public void setSetSpacing(float spacing) {

        style.setSpacing = spacing;
    }


    /**
     * Color to use in bars background.
     *
     * @param color Color of background in case setBarBackground has been set to True
     */
    public void setBarBackgroundColor(@ColorInt int color) {

        style.hasBarBackground = true;
        style.mBarBackgroundColor = color;
    }


    /**
     * Round corners of bars.
     *
     * @param radius Radius applied to the corners of {@link com.aiqfome.aiqchart.model.Bar}
     */
    public void setRoundCorners(@FloatRange(from = 0.f) float radius) {

        style.cornerRadius = radius;
    }


    /**
     * Style object containing bar chart specific customization.
     */
    public class Style {

        private static final int DEFAULT_COLOR = -16777216;

        /**
         * Bars fill variables
         */
        Paint barPaint;

        /**
         * Spacing between bars
         */
        float barSpacing;

        float setSpacing;

        /**
         * Bar background variables
         */
        Paint barBackgroundPaint;

        boolean hasBarBackground;
        /**
         * Radius to round corners
         **/
        float cornerRadius;
        private int mBarBackgroundColor;


        Style() {

            mBarBackgroundColor = DEFAULT_COLOR;
            hasBarBackground = false;

            barSpacing = getResources().getDimension(R.dimen.bar_spacing);
            setSpacing = getResources().getDimension(R.dimen.set_spacing);
            cornerRadius = getResources().getDimension(R.dimen.corner_radius);
        }


        Style(TypedArray attrs) {

            mBarBackgroundColor = attrs.getColor(R.styleable.BarChartAttrs_chart_barBackgroundColor,
                    -1);
            hasBarBackground = mBarBackgroundColor != -1;

            barSpacing = attrs.getDimension(R.styleable.BarChartAttrs_chart_barSpacing,
                    getResources().getDimension(R.dimen.bar_spacing));
            setSpacing = attrs.getDimension(R.styleable.BarChartAttrs_chart_setSpacing,
                    getResources().getDimension(R.dimen.set_spacing));
            cornerRadius = attrs.getDimension(R.styleable.BarChartAttrs_chart_cornerRadius,
                    getResources().getDimension(R.dimen.corner_radius));
        }

        private void init() {

            barPaint = new Paint();
            barPaint.setStyle(Paint.Style.FILL);

            barBackgroundPaint = new Paint();
            barBackgroundPaint.setColor(mBarBackgroundColor);
            barBackgroundPaint.setStyle(Paint.Style.FILL);
        }

        private void clean() {

            barPaint = null;
            barBackgroundPaint = null;
        }

    }

}