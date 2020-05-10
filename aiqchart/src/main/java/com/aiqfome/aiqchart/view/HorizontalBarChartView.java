package com.aiqfome.aiqchart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Region;
import android.graphics.Shader;
import android.util.AttributeSet;

import com.aiqfome.aiqchart.model.Bar;
import com.aiqfome.aiqchart.model.BarSet;
import com.aiqfome.aiqchart.model.ChartSet;

import java.util.ArrayList;


/**
 * Implements a {@link com.aiqfome.aiqchart.view.HorizontalBarChartView} extending {@link
 * com.aiqfome.aiqchart.view.ChartView}
 */
public class HorizontalBarChartView extends BaseBarChartView {


    public HorizontalBarChartView(Context context, AttributeSet attrs) {

        super(context, attrs);

        setOrientation(Orientation.HORIZONTAL);
        setMandatoryBorderSpacing();
    }


    public HorizontalBarChartView(Context context) {

        super(context);

        setOrientation(Orientation.HORIZONTAL);
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
            offset = data.get(0).getEntry(i).getY() - drawingOffset;

            for (int j = 0; j < nSets; j++) {

                barSet = (BarSet) data.get(j);
                bar = (Bar) barSet.getEntry(i);

                // If entry value is 0 it won't be drawn
                if (!barSet.isVisible()) continue;

                // Style it!
                if (!bar.hasGradientColor()) style.barPaint.setColor(bar.getColor());
                else style.barPaint.setShader(
                        new LinearGradient(this.getZeroPosition(), bar.getY(), bar.getX(), bar.getY(),
                                bar.getGradientColors(), bar.getGradientPositions(),
                                Shader.TileMode.MIRROR));
                applyShadow(style.barPaint, barSet.getAlpha(), bar.getShadowDx(), bar
                        .getShadowDy(), bar.getShadowRadius(), bar.getShadowColor());

                // Draw background
                if (style.hasBarBackground)
                    drawBarBackground(canvas, this.getInnerChartLeft(), offset,
                            this.getInnerChartRight(), (offset + barWidth));

                // Draw bar
                if (bar.getValue() >= 0) // Positive
                    drawBar(canvas, this.getZeroPosition(), offset, bar.getX(), offset + barWidth);
                else // Negative
                    drawBar(canvas, bar.getX(), offset, this.getZeroPosition(), offset + barWidth);

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
            calculateBarsWidth(data.size(), 0, this.getInnerChartBottom()
                    - this.getInnerChartTop()
                    - this.getBorderSpacing() * 2);
            // In case of more than one entry
        } else calculateBarsWidth(data.size(), data.get(0).getEntry(1).getY(),
                data.get(0).getEntry(0).getY());

        calculatePositionOffset(data.size());
    }

    @Override
    void defineRegions(ArrayList<ArrayList<Region>> regions, ArrayList<ChartSet> data) {

        int nSets = data.size();
        int nEntries = data.get(0).size();

        float offset;
        BarSet barSet;
        Bar bar;

        for (int i = 0; i < nEntries; i++) {

            // Set first offset to draw a group of bars
            offset = data.get(0).getEntry(i).getY() - drawingOffset;

            for (int j = 0; j < nSets; j++) {

                barSet = (BarSet) data.get(j);
                bar = (Bar) barSet.getEntry(i);

                if (bar.getValue() > 0 && (int) bar.getX() != (int) this.getZeroPosition())
                    regions.get(j).get(i).set((int) this.getZeroPosition(), (int) offset,
                            (int) bar.getX(), (int) (offset + barWidth));
                else if (bar.getValue() < 0 && (int) bar.getX() != (int) this.getZeroPosition())
                    regions.get(j).get(i).set((int) bar.getX(), (int) offset,
                            (int) this.getZeroPosition(), (int) (offset + barWidth));
                else // If bar.getValue() == 0, force region to 1 pixel
                    regions.get(j).get(i).set((int) this.getZeroPosition() - 1, (int) offset,
                            (int) this.getZeroPosition(), (int) (offset + barWidth));

                // If last bar of group no set spacing is necessary
                if (j != nSets - 1) offset += style.setSpacing;
            }
        }
    }

}