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

package com.db.chart.renderer;

import android.graphics.Canvas;
import android.graphics.Paint.Align;

import java.util.Collections;


/**
 * Class responsible to control vertical measures, positions, yadda yadda.
 * If the drawing is requested it will also take care of it.
 */
public class YRenderer extends AxisRenderer {


    public YRenderer() {

        super();
    }


    /*
     * IMPORTANT: Method's calls order is crucial. Change it (or not) carefully.
     */
    @Override
    public void dispose() {

        super.dispose();

        defineMandatoryBorderSpacing(mInnerChartTop, mInnerChartBottom);
        defineLabelsPosition(mInnerChartTop, mInnerChartBottom);
    }

    @Override
    protected float defineAxisPosition() {

        float result = mInnerChartLeft;
        if (style.hasYAxis()) result -= style.getAxisThickness() / 2;
        return result;
    }

    @Override
    protected float defineStaticLabelsPosition(float axisCoordinate, int distanceToAxis) {

        float result = axisCoordinate;

        if (style.getYLabelsPositioning() == LabelPosition.INSIDE) {
            result += distanceToAxis;
            if (style.hasYAxis()) result += style.getAxisThickness() / 2;

        } else if (style.getYLabelsPositioning() == LabelPosition.OUTSIDE) {
            result -= distanceToAxis;
            if (style.hasYAxis()) result -= style.getAxisThickness() / 2;
        }
        return result;
    }

    @Override
    public void draw(Canvas canvas) {

        if (style.hasYAxis()) {
            // Draw axis line
            float bottom = mInnerChartBottom;
            if (style.hasXAxis()) bottom += style.getAxisThickness();

            canvas.drawLine(axisPosition, mInnerChartTop, axisPosition, bottom, style.getChartPaint());
        }

        if (style.getYLabelsPositioning() != LabelPosition.NONE) {

            style.getLabelsPaint().setTextAlign(
                    (style.getYLabelsPositioning() == LabelPosition.OUTSIDE) ? Align.RIGHT : Align.LEFT);

            // Draw labels
            int nLabels = labels.size();
            for (int i = 0; i < nLabels; i++) {
                canvas.drawText(labels.get(i), labelsStaticPos,
                        labelsPos.get(i) + style.getLabelHeight(labels.get(i)) / 2,
                        style.getLabelsPaint());
            }
        }
    }

    @Override
    void defineLabelsPosition(float innerStart, float innerEnd) {

        super.defineLabelsPosition(innerStart, innerEnd);
        Collections.reverse(labelsPos);
    }

    @Override
    public float parsePos(int index, double value) {

        if (handleValues)
            return (float) (mInnerChartBottom
                    - (((value - minLabelValue) * screenStep) / (labelsValues.get(1) - minLabelValue)));
        else return labelsPos.get(index);
    }

    @Override
    protected float measureInnerChartLeft(int left) {

        float result = left;

        if (style.hasYAxis()) result += style.getAxisThickness();

        if (style.getYLabelsPositioning() == LabelPosition.OUTSIDE) {
            float aux;
            float maxLabelLength = 0;
            for (String label : labels) {
                aux = style.getLabelsPaint().measureText(label);
                if (aux > maxLabelLength)
                    maxLabelLength = aux;
            }
            result += maxLabelLength + style.getAxisLabelsSpacing();
        }
        return result;
    }

    @Override
    protected float measureInnerChartTop(int top) {

        return top;
    }

    @Override
    protected float measureInnerChartRight(int right) {

        return right;
    }

    @Override
    protected float measureInnerChartBottom(int bottom) {

        if (style.getYLabelsPositioning() != LabelPosition.NONE
                && style.getAxisBorderSpacing() < style.getFontMaxHeight() / 2)
            return bottom - style.getFontMaxHeight() / 2;
        return bottom;
    }

}