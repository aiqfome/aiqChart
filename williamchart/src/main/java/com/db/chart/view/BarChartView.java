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

package com.db.chart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Region;
import android.graphics.Shader;
import android.util.AttributeSet;

import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.model.ChartSet;

import java.util.ArrayList;


/**
 * Implements a {@link com.db.chart.view.BarChartView} extending {@link
 * com.db.chart.view.BaseBarChartView}
 */
public class BarChartView extends BaseBarChartView {


    public BarChartView(Context context, AttributeSet attrs) {

        super(context, attrs);

        setOrientation(Orientation.VERTICAL);
        setMandatoryBorderSpacing();
    }


    public BarChartView(Context context) {

        super(context);

        setOrientation(Orientation.VERTICAL);
        setMandatoryBorderSpacing();
    }

    @Override
    public void onDrawChart(Canvas canvas, ArrayList<ChartSet> data) {

        final int nSets = data.size();
        final int nEntries = data.get(0).size();

        float offset;
        BarSet barSet;
        Bar bar;

        for (int i = 0; i < nEntries; i++) {

            // Set first offset to draw a group of bars
            offset = data.get(0).getEntry(i).getX() - drawingOffset;

            for (int j = 0; j < nSets; j++) {

                barSet = (BarSet) data.get(j);
                bar = (Bar) barSet.getEntry(i);

                // If entry value is 0 it won't be drawn
                if (!barSet.isVisible()) continue;

                // Style it!
                if (!bar.hasGradientColor()) style.barPaint.setColor(bar.getColor());
                else style.barPaint.setShader(
                        new LinearGradient(bar.getX(), this.getZeroPosition(), bar.getX(), bar.getY(),
                                bar.getGradientColors(), bar.getGradientPositions(),
                                Shader.TileMode.MIRROR));
                applyShadow(style.barPaint, barSet.getAlpha(), bar.getShadowDx(), bar
                        .getShadowDy(), bar.getShadowRadius(), bar.getShadowColor());

                // Draw background
                if (style.hasBarBackground) {
                    drawBarBackground(canvas, offset, this.getInnerChartTop(), offset + barWidth,
                            this.getInnerChartBottom());
                }

                // Draw bar
                if (bar.getValue() >= 0) // Positive
                    drawBar(canvas, offset, bar.getY(), offset + barWidth, this.getZeroPosition());
                else // Negative
                    drawBar(canvas, offset, this.getZeroPosition(), offset + barWidth, bar.getY());

                offset += barWidth;

                // If last bar of group no set spacing is necessary
                if (j != nSets - 1) offset += style.setSpacing;
            }
        }
    }

    @Override
    protected void onPreDrawChart(ArrayList<ChartSet> data) {

        // In case of only on entry
        if (data.get(0).size() == 1) {
            style.barSpacing = 0;
            calculateBarsWidth(data.size(), 0, this.getInnerChartRight()
                    - this.getInnerChartLeft()
                    - this.getBorderSpacing() * 2);
            // In case of more than one entry
        } else calculateBarsWidth(data.size(), data.get(0).getEntry(0).getX(),
                data.get(0).getEntry(1).getX());

        calculatePositionOffset(data.size());
    }

    @Override
    void defineRegions(ArrayList<ArrayList<Region>> regions, ArrayList<ChartSet> data) {

        final int nSets = data.size();
        final int nEntries = data.get(0).size();

        float offset;
        BarSet barSet;
        Bar bar;

        for (int i = 0; i < nEntries; i++) {

            // Set first offset to draw a group of bars
            offset = data.get(0).getEntry(i).getX() - drawingOffset;

            for (int j = 0; j < nSets; j++) {

                barSet = (BarSet) data.get(j);
                bar = (Bar) barSet.getEntry(i);

                if (bar.getValue() > 0 && (int) bar.getY() != (int) this.getZeroPosition())
                    regions.get(j).get(i).set((int) offset, (int) bar.getY(),
                            (int) (offset += barWidth), (int) this.getZeroPosition());
                else if (bar.getValue() < 0 && (int) bar.getY() != (int) this.getZeroPosition())
                    regions.get(j).get(i).set((int) offset, (int) this.getZeroPosition(),
                            (int) (offset += barWidth), (int) bar.getY());
                else // If bar.getValue() == 0, force region to 1 pixel
                    regions.get(j).get(i).set((int) offset, (int) this.getZeroPosition(),
                            (int) (offset += barWidth), (int) this.getZeroPosition() + 1);

                // If last bar of group no set spacing is necessary
                if (j != nSets - 1) offset += style.setSpacing;
            }
        }
    }

}