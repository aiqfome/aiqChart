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

import android.animation.ValueAnimator;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;

import static com.db.chart.util.Preconditions.checkNotNull;
import static com.db.chart.util.Preconditions.checkPositionIndex;


/**
 * Data model containing {@link ChartEntry} elements to be used by {@link
 * com.db.chart.view.ChartView}.
 */
public abstract class ChartSet {

    private static final String TAG = "chart.model.ChartSet";


    /**
     * Set with entries
     */
    private final ArrayList<ChartEntry> mEntries;


    /**
     * Paint alpha value from 0 to 1
     */
    private float mAlpha;


    /**
     * Whether the set will be visible or not
     */
    private boolean mIsVisible;


    ChartSet() {

        mEntries = new ArrayList<>();
        mAlpha = 1;
        mIsVisible = false;
    }


    /**
     * Add new entry to set.
     *
     * @param e New entry.
     */
    void addEntry(@NonNull ChartEntry e) {

        mEntries.add(checkNotNull(e));
    }


    /**
     * Updates set values.
     *
     * @param newValues New updated values to override current.
     */
    public void updateValues(@NonNull float[] newValues) {

        checkNotNull(newValues);
        if (newValues.length != size()) throw new IllegalArgumentException(
                "New set values given doesn't match previous " + "number of entries.");

        int nEntries = size();
        for (int i = 0; i < nEntries; i++)
            setValue(i, newValues[i]);
    }


    /**
     * Animate set's alpha value.
     *
     * @param from Start alpha value.
     * @param to   End alpha value.
     * @return {@link ValueAnimator} object responsible to handle animation.
     */
    public ValueAnimator animateAlpha(float from, float to) {

        final ValueAnimator animator = ValueAnimator.ofFloat(mAlpha, 1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                mAlpha = (float) animation.getAnimatedValue();
            }
        });
        mAlpha = from;
        return animator;
    }


    /**
     * Returns the number of entries in this set.
     *
     * @return the number of entries in this set.
     */
    public int size() {

        return mEntries.size();
    }


	/*
     * --------
	 * Getters
	 * --------
	 */

    /**
     * Get set of {@link ChartEntry}s.
     *
     * @return List of entries contained in the set.
     */
    public ArrayList<ChartEntry> getEntries() {

        return mEntries;
    }


    /**
     * Get {@link ChartEntry} from specific index.
     *
     * @param index Entry's index
     * @return {@link com.db.chart.model.ChartSet} self-reference.
     */
    public ChartEntry getEntry(int index) {

        return mEntries.get(checkPositionIndex(index, size()));
    }


    /**
     * Get {@link ChartEntry} value from specific index.
     *
     * @param index Value's index
     * @return Value of given index.
     */
    public float getValue(int index) {

        return mEntries.get(checkPositionIndex(index, size())).getValue();
    }


    /**
     * Get {@link ChartEntry} label from specific index.
     *
     * @param index Label's index
     * @return Label of given index.
     */
    public String getLabel(int index) {

        return mEntries.get(checkPositionIndex(index, size())).getLabel();
    }


    /**
     * Get {@link ChartEntry} with the highest value.
     *
     * @return Highest value entry.
     */
    public ChartEntry getMax() {

        return Collections.max(mEntries);
    }


    /**
     * Get {@link ChartEntry} with the lowest value.
     *
     * @return Lowest value entry.
     */
    public ChartEntry getMin() {

        return Collections.min(mEntries);
    }


    /**
     * Get screen points.
     *
     * @return Display coordinates of all entries.
     */
    public float[][] getScreenPoints() {

        int nEntries = size();
        float[][] result = new float[nEntries][2];
        for (int i = 0; i < nEntries; i++) {
            result[i][0] = mEntries.get(i).getX();
            result[i][1] = mEntries.get(i).getY();
        }

        return result;
    }


    /**
     * Get current set's alpha.
     *
     * @return Set's alpha.
     */
    public float getAlpha() {

        return mAlpha;
    }

    /**
     * Set set's alpha.
     *
     * @param alpha alpha value from 0 to 1.
     *              If you need to make the set invisible than consider
     *              using the method setVisible().
     */
    public void setAlpha(@FloatRange(from = 0.f, to = 1.f) float alpha) {

        mAlpha = (alpha < 1) ? alpha : 1;
    }

	
	/*
     * --------
	 * Setters
	 * --------
	 */

    /**
     * Get whether the set should be presented or not.
     *
     * @return True if set visible, False if not.
     */
    public boolean isVisible() {

        return mIsVisible;
    }

    /**
     * Set whether the set should be visible or not.
     *
     * @param visible false if set should not be visible.
     */
    public void setVisible(boolean visible) {

        mIsVisible = visible;
    }

    /**
     * Set {@link ChartEntry} value at specific index position.
     *
     * @param index Value's index where value will be placed
     */
    private void setValue(int index, float value) {

        mEntries.get(checkPositionIndex(index, size())).setValue(value);
    }

    /**
     * Define set shadow
     *
     * @param radius Radius
     * @param dx     Dx
     * @param dy     Dy
     * @param color  Color
     */
    void setShadow(float radius, float dx, float dy, int color) {

        for (ChartEntry e : getEntries())
            e.setShadow(radius, dx, dy, color);
    }

    /**
     * Returns a string representation of this set.
     *
     * @return a string representation of this set
     */
    public String toString() {

        return mEntries.toString();
    }

}
