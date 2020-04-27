/*
 * Copyright 2015 Diogo Bernardino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.db.chart.model;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import static com.db.chart.util.Preconditions.checkNotNull;


/**
 * Generic Data model of a {@link com.db.chart.view.ChartView} entry
 */
public abstract class ChartEntry implements Comparable<ChartEntry> {

    /**
     * Default bar color
     */
    private static final int DEFAULT_COLOR = -16777216;

    boolean isVisible;

    /**
     * Input from user
     */
    private final String mLabel;

    /**
     * Defines if entry is visible
     */
    private float mValue;

    /**
     * Display coordinates
     */
    private float mX;

    private float mY;

    /**
     * Bar color
     */
    private int mColor;

    /**
     * Shadow variables
     */
    private float mShadowRadius;

    private float mShadowDx;

    private float mShadowDy;

    private int[] mShadowColor;


    ChartEntry(String label, float value) {

        mLabel = label;
        mValue = value;

        mColor = DEFAULT_COLOR;

        mShadowRadius = 0;
        mShadowDx = 0;
        mShadowDy = 0;
        mShadowColor = new int[4];
    }


    /**
     * If entry is currently visible (displayed).
     *
     * @return True if entry is visible in display, False otherwise.
     */
    public boolean isVisible() {

        return isVisible;
    }

    /**
     * Define whether this entry will be drawn or not.
     *
     * @param visible True if entry should be displayed.
     */
    public void setVisible(boolean visible) {

        isVisible = visible;
    }


    /**
     * @return True if entry has shadow defined, False otherwise.
     */
    public boolean hasShadow() {

        return mShadowRadius != 0;
    }


    /**
     * Animate entry between two positions.
     *
     * @param x0 Start x position
     * @param y0 Start y position
     * @param x1 End x position
     * @param y1 End y position
     * @return {@link ValueAnimator} object responsible to handle animation.
     */
    public ValueAnimator animateXY(float x0, float y0, float x1, float y1) {

        final ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(
                PropertyValuesHolder.ofFloat("x", x0, x1),
                PropertyValuesHolder.ofFloat("y", y0, y1));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mX = (float) animation.getAnimatedValue("x");
                mY = (float) animation.getAnimatedValue("y");
            }
        });
        mX = x0;
        mY = y0;
        return animator;
    }


    /**
     * Animate entry color.
     *
     * @param color0 Start color resource.
     * @param color1 End color resource.
     * @return {@link ValueAnimator} responsible to handle animation.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ValueAnimator animateColor(int color0, int color1) {

        final ValueAnimator animator = ValueAnimator.ofArgb(color0, color1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mColor = (int) animation.getAnimatedValue();
            }
        });
        mColor = color0;
        return animator;
    }


    /*
    * --------
    * Getters
    * --------
    */

    public String getLabel() {

        return mLabel;
    }

    public float getValue() {

        return mValue;
    }

    /**
     * Set new entry value.
     *
     * @param value New value
     */
    public void setValue(float value) {

        mValue = value;
    }

    public float getX() {

        return mX;
    }

    public float getY() {

        return mY;
    }

    public int getColor() {

        return mColor;
    }

    /**
     * Define the color of the entry.
     *
     * @param color Color to be set.
     */
    public void setColor(@ColorInt int color) {

        isVisible = true;
        mColor = color;
    }

    public float getShadowRadius() {

        return mShadowRadius;
    }

    public float getShadowDx() {

        return mShadowDx;
    }

    public float getShadowDy() {

        return mShadowDy;
    }

    public int[] getShadowColor() {

        return mShadowColor;
    }

	
	/*
     * --------
	 * Setters
	 * --------
	 */

    /**
     * Set the parsed display coordinates.
     *
     * @param x display x coordinate.
     * @param y display y coordinate.
     */
    public void setCoordinates(float x, float y) {

        mX = x;
        mY = y;
    }


    /**
     * Define entry shadow
     *
     * @param radius Radius
     * @param dx     Dx
     * @param dy     Dy
     * @param color  Color
     */
    public void setShadow(float radius, float dx, float dy, @ColorInt int color) {

        mShadowRadius = radius;
        mShadowDx = dx;
        mShadowDy = dy;
        mShadowColor[0] = Color.alpha(color);
        mShadowColor[1] = Color.red(color);
        mShadowColor[2] = Color.blue(color);
        mShadowColor[3] = Color.green(color);
    }


    /**
     * Convert object into a printable text.
     *
     * @return Object's text description
     */
    public String toString() {

        return "Label=" + mLabel + " \n" + "Value=" + mValue + "\n"
                + "X = " + mX + "\n" + "Y = " + mY;
    }


    /**
     * Compare given entry with itself.
     *
     * @param other {@link ChartEntry} object to be compared with.
     * @return the value {@code 0} if {@code f1} is
     * numerically equal to {@code f2}; a value less than
     * {@code 0} if {@code f1} is numerically less than
     * {@code f2}; and a value greater than {@code 0}
     * if {@code f1} is numerically greater than
     * {@code f2}.
     */
    public int compareTo(@NonNull ChartEntry other) {
        checkNotNull(other);
        return Float.compare(this.getValue(), other.getValue());
    }

}
