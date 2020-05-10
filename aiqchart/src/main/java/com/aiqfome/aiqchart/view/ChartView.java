package com.aiqfome.aiqchart.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.RelativeLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.aiqfome.aiqchart.R;
import com.aiqfome.aiqchart.model.ChartSet;
import com.aiqfome.aiqchart.animation.Animation;
import com.aiqfome.aiqchart.animation.ChartAnimationListener;
import com.aiqfome.aiqchart.listener.OnEntryClickListener;
import com.aiqfome.aiqchart.model.ChartEntry;
import com.aiqfome.aiqchart.renderer.AxisRenderer;
import com.aiqfome.aiqchart.renderer.XRenderer;
import com.aiqfome.aiqchart.renderer.YRenderer;
import com.aiqfome.aiqchart.tooltip.Tooltip;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.aiqfome.aiqchart.util.Preconditions.checkPositionIndex;
import static com.aiqfome.aiqchart.util.Preconditions.checkNotNull;


/**
 * Abstract class to be extend to define any chart that implies axis.
 */
public abstract class ChartView extends RelativeLayout {

    private static final String TAG = "chart.view.ChartView";

    private static final int DEFAULT_WIDTH = 200;

    private static final int DEFAULT_HEIGHT = 100;

    /**
     * Horizontal and Vertical position controllers
     */
    final XRenderer xRndr;

    final YRenderer yRndr;

    /**
     * Style applied to chart
     */
    final Style style;

    /**
     * Chart data to be displayed
     */
    ArrayList<ChartSet> data;

    /**
     * Chart orientation
     */
    private Orientation mOrientation;

    /**
     * Chart borders including padding
     */
    private int mChartLeft;

    private int mChartTop;

    private int mChartRight;

    private int mChartBottom;

    /**
     * Threshold line value
     */

    private ArrayList<Float> mThresholdStartValues;

    private ArrayList<Float> mThresholdEndValues;

    /**
     * Threshold line label
     */

    private ArrayList<Integer> mThresholdStartLabels;

    private ArrayList<Integer> mThresholdEndLabels;

    /**
     * Chart data to be displayed
     */
    private ArrayList<ArrayList<Region>> mRegions;

    /**
     * Gestures detector to trigger listeners callback
     */
    private GestureDetector mGestureDetector;

    /**
     * Listener callback on entry click
     */
    private OnEntryClickListener mEntryListener;

    /**
     * Listener callback on chart click, no entry intersection
     */
    private OnClickListener mChartListener;

    /**
     * Drawing flag
     */
    private boolean mReadyToDraw;

    /**
     * Drawing flag
     */
    private boolean mIsDrawing;

    /**
     * Chart animation
     */
    private Animation mAnim;
    /**
     * Executed only before the chart is drawn for the first time.
     * . borders are defined
     * . digestData(data), to process the data to be drawn
     * . defineRegions(), if listener has been registered
     * this will define the chart regions to handle by onTouchEvent
     */
    private final OnPreDrawListener drawListener = new OnPreDrawListener() {
        @SuppressLint("NewApi")
        @Override
        public boolean onPreDraw() {

            ChartView.this.getViewTreeObserver().removeOnPreDrawListener(this);

            // Generate Paint object with style attributes
            style.init();

            // Initiate axis labels with data and style
            yRndr.init(data, style);
            xRndr.init(data, style);

            // Set the positioning of the whole chart's frame
            mChartLeft = getPaddingLeft();
            mChartTop = getPaddingTop() + style.fontMaxHeight / 2;
            mChartRight = getMeasuredWidth() - getPaddingRight();
            mChartBottom = getMeasuredHeight() - getPaddingBottom();

            // Measure space and set the positioning of the inner border.
            // Inner borders will be chart's frame excluding the space needed by axis.
            // They define the actual area where chart's content will be drawn.
            yRndr.measure(mChartLeft, mChartTop, mChartRight, mChartBottom);
            xRndr.measure(mChartLeft, mChartTop, mChartRight, mChartBottom);

            // Negotiate chart inner boundaries.
            // Both renderers may require different space to draw axis stuff.
            final float[] bounds = negotiateInnerChartBounds(yRndr.getInnerChartBounds(),
                    xRndr.getInnerChartBounds());
            yRndr.setInnerChartBounds(bounds[0], bounds[1], bounds[2], bounds[3]);
            xRndr.setInnerChartBounds(bounds[0], bounds[1], bounds[2], bounds[3]);

            // Dispose the various axis elements in their positions
            yRndr.dispose();
            xRndr.dispose();

            // Parse threshold screen coordinates
            if (!mThresholdStartValues.isEmpty()) {
                for (int i = 0; i < mThresholdStartValues.size(); i++) {
                    mThresholdStartValues.set(i, yRndr.parsePos(0, mThresholdStartValues.get(i)));
                    mThresholdEndValues.set(i, yRndr.parsePos(0, mThresholdEndValues.get(i)));
                }
            }

            // Process data to define screen coordinates
            digestData();

            // In case Views extending ChartView need to pre process data before the onDraw
            onPreDrawChart(data);

            // Define entries regions
            if (mRegions.isEmpty()) {
                int dataSize = data.size();
                int setSize;
                mRegions = new ArrayList<>(dataSize);
                ArrayList<Region> regionSet;
                for (int i = 0; i < dataSize; i++) {
                    setSize = data.get(0).size();
                    regionSet = new ArrayList<>(setSize);
                    for (int j = 0; j < setSize; j++)
                        regionSet.add(new Region());
                    mRegions.add(regionSet);
                }
            }
            defineRegions(mRegions, data);

            // Prepare the animation retrieving the first dump of coordinates to be used
            if (mAnim != null) data = mAnim.prepareEnterAnimation(ChartView.this);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
                ChartView.this.setLayerType(LAYER_TYPE_SOFTWARE, null);

            return mReadyToDraw = true;
        }
    };
    private ChartAnimationListener mAnimListener;
    /**
     * Tooltip
     */
    private Tooltip mTooltip;


    public ChartView(Context context, AttributeSet attrs) {

        super(context, attrs);

        init();
        mGestureDetector = new GestureDetector(context, new GestureListener());
        xRndr = new XRenderer();
        yRndr = new YRenderer();
        style = new Style(context, attrs);
    }


    public ChartView(Context context) {

        super(context);

        init();
        mGestureDetector = new GestureDetector(context, new GestureListener());
        xRndr = new XRenderer();
        yRndr = new YRenderer();
        style = new Style(context);
    }

    private void init() {

        mReadyToDraw = false;
        mThresholdStartValues = new ArrayList<>();
        mThresholdEndValues = new ArrayList<>();
        mThresholdStartLabels = new ArrayList<>();
        mThresholdEndLabels = new ArrayList<>();
        mIsDrawing = false;
        data = new ArrayList<>();
        mRegions = new ArrayList<>();
        mAnimListener = new ChartAnimationListener() {
            @Override
            public boolean onAnimationUpdate(ArrayList<ChartSet> data) {
                if (!mIsDrawing) {
                    addData(data);
                    postInvalidate();
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public void onAttachedToWindow() {

        super.onAttachedToWindow();

        this.setWillNotDraw(false);
        style.init();
    }

    @Override
    public void onDetachedFromWindow() {

        super.onDetachedFromWindow();

        style.clean();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int tmpWidth = widthMeasureSpec;
        int tmpHeight = heightMeasureSpec;

        if (widthMode == MeasureSpec.AT_MOST) tmpWidth = DEFAULT_WIDTH;

        if (heightMode == MeasureSpec.AT_MOST) tmpHeight = DEFAULT_HEIGHT;

        setMeasuredDimension(tmpWidth, tmpHeight);
    }


    /**
     * The method listens for chart clicks and checks whether it intercepts
     * a known Region. It will then use the registered Listener.onClick
     * to return the region's index.
     */
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {

        super.onTouchEvent(event);
        return !(mAnim != null && mAnim.isPlaying()
                || mEntryListener == null && mChartListener == null && mTooltip == null)
                && mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        mIsDrawing = true;
        super.onDraw(canvas);

        if (mReadyToDraw) {
            //long time = System.currentTimeMillis();

            // Draw grid
            if (style.hasVerticalGrid()) drawVerticalGrid(canvas);
            if (style.hasHorizontalGrid()) drawHorizontalGrid(canvas);

            // Draw threshold
            if (!mThresholdStartValues.isEmpty())
                for (int i = 0; i < mThresholdStartValues.size(); i++)
                    drawThreshold(canvas, getInnerChartLeft(), mThresholdStartValues.get(i),
                            getInnerChartRight(), mThresholdEndValues.get(i), style.valueThresPaint);
            if (!mThresholdStartLabels.isEmpty())
                for (int i = 0; i < mThresholdStartLabels.size(); i++)
                    drawThreshold(canvas, data.get(0).getEntry(mThresholdStartLabels.get(i)).getX(),
                            getInnerChartTop(), data.get(0).getEntry(mThresholdEndLabels.get(i)).getX(),
                            getInnerChartBottom(), style.labelThresPaint);

            // Draw data
            if (!data.isEmpty()) onDrawChart(canvas, data);

            // Draw Axis Y
            yRndr.draw(canvas);

            // Draw axis X
            xRndr.draw(canvas);

            //System.out.println("Time drawing "+(System.currentTimeMillis() - time));
        }

        mIsDrawing = false;
    }


    /**
     * Convert {@link ChartEntry} values into screen points.
     */
    private void digestData() {

        int nEntries = data.get(0).size();
        for (ChartSet set : data) {
            for (int i = 0; i < nEntries; i++) {
                set.getEntry(i)
                        .setCoordinates(xRndr.parsePos(i, set.getValue(i)),
                                yRndr.parsePos(i, set.getValue(i)));
            }
        }
    }


    /**
     * (Optional) To be overridden in case the view needs to execute some code before
     * starting the drawing.
     *
     * @param data Array of {@link ChartSet} to do the necessary preparation just before onDraw
     */
    void onPreDrawChart(ArrayList<ChartSet> data) {
    }


    /**
     * (Optional) To be overridden in order for each chart to define its own clickable regions.
     * This way, classes extending ChartView will only define their clickable regions.
     * <p>
     * Important: the returned vector must match the order of the data passed
     * by the user. This ensures that onTouchEvent will return the correct index.
     *
     * @param regions Empty list of regions where result of this method must be assigned
     * @param data    {@link ArrayList} of {@link com.aiqfome.aiqchart.model.ChartSet}
     *                to use while defining each region of a {@link com.aiqfome.aiqchart.view.ChartView}
     */
    void defineRegions(ArrayList<ArrayList<Region>> regions, ArrayList<ChartSet> data) {
    }


    /**
     * Method responsible to draw bars with the parsed screen points.
     *
     * @param canvas The canvas to draw on
     * @param data   {@link ArrayList} of {@link com.aiqfome.aiqchart.model.ChartSet}
     *               to use while drawing the Chart
     */
    protected abstract void onDrawChart(Canvas canvas, ArrayList<ChartSet> data);


    /**
     * Set new data to the chart and invalidates the view to be then drawn.
     *
     * @param set {@link ChartSet} object.
     */
    public void addData(@NonNull ChartSet set) {

        checkNotNull(set);

        if (!data.isEmpty() && set.size() != data.get(0).size())
            throw new IllegalArgumentException("The number of entries between sets doesn't match.");

        data.add(set);
    }


    /**
     * Add full chart data.
     *
     * @param data An array of {@link ChartSet}
     */
    public void addData(ArrayList<ChartSet> data) {

        this.data = data;
    }


    /**
     * Base method when a show chart occurs
     */
    private void display() {

        this.getViewTreeObserver().addOnPreDrawListener(drawListener);
        postInvalidate();
    }


    /**
     * Show chart data
     */
    public void show() {

        for (ChartSet set : data)
            set.setVisible(true);
        display();
    }


    /**
     * Show only a specific chart dataset.
     *
     * @param setIndex Dataset index to be displayed
     */
    public void show(int setIndex) {

        data.get(checkPositionIndex(setIndex, data.size())).setVisible(true);
        display();
    }


    /**
     * Starts the animation given as parameter.
     *
     * @param anim Animation used while showing and updating sets
     */
    public void show(@NonNull Animation anim) {

        mAnim = checkNotNull(anim);
        mAnim.setAnimationListener(mAnimListener);
        show();
    }


    /**
     * Dismiss chart data.
     */
    public void dismiss() {

        dismiss(mAnim);
    }


    /**
     * Dismiss a specific chart dataset.
     *
     * @param setIndex Dataset index to be dismissed
     */
    public void dismiss(int setIndex) {

        data.get(checkPositionIndex(setIndex, data.size())).setVisible(false);
        invalidate();
    }


    /**
     * Dismiss chart data with animation.
     *
     * @param anim Animation used to exit
     */
    public void dismiss(@NonNull Animation anim) {

        mAnim = checkNotNull(anim);
        mAnim.setAnimationListener(mAnimListener);

        final Runnable endAction = mAnim.getEndAction();
        mAnim.withEndAction(new Runnable() {
            @Override
            public void run() {

                if (endAction != null) endAction.run();
                data.clear();
                invalidate();
            }
        });

        data = mAnim.prepareExitAnimation(this);

        invalidate();
    }


    /**
     * Method not expected to be used often. More for testing.
     * Resets chart state to insert new configuration.
     */
    public void reset() {

        if (mAnim != null && mAnim.isPlaying()) mAnim.cancel();

        init();
        xRndr.reset();
        yRndr.reset();
        setOrientation(mOrientation);

        style.labelThresPaint = null;
        style.valueThresPaint = null;
        style.gridPaint = null;
    }


    /**
     * Update set values. Animation support in case previously added.
     *
     * @param setIndex Index of set to be updated
     * @param values   Array of new values. Array length must match current data
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView updateValues(int setIndex, float[] values) {

        data.get(checkPositionIndex(setIndex, data.size())).updateValues(values);
        return this;
    }


    /**
     * Notify {@link ChartView} about updated values. {@link ChartView} will be validated.
     */
    public void notifyDataUpdate() {

        // Ignore update if chart is not even ready to draw or if it is still animating
        if (mAnim != null && !mAnim.isPlaying() && mReadyToDraw || mAnim == null && mReadyToDraw) {

            ArrayList<float[][]> oldCoords = new ArrayList<>(data.size());
            ArrayList<float[][]> newCoords = new ArrayList<>(data.size());

            for (ChartSet set : data)
                oldCoords.add(set.getScreenPoints());

            digestData();
            for (ChartSet set : data)
                newCoords.add(set.getScreenPoints());

            defineRegions(mRegions, data);
            if (mAnim != null) mAnim.prepareUpdateAnimation(oldCoords, newCoords);
            else invalidate();

        } else {
            Log.w(TAG, "Unexpected data update notification. "
                    + "Chart is still not displayed or still displaying.");
        }

    }


    /**
     * Toggles {@link Tooltip} between show and dismiss.
     *
     * @param rect  {@link Rect} containing the bounds of last clicked entry
     * @param value Value of the last entry clicked
     */
    private void toggleTooltip(@NonNull Rect rect, float value) {

        checkNotNull(rect);
        if (!mTooltip.on()) {
            mTooltip.prepare(rect, value);
            showTooltip(mTooltip, true);
        } else {
            dismissTooltip(mTooltip, rect, value);
        }
    }


    /**
     * Adds a tooltip to {@link ChartView}.
     * If is not the case already, the whole tooltip is forced to be inside {@link ChartView}
     * bounds. The area used to apply the correction exclude any padding applied, the whole view
     * size in the layout is take into account.
     *
     * @param tooltip    {@link Tooltip} view to be added
     * @param correctPos False if tooltip should not be forced to be inside ChartView.
     *                   You may want to take care of it.
     */
    public void showTooltip(@NonNull Tooltip tooltip, boolean correctPos) {

        checkNotNull(tooltip);
        if (correctPos) tooltip.correctPosition(mChartLeft, mChartTop, mChartRight, mChartBottom);
        if (tooltip.hasEnterAnimation()) tooltip.animateEnter();
        this.addTooltip(tooltip);
    }


    /**
     * Add {@link Tooltip}/{@link View}. to chart/parent view.
     *
     * @param tooltip tooltip to be added to chart
     */
    private void addTooltip(@NonNull Tooltip tooltip) {

        checkNotNull(tooltip);
        this.addView(tooltip);
        tooltip.setOn(true);
    }


    /**
     * Remove {@link Tooltip}/{@link View} to chart/parent view.
     *
     * @param tooltip tooltip to be removed to chart
     */
    private void removeTooltip(@NonNull Tooltip tooltip) {

        checkNotNull(tooltip);
        this.removeView(tooltip);
        tooltip.setOn(false);
    }


    /**
     * Dismiss tooltip from {@link ChartView}.
     *
     * @param tooltip View to be dismissed
     */
    private void dismissTooltip(@NonNull Tooltip tooltip) {

        dismissTooltip(checkNotNull(tooltip), null, 0);
    }


    /**
     * Dismiss tooltip from {@link ChartView}.
     *
     * @param tooltip View to be dismissed
     */
    private void dismissTooltip(@NonNull final Tooltip tooltip, final Rect rect, final float value) {

        checkNotNull(tooltip);

        if (tooltip.hasExitAnimation()) {
            tooltip.animateExit(new Runnable() {
                @Override
                public void run() {

                    removeTooltip(tooltip);
                    if (rect != null) toggleTooltip(rect, value);
                }
            });
        } else {
            this.removeTooltip(tooltip);
            if (rect != null) this.toggleTooltip(rect, value);
        }
    }


    /**
     * Removes all tooltips currently presented in the chart.
     */
    public void dismissAllTooltips() {

        this.removeAllViews();
        if (mTooltip != null) mTooltip.setOn(false);
    }


    /**
     * Asks the view if it is able to draw now.
     *
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public boolean canIPleaseAskYouToDraw() {

        return !mIsDrawing;
    }


    /**
     * Negotiates the inner bounds required by renderers.
     *
     * @param innersA Inner bounds require by element A
     * @param innersB Inned bound required by element B
     * @return float vector with size equal to 4 containing agreed
     * inner bounds (left, top, right, bottom).
     */
    float[] negotiateInnerChartBounds(float[] innersA, float[] innersB) {

        return new float[]{(innersA[0] > innersB[0]) ? innersA[0] : innersB[0],
                (innersA[1] > innersB[1]) ? innersA[1] : innersB[1],
                (innersA[2] < innersB[2]) ? innersA[2] : innersB[2],
                (innersA[3] < innersB[3]) ? innersA[3] : innersB[3]};
    }


    /**
     * Draw a threshold line or band on the labels or values axis. If same values or same label
     * index have been given then a line will be drawn rather than a band.
     *
     * @param canvas Canvas to draw line/band on.
     * @param left   The left side of the line/band to be drawn
     * @param top    The top side of the line/band to be drawn
     * @param right  The right side of the line/band to be drawn
     * @param bottom The bottom side of the line/band to be drawn
     */
    private void drawThreshold(Canvas canvas, float left, float top, float right, float bottom,
                               Paint paint) {

        if (left == right || top == bottom)
            canvas.drawLine(left, top, right, bottom, paint);
        else canvas.drawRect(left, top, right, bottom, paint);
    }


    /**
     * Draw vertical lines of Grid.
     *
     * @param canvas Canvas to draw on.
     */
    private void drawVerticalGrid(Canvas canvas) {

        final float offset = (getInnerChartRight() - getInnerChartLeft()) / style.gridColumns;
        float marker = getInnerChartLeft();

        if (style.hasYAxis) marker += offset;

        while (marker < getInnerChartRight()) {
            canvas.drawLine(marker, getInnerChartTop(), marker, getInnerChartBottom(),
                    style.gridPaint);
            marker += offset;
        }

        canvas.drawLine(getInnerChartRight(), getInnerChartTop(), getInnerChartRight(),
                getInnerChartBottom(), style.gridPaint);
    }


    /**
     * Draw horizontal lines of Grid.
     *
     * @param canvas Canvas to draw on.
     */
    private void drawHorizontalGrid(Canvas canvas) {

        final float offset = (getInnerChartBottom() - getInnerChartTop()) / style.gridRows;
        float marker = getInnerChartTop();
        while (marker < getInnerChartBottom()) {
            canvas.drawLine(getInnerChartLeft(), marker, getInnerChartRight(), marker,
                    style.gridPaint);
            marker += offset;
        }

        if (!style.hasXAxis)
            canvas.drawLine(getInnerChartLeft(), getInnerChartBottom(), getInnerChartRight(),
                    getInnerChartBottom(), style.gridPaint);
    }


    /**
     * Get orientation of chart.
     *
     * @return Object of type {@link com.aiqfome.aiqchart.view.ChartView.Orientation}
     * defining an horizontal or vertical orientation.
     * Orientation.HORIZONTAL | Orientation.VERTICAL
     */
    public Orientation getOrientation() {

        return mOrientation;
    }

    /**
     * Sets the chart's orientation.
     *
     * @param orien Orientation.HORIZONTAL | Orientation.VERTICAL
     */
    void setOrientation(@NonNull Orientation orien) {

        mOrientation = checkNotNull(orien);
        if (mOrientation == Orientation.VERTICAL) {
            yRndr.setHandleValues(true);
        } else {
            xRndr.setHandleValues(true);
        }
    }

    /**
     * Inner Chart refers only to the area where chart data will be draw,
     * excluding labels, axis, etc.
     *
     * @return Position of the inner bottom side of the chart
     */
    public float getInnerChartBottom() {

        return yRndr.getInnerChartBottom();
    }

    /**
     * Inner Chart refers only to the area where chart data will be draw,
     * excluding labels, axis, etc.
     *
     * @return Position of the inner left side of the chart
     */
    public float getInnerChartLeft() {

        return xRndr.getInnerChartLeft();
    }

    /**
     * Inner Chart refers only to the area where chart data will be draw,
     * excluding labels, axis, etc.
     *
     * @return Position of the inner right side of the chart
     */
    public float getInnerChartRight() {

        return xRndr.getInnerChartRight();
    }

    /**
     * Inner Chart refers only to the area where chart data will be draw,
     * excluding labels, axis, etc.
     *
     * @return Position of the inner top side of the chart
     */
    public float getInnerChartTop() {

        return yRndr.getInnerChartTop();
    }

    /**
     * Returns the position of 0 value on chart.
     *
     * @return Position of 0 value on chart
     */
    public float getZeroPosition() {

        AxisRenderer rndr;
        if (mOrientation == Orientation.VERTICAL) rndr = yRndr;
        else rndr = xRndr;

        if (rndr.getBorderMinimumValue() > 0)
            return rndr.parsePos(0, rndr.getBorderMinimumValue());
        else if (rndr.getBorderMaximumValue() < 0)
            return rndr.parsePos(0, rndr.getBorderMaximumValue());
        return rndr.parsePos(0, 0);
    }

    /**
     * Get the step used between Y values.
     *
     * @return step
     */
    float getStep() {

        if (mOrientation == Orientation.VERTICAL) return yRndr.getStep();
        else return xRndr.getStep();
    }

    /**
     * A step is seen as the step to be defined between 2 labels.
     * As an example a step of 2 with a max label value of 6 will end
     * up with {0, 2, 4, 6} as labels.
     *
     * @param step (real) value distance from every label
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setStep(int step) {

        if (step <= 0) throw new IllegalArgumentException("Step can't be lower or equal to 0");

        if (mOrientation == Orientation.VERTICAL) yRndr.setStep(step);
        else xRndr.setStep(step);

        return this;
    }

    /**
     * Get chart's border spacing.
     *
     * @return spacing
     */
    float getBorderSpacing() {

        return style.axisBorderSpacing;
    }

    /**
     * @param spacing Spacing between left/right of the chart and the first/last label
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setBorderSpacing(int spacing) {

        style.axisBorderSpacing = spacing;
        return this;
    }

    /**
     * Get the whole data owned by the chart.
     *
     * @return List of {@link com.aiqfome.aiqchart.model.ChartSet} owned by the chart
     */
    public ArrayList<ChartSet> getData() {

        return data;
    }

    /**
     * Get the list of {@link Rect} associated to each entry of a ChartSet.
     *
     * @param index {@link com.aiqfome.aiqchart.model.ChartSet} index
     * @return The list of {@link Rect} for the specified dataset
     */
    public ArrayList<Rect> getEntriesArea(int index) {

        checkPositionIndex(index, mRegions.size());
        ArrayList<Rect> result = new ArrayList<>(mRegions.get(index).size());
        for (Region r : mRegions.get(index))
            result.add(getEntryRect(r));

        return result;
    }

    /**
     * Get the area, {@link Rect}, of an entry from the entry's {@link
     * Region}
     *
     * @param region Region covering {@link ChartEntry} area
     * @return {@link Rect} specifying the area of an {@link ChartEntry}
     */
    Rect getEntryRect(@NonNull Region region) {
        checkNotNull(region);
        // Subtract the view left/top padding to correct position
        return new Rect(region.getBounds().left - getPaddingLeft(),
                region.getBounds().top - getPaddingTop(), region.getBounds().right - getPaddingLeft(),
                region.getBounds().bottom - getPaddingTop());
    }

    /**
     * Get the current {@link com.aiqfome.aiqchart.animation.Animation}
     * held by {@link com.aiqfome.aiqchart.view.ChartView}.
     * Useful, for instance, to define another endAction.
     *
     * @return Current {@link com.aiqfome.aiqchart.animation.Animation}
     */
    public Animation getChartAnimation() {

        return mAnim;
    }

    /**
     * Show/Hide Y labels and respective axis.
     *
     * @param position NONE - No labels
     *                 OUTSIDE - Labels will be positioned outside the chart
     *                 INSIDE - Labels will be positioned inside the chart
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setYLabels(@NonNull YRenderer.LabelPosition position) {

        style.yLabelsPositioning = checkNotNull(position);
        return this;
    }

    /**
     * Show/Hide X labels and respective axis.
     *
     * @param position NONE - No labels
     *                 OUTSIDE - Labels will be positioned outside the chart
     *                 INSIDE - Labels will be positioned inside the chart
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setXLabels(@NonNull XRenderer.LabelPosition position) {

        style.xLabelsPositioning = checkNotNull(position);
        return this;
    }

    /**
     * Set the format to be added to Y labels.
     *
     * @param format Format to be applied
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setLabelsFormat(@NonNull DecimalFormat format) {

        style.labelsFormat = checkNotNull(format);
        return this;
    }

    /**
     * Set color to be used in labels.
     *
     * @param color Color to be applied to labels
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setLabelsColor(@ColorInt int color) {

        style.labelsColor = color;
        return this;
    }

    /**
     * Set size of font to be used in labels.
     *
     * @param size Font size to be applied to labels
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setFontSize(@IntRange(from = 0) int size) {

        style.fontSize = size;
        return this;
    }

    /**
     * Set typeface to be used in labels.
     *
     * @param typeface To be applied to labels
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setTypeface(@NonNull Typeface typeface) {

        style.typeface = checkNotNull(typeface);
        return this;
    }

    /**
     * Show/Hide X axis.
     *
     * @param bool If true axis won't be visible
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setXAxis(boolean bool) {

        style.hasXAxis = bool;
        return this;
    }

    /**
     * Show/Hide Y axis.
     *
     * @param bool If true axis won't be visible
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setYAxis(boolean bool) {

        style.hasYAxis = bool;
        return this;
    }

    /**
     * A step is seen as the step to be defined between 2 labels. As an
     * example a step of 2 with a maxAxisValue of 6 will end up with
     * {0, 2, 4, 6} as labels.
     *
     * @param minValue The minimum value that Y axis will have as a label
     * @param maxValue The maximum value that Y axis will have as a label
     * @param step     (real) value distance from every label
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setAxisBorderValues(float minValue, float maxValue, float step) {

        if (mOrientation == Orientation.VERTICAL) yRndr.setBorderValues(minValue, maxValue, step);
        else xRndr.setBorderValues(minValue, maxValue, step);

        return this;
    }

    /**
     * @param minValue The minimum value that Y axis will have as a label
     * @param maxValue The maximum value that Y axis will have as a label
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setAxisBorderValues(float minValue, float maxValue) {

        if (mOrientation == Orientation.VERTICAL) yRndr.setBorderValues(minValue, maxValue);
        else xRndr.setBorderValues(minValue, maxValue);

        return this;
    }

    /**
     * Define the thickness of the axis.
     *
     * @param thickness size of the thickness
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setAxisThickness(@FloatRange(from = 0.f) float thickness) {

        style.axisThickness = thickness;
        return this;
    }

    /**
     * Define the color of the axis.
     *
     * @param color color of the axis
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setAxisColor(@ColorInt int color) {

        style.axisColor = color;
        return this;
    }

    /**
     * Register a listener to be called when an {@link ChartEntry} is clicked.
     *
     * @param listener Listener to be used for callback.
     */
    public void setOnEntryClickListener(OnEntryClickListener listener) {

        this.mEntryListener = listener;
    }

    /**
     * Register a listener to be called when the {@link ChartView} is clicked.
     *
     * @param listener Listener to be used for callback.
     */
    @Override
    public void setOnClickListener(OnClickListener listener) {

        this.mChartListener = listener;
    }

    /**
     * @param spacing Spacing between top of the chart and the first label
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setTopSpacing(int spacing) {

        style.axisTopSpacing = spacing;
        return this;
    }


    /**
     * Apply grid to chart.
     *
     * @param rows    Grid's number of rows
     * @param columns Grid's number of columns
     * @param paint   The Paint instance that will be used to draw the grid
     *                If null the grid won't be drawn
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setGrid(@IntRange(from = 0) int rows, @IntRange(from = 0) int columns,
                             @NonNull Paint paint) {

        if (rows < 0 || columns < 0)
            throw new IllegalArgumentException("Number of rows/columns can't be smaller than 0.");

        style.gridRows = rows;
        style.gridColumns = columns;
        style.gridPaint = checkNotNull(paint);
        return this;
    }


    /**
     * Display a value threshold either in a form of line or band.
     * In order to produce a line, the start and end value will be equal.
     *
     * @param startValue Threshold value.
     * @param endValue   Threshold value.
     * @param paint      The Paint instance that will be used to draw the grid
     *                   If null the grid won't be drawn
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setValueThreshold(float startValue, float endValue, @NonNull Paint paint) {

        mThresholdStartValues.add(startValue);
        mThresholdEndValues.add(endValue);
        style.valueThresPaint = checkNotNull(paint);
        return this;
    }


    /**
     * Display a value threshold either in a form of line or band.
     * In order to produce a line, the start and end value will be equal.
     *
     * @param startValues Threshold values.
     * @param endValues   Threshold values.
     * @param paint       The Paint instance that will be used to draw the grid
     *                    If null the grid won't be drawn
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setValueThreshold(@NonNull float[] startValues, @NonNull float[] endValues, @NonNull Paint paint) {

        checkNotNull(startValues);
        checkNotNull(endValues);

        mThresholdStartValues.clear();
        mThresholdEndValues.clear();
        for (int i = 0; i < startValues.length; i++) {
            mThresholdStartValues.add(startValues[i]);
            mThresholdEndValues.add(endValues[i]);
        }
        style.valueThresPaint = checkNotNull(paint);
        return this;
    }


    /**
     * Display a label threshold either in a form of line or band.
     * In order to produce a line, the start and end label will be equal.
     *
     * @param startLabel Threshold start label index.
     * @param endLabel   Threshold end label index.
     * @param paint      The Paint instance that will be used to draw the grid
     *                   If null the grid won't be drawn
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setLabelThreshold(int startLabel, int endLabel, @NonNull Paint paint) {

        mThresholdStartLabels.add(startLabel);
        mThresholdEndLabels.add(endLabel);
        style.labelThresPaint = checkNotNull(paint);
        return this;
    }


    /**
     * Display a label threshold either in a form of line or band.
     * In order to produce a line, the start and end label will be equal.
     *
     * @param startLabels Threshold start label index.
     * @param endLabels   Threshold end label index.
     * @param paint       The Paint instance that will be used to draw the grid
     *                    If null the grid won't be drawn
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setLabelThreshold(@NonNull int[] startLabels, @NonNull int[] endLabels, @NonNull Paint paint) {

        checkNotNull(startLabels);
        checkNotNull(endLabels);

        mThresholdStartLabels.clear();
        mThresholdEndLabels.clear();
        for (int i = 0; i < startLabels.length; i++) {
            mThresholdStartLabels.add(startLabels[i]);
            mThresholdEndLabels.add(endLabels[i]);
        }
        style.labelThresPaint = checkNotNull(paint);
        return this;
    }


    /**
     * Set spacing between Labels and Axis. Will be applied to both X and Y.
     *
     * @param spacing Spacing between labels and axis
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setAxisLabelsSpacing(int spacing) {

        style.axisLabelsSpacing = spacing;
        return this;
    }


    /**
     * Mandatory horizontal border when necessary (ex: BarCharts)
     * Sets the attribute depending on the chart's orientation.
     * e.g. If orientation is VERTICAL it means that this attribute must be handled
     * by horizontal axis and not the vertical axis.
     */
    void setMandatoryBorderSpacing() {

        if (mOrientation == Orientation.VERTICAL) xRndr.setMandatoryBorderSpacing(true);
        else yRndr.setMandatoryBorderSpacing(true);
    }


    /**
     * Set the {@link Tooltip} object which will be used to create chart tooltips.
     *
     * @param tooltip {@link Tooltip} object in order to produce chart tooltips
     * @return {@link com.aiqfome.aiqchart.view.ChartView} self-reference.
     */
    public ChartView setTooltips(Tooltip tooltip) {

        mTooltip = tooltip;
        return this;
    }


    /**
     * Manually set chart clickable regions.
     * Normally the system sets the regions matching the entries position in the screen.
     * See method defineRegions for more information.
     *
     * @param regions Clickable regions where touch event will be detected.
     */
    void setClickableRegions(ArrayList<ArrayList<Region>> regions) {

        mRegions = regions;
    }


    /**
     * Applies an alpha to the paint object.
     *
     * @param paint  {@link Paint} object to apply alpha
     * @param alpha  Alpha value (opacity)
     * @param dx     Dx
     * @param dy     Dy
     * @param radius Radius
     * @param color  Color
     */
    protected void applyShadow(Paint paint, float alpha, float dx, float dy, float radius,
                               int[] color) {

        paint.setAlpha((int) (alpha * style.FULL_ALPHA));
        paint.setShadowLayer(radius, dx, dy,
                Color.argb(((int) (alpha * style.FULL_ALPHA) < color[0]) ? (int) (alpha * style.FULL_ALPHA) : color[0],
                        color[1], color[2], color[3]));
    }


    public enum Orientation {
        /**
         * Chart horizontal orientation.
         */
        HORIZONTAL,
        /**
         * Chart vertical orientation.
         */
        VERTICAL
    }


    /**
     * Class responsible to style the Graph!
     * Can be instantiated with or without attributes.
     */
    public class Style {

        static final int FULL_ALPHA = 255;

        private static final int DEFAULT_COLOR = Color.BLACK;

        private static final int DEFAULT_GRID_OFF = 0;

        /**
         * Chart
         */
        private Paint chartPaint;

        /**
         * Axis
         */
        private boolean hasXAxis;

        private boolean hasYAxis;

        private float axisThickness;

        private int axisColor;

        /**
         * Distance between axis and label
         */
        private int axisLabelsSpacing;

        /**
         * Spacing between axis labels and chart sides
         */
        private int axisBorderSpacing;

        /**
         * Spacing between chart top and axis label
         */
        private int axisTopSpacing;

        /**
         * Grid
         */
        private Paint gridPaint;

        /**
         * Threshold
         **/
        private Paint labelThresPaint;

        private Paint valueThresPaint;

        /**
         * Font
         */
        private AxisRenderer.LabelPosition xLabelsPositioning;

        private AxisRenderer.LabelPosition yLabelsPositioning;

        private Paint labelsPaint;

        private int labelsColor;

        private float fontSize;

        private Typeface typeface;

        /**
         * Height of the text based on the font style defined.
         * Includes uppercase height and bottom padding of special
         * lowercase letter such as g, p, etc.
         */
        private int fontMaxHeight;

        private int gridRows;

        private int gridColumns;

        /**
         * Labels Metric to draw together with labels.
         */
        private DecimalFormat labelsFormat;


        Style(Context context) {

            axisColor = DEFAULT_COLOR;
            axisThickness = context.getResources().getDimension(R.dimen.grid_thickness);
            hasXAxis = true;
            hasYAxis = true;

            xLabelsPositioning = AxisRenderer.LabelPosition.OUTSIDE;
            yLabelsPositioning = AxisRenderer.LabelPosition.OUTSIDE;
            labelsColor = DEFAULT_COLOR;
            fontSize = context.getResources().getDimension(R.dimen.font_size);

            axisLabelsSpacing = context.getResources().getDimensionPixelSize(R.dimen.axis_labels_spacing);
            axisBorderSpacing = context.getResources().getDimensionPixelSize(R.dimen.axis_border_spacing);
            axisTopSpacing = context.getResources().getDimensionPixelSize(R.dimen.axis_top_spacing);

            gridRows = DEFAULT_GRID_OFF;
            gridColumns = DEFAULT_GRID_OFF;

            labelsFormat = new DecimalFormat();
        }


        Style(Context context, AttributeSet attrs) {

            TypedArray arr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChartAttrs, 0, 0);

            hasXAxis = arr.getBoolean(R.styleable.ChartAttrs_chart_axis, true);
            hasYAxis = arr.getBoolean(R.styleable.ChartAttrs_chart_axis, true);
            axisColor = arr.getColor(R.styleable.ChartAttrs_chart_axisColor, DEFAULT_COLOR);
            axisThickness = arr.getDimension(R.styleable.ChartAttrs_chart_axisThickness,
                    context.getResources().getDimension(R.dimen.axis_thickness));

            switch (arr.getInt(R.styleable.ChartAttrs_chart_labels, 0)) {
                case 1:
                    xLabelsPositioning = AxisRenderer.LabelPosition.INSIDE;
                    yLabelsPositioning = AxisRenderer.LabelPosition.INSIDE;
                    break;
                case 2:
                    xLabelsPositioning = AxisRenderer.LabelPosition.NONE;
                    yLabelsPositioning = AxisRenderer.LabelPosition.NONE;
                    break;
                default:
                    xLabelsPositioning = AxisRenderer.LabelPosition.OUTSIDE;
                    yLabelsPositioning = AxisRenderer.LabelPosition.OUTSIDE;
                    break;
            }

            labelsColor = arr.getColor(R.styleable.ChartAttrs_chart_labelColor, DEFAULT_COLOR);

            fontSize = arr.getDimension(R.styleable.ChartAttrs_chart_fontSize,
                    context.getResources().getDimension(R.dimen.font_size));

            String typefaceName = arr.getString(R.styleable.ChartAttrs_chart_typeface);
            if (typefaceName != null) typeface = Typeface.createFromAsset(getResources()
                    .getAssets(), typefaceName);

            axisLabelsSpacing = arr.getDimensionPixelSize(R.styleable.ChartAttrs_chart_axisLabelsSpacing,
                    context.getResources().getDimensionPixelSize(R.dimen.axis_labels_spacing));
            axisBorderSpacing = arr.getDimensionPixelSize(R.styleable.ChartAttrs_chart_axisBorderSpacing,
                    context.getResources().getDimensionPixelSize(R.dimen.axis_border_spacing));
            axisTopSpacing = arr.getDimensionPixelSize(R.styleable.ChartAttrs_chart_axisTopSpacing,
                    context.getResources().getDimensionPixelSize(R.dimen.axis_top_spacing));

            gridRows = DEFAULT_GRID_OFF;
            gridColumns = DEFAULT_GRID_OFF;

            labelsFormat = new DecimalFormat();
        }

        private void init() {

            chartPaint = new Paint();
            chartPaint.setColor(axisColor);
            chartPaint.setStyle(Paint.Style.STROKE);
            chartPaint.setStrokeWidth(axisThickness);
            chartPaint.setAntiAlias(true);

            labelsPaint = new Paint();
            labelsPaint.setColor(labelsColor);
            labelsPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            labelsPaint.setAntiAlias(true);
            labelsPaint.setTextSize(fontSize);
            labelsPaint.setTypeface(typeface);

            fontMaxHeight = (int) (style.labelsPaint.descent() - style.labelsPaint.ascent());
        }

        private void clean() {

            chartPaint = null;
            labelsPaint = null;
        }


        /**
         * Get label's height.
         *
         * @param text Label to measure
         * @return Height of label
         */
        public int getLabelHeight(String text) {

            final Rect rect = new Rect();
            style.labelsPaint.getTextBounds(text, 0, text.length(), rect);
            return rect.height();
        }

        public Paint getChartPaint() {
            return chartPaint;
        }

        public float getAxisThickness() {
            return axisThickness;
        }

        /**
         * If axis x (not the labels) should be displayed.
         *
         * @return True if axis x is displayed
         */
        public boolean hasXAxis() {
            return hasXAxis;
        }

        /**
         * If axis y (not the labels) should be displayed.
         *
         * @return True if axis y is displayed
         */
        public boolean hasYAxis() {
            return hasYAxis;
        }

        public Paint getLabelsPaint() {
            return labelsPaint;
        }

        public int getFontMaxHeight() {
            return fontMaxHeight;
        }

        public AxisRenderer.LabelPosition getXLabelsPositioning() {
            return xLabelsPositioning;
        }

        public AxisRenderer.LabelPosition getYLabelsPositioning() {
            return yLabelsPositioning;
        }

        public int getAxisLabelsSpacing() {
            return axisLabelsSpacing;
        }

        public int getAxisBorderSpacing() {
            return axisBorderSpacing;
        }

        public int getAxisTopSpacing() {
            return axisTopSpacing;
        }

        public DecimalFormat getLabelsFormat() {
            return labelsFormat;
        }

        private boolean hasHorizontalGrid() {
            return gridRows > 0;
        }

        private boolean hasVerticalGrid() {
            return gridColumns > 0;
        }

    }


    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent ev) {

            if (mEntryListener != null || mTooltip != null) { // Check if tap on any entry
                int nSets = mRegions.size();
                int nEntries = mRegions.get(0).size();
                for (int i = 0; i < nSets; i++)
                    for (int j = 0; j < nEntries; j++)
                        if (mRegions.get(i).get(j).contains((int) ev.getX(), (int) ev.getY())) {
                            if (mEntryListener != null)  // Trigger entry callback
                                mEntryListener.onClick(i, j, getEntryRect(mRegions.get(i).get(j)));
                            if (mTooltip != null)  // Toggle tooltip
                                toggleTooltip(getEntryRect(mRegions.get(i).get(j)), data.get(i).getValue(j));
                            return true;
                        }
            }

            if (mChartListener != null) mChartListener.onClick(ChartView.this);
            if (mTooltip != null && mTooltip.on()) dismissTooltip(mTooltip);
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {

            return true;
        }

    }

}
