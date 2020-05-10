package com.aiqfome.aiqchart.tooltip;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aiqfome.aiqchart.listener.OnTooltipEventListener;

import java.text.DecimalFormat;


/**
 * Class representing chart's tooltips. It works basically as a wrapper.
 */
public class Tooltip extends RelativeLayout {

    private Alignment mVerticalAlignment = Alignment.CENTER;

    private Alignment mHorizontalAlignment = Alignment.CENTER;

    private TextView mTooltipValue;

    private OnTooltipEventListener mTooltipEventListener;

    private ObjectAnimator mEnterAnimator;

    private ObjectAnimator mExitAnimator;

    private int mWidth;

    private int mHeight;

    private int mLeftMargin;

    private int mTopMargin;

    private int mRightMargin;

    private int mBottomMargin;

    private boolean mOn;

    private DecimalFormat mValueFormat;


    public Tooltip(Context context) {

        super(context);
        init();
    }


    public Tooltip(Context context, int layoutId) {

        super(context);
        init();

        View layoutParent = inflate(getContext(), layoutId, null);
        layoutParent.setLayoutParams(
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(layoutParent);
    }


    public Tooltip(Context context, int layoutId, int valueId) {

        super(context);
        init();

        View layoutParent = inflate(getContext(), layoutId, null);
        layoutParent.setLayoutParams(
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(layoutParent);
        mTooltipValue = (TextView) findViewById(valueId);
    }

    private void init() {

        mWidth = -1;
        mHeight = -1;
        mLeftMargin = 0;
        mTopMargin = 0;
        mRightMargin = 0;
        mBottomMargin = 0;
        mOn = false;
        mValueFormat = new DecimalFormat();
    }


    /**
     * Method called by ChartView before displaying the
     * tooltip in order to set its layout parameters.
     *
     * @param rect  {@link Rect} covering the are of the
     *              clicked {@link com.aiqfome.aiqchart.model.ChartEntry}.
     * @param value Value of the entry.
     */
    public void prepare(Rect rect, float value) {

        // If no previous dimensions defined, the size of the area of the entry will be used.
        int width = (mWidth == -1) ? rect.width() : mWidth;
        int height = (mHeight == -1) ? rect.height() : mHeight;

        LayoutParams layoutParams = new LayoutParams(width, height);

        // Adjust left coordinate of the tooltip based on the Alignment defined
        if (mHorizontalAlignment == Alignment.RIGHT_LEFT)
            layoutParams.leftMargin = rect.left - width - mRightMargin;
        if (mHorizontalAlignment == Alignment.LEFT_LEFT)
            layoutParams.leftMargin = rect.left + mLeftMargin;
        if (mHorizontalAlignment == Alignment.CENTER)
            layoutParams.leftMargin = rect.centerX() - width / 2;
        if (mHorizontalAlignment == Alignment.RIGHT_RIGHT)
            layoutParams.leftMargin = rect.right - width - mRightMargin;
        if (mHorizontalAlignment == Alignment.LEFT_RIGHT)
            layoutParams.leftMargin = rect.right + mLeftMargin;

        // Adjust top coordinate of tooltip based on the Alignment defined
        if (mVerticalAlignment == Alignment.BOTTOM_TOP)
            layoutParams.topMargin = rect.top - height - mBottomMargin;
        else if (mVerticalAlignment == Alignment.TOP_TOP)
            layoutParams.topMargin = rect.top + mTopMargin;
        else if (mVerticalAlignment == Alignment.CENTER)
            layoutParams.topMargin = rect.centerY() - height / 2;
        else if (mVerticalAlignment == Alignment.BOTTOM_BOTTOM)
            layoutParams.topMargin = rect.bottom - height - mBottomMargin;
        else if (mVerticalAlignment == Alignment.TOP_BOTTOM)
            layoutParams.topMargin = rect.bottom + mTopMargin;

        setLayoutParams(layoutParams);

        if (mTooltipValue != null) mTooltipValue.setText(mValueFormat.format(value));
    }


    /**
     * Corrects the position of a tooltip and forces it to
     * be within {@link com.aiqfome.aiqchart.view.ChartView}.
     *
     * @param left   left coordinate of {@link com.aiqfome.aiqchart.view.ChartView}
     * @param top    top coordinate of {@link com.aiqfome.aiqchart.view.ChartView}
     * @param right  right coordinate of {@link com.aiqfome.aiqchart.view.ChartView}
     * @param bottom bottom coordinate of {@link com.aiqfome.aiqchart.view.ChartView}
     */
    public void correctPosition(int left, int top, int right, int bottom) {

        final LayoutParams layoutParams = (LayoutParams) getLayoutParams();

        if (layoutParams.leftMargin < left) layoutParams.leftMargin = left;
        if (layoutParams.topMargin < top) layoutParams.topMargin = top;
        if (layoutParams.leftMargin + layoutParams.width > right)
            layoutParams.leftMargin = right - layoutParams.width;
        if (layoutParams.topMargin + layoutParams.height > bottom)
            layoutParams.topMargin = bottom - layoutParams.height;
        setLayoutParams(layoutParams);
    }


    /**
     * Starts enter animation.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void animateEnter() {

        mEnterAnimator.start();
    }


    /**
     * Start exit animation.
     *
     * @param endAction Action to be executed at the end of the animation.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void animateExit(final Runnable endAction) {

        mExitAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                endAction.run();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        mExitAnimator.start();
    }


    /**
     * Check if enter animation of type {@link ObjectAnimator} exists.
     *
     * @return true if {@link Tooltip} has enter animation defined.
     */
    public boolean hasEnterAnimation() {

        return mEnterAnimator != null;
    }


    /**
     * Check if exit animation of type {@link ObjectAnimator} exists.
     *
     * @return true if {@link Tooltip} has exit animation define.
     */
    public boolean hasExitAnimation() {

        return mExitAnimator != null;
    }


    /**
     * Maintain information about whether the tooltip is being displayed or not.
     *
     * @return true if {@link Tooltip} is currently displayed.
     */
    public boolean on() {

        return mOn;
    }


    /**
     * Define the horizontal alignment of tooltip wrt entry's position.
     * Ex. Alignment.LEFT_RIGHT means that the left side of the tooltip
     * will be aligned with the right side of the entry.
     *
     * @param alignment horizontal alignment wrt entry's position.
     * @return {@link Tooltip} self-reference.
     */
    public Tooltip setHorizontalAlignment(Alignment alignment) {

        mHorizontalAlignment = alignment;
        return this;
    }


    /**
     * Define the vertical alignment of tooltip wrt entry's position.
     * Ex. Alignment.TOP_TOP means that the top side of the tooltip
     * will be aligned with the top side of the entry.
     *
     * @param alignment vertical alignment wrt entry's position.
     * @return {@link Tooltip} self-reference.
     */
    public Tooltip setVerticalAlignment(Alignment alignment) {

        mVerticalAlignment = alignment;
        return this;
    }


    /**
     * Set the dimensions of the tooltip.
     *
     * @param width  width dimension
     * @param height height dimension
     * @return {@link Tooltip} self-reference.
     */
    public Tooltip setDimensions(int width, int height) {

        mWidth = width;
        mHeight = height;
        return this;
    }


    /**
     * Set the margins of the tooltip wrt entry.
     *
     * @param left   left margin dimension.
     * @param top    top margin dimension.
     * @param right  right margin dimension.
     * @param bottom bottom margin dimension.
     * @return {@link Tooltip} self-reference.
     */
    public Tooltip setMargins(int left, int top, int right, int bottom) {

        mLeftMargin = left;
        mTopMargin = top;
        mRightMargin = right;
        mBottomMargin = bottom;
        return this;
    }


    /**
     * If the tooltip is being displayed.
     *
     * @param on True if displayed, False if not.
     */
    public void setOn(boolean on) {

        mOn = on;
    }


    /**
     * Set the format to be applied to tooltip's value.
     *
     * @param format value format to be used once the tooltip is displayed.
     * @return {@link Tooltip} self-reference.
     */
    public Tooltip setValueFormat(DecimalFormat format) {

        mValueFormat = format;
        return this;
    }


    /**
     * @param values Property values to use in animation
     * @return {@link ObjectAnimator} responsible to handle animation
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ObjectAnimator setEnterAnimation(PropertyValuesHolder... values) {

        for (PropertyValuesHolder value : values) {

            if (value.getPropertyName().equals("alpha")) setAlpha(0);
            if (value.getPropertyName().equals("rotation")) setRotation(0);
            if (value.getPropertyName().equals("rotationX")) setRotationX(0);
            if (value.getPropertyName().equals("rotationY")) setRotationY(0);
            if (value.getPropertyName().equals("translationX")) setTranslationX(0);
            if (value.getPropertyName().equals("translationY")) setTranslationY(0);
            if (value.getPropertyName().equals("scaleX")) setScaleX(0);
            if (value.getPropertyName().equals("scaleY")) setScaleY(0);
        }
        return mEnterAnimator = ObjectAnimator.ofPropertyValuesHolder(this, values);
    }


    /**
     * @param values Property values to use in animation
     * @return {@link ObjectAnimator} responsible to handle animation
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ObjectAnimator setExitAnimation(PropertyValuesHolder... values) {

        return mExitAnimator = ObjectAnimator.ofPropertyValuesHolder(this, values);
    }


    public enum Alignment {
        /**
         * Tooltip bottom aligned at top.
         */
        BOTTOM_TOP,
        /**
         * Tooltip top aligned at bottom.
         */
        TOP_BOTTOM,
        /**
         * Tooltip top aligned at top.
         */
        TOP_TOP,
        /**
         * Tooltip center aligned at center.
         */
        CENTER,
        /**
         * Tooltip bottom aligned at bottom.
         */
        BOTTOM_BOTTOM,
        /**
         * Tooltip left aligned at left.
         */
        LEFT_LEFT,
        /**
         * Tooltip right aligned at left.
         */
        RIGHT_LEFT,
        /**
         * Tooltip right aligned at right.
         */
        RIGHT_RIGHT,
        /**
         * Tooltip left aligned at right.
         */
        LEFT_RIGHT
    }

}
