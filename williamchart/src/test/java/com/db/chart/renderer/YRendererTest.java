package com.db.chart.renderer;

import android.graphics.Paint;
import android.test.suitebuilder.annotation.MediumTest;

import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.model.ChartSet;
import com.db.chart.view.ChartView.Style;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
@MediumTest
public class YRendererTest {

    private final static float RESULT_THRESHOLD = .5f;

    @Mock
    Style mStyleMock;

    @Mock
    Paint mMockPaint;

    private YRenderer mYRndr;

    private ArrayList<ChartSet> mData;


    @Before
    public void setUp() {

        mYRndr = new YRenderer();
        BarSet set = new BarSet();
        for (int i = 0; i < 2; i++)
            set.addBar(new Bar(Integer.toString(i), (float) i));
        mData = new ArrayList<>();
        mData.add(set);
    }


    @Test
    public void defineAxisPosition_HasAxis_Result() {

        when(mStyleMock.getAxisThickness()).thenReturn(6.f);
        when(mStyleMock.hasYAxis()).thenReturn(true);

        mYRndr.init(mData, mStyleMock);
        mYRndr.setInnerChartBounds(5.f, 0.f, 0.f, 0.f);

        assertEquals(2.f, mYRndr.defineAxisPosition(), RESULT_THRESHOLD);
    }


    @Test
    public void defineStaticLabelsPosition_LabelsInsideHasAxis_Result() {

        when(mStyleMock.hasYAxis()).thenReturn(true);
        when(mStyleMock.getAxisThickness()).thenReturn(6.f);
        when(mStyleMock.getYLabelsPositioning()).thenReturn(AxisRenderer.LabelPosition.INSIDE);

        mYRndr.init(mData, mStyleMock);
        mYRndr.setInnerChartBounds(18.f, 0.f, 0.f, 0.f);
        float axisCoordinate = mYRndr.defineAxisPosition();
        float labelsCoordinate = mYRndr.defineStaticLabelsPosition(axisCoordinate, 6);

        assertEquals(24.f, labelsCoordinate, RESULT_THRESHOLD);
        assertTrue(labelsCoordinate > axisCoordinate);
    }


    @Test
    public void defineStaticLabelsPosition_LabelsOutsideHasAxis_Result() {

        when(mStyleMock.hasYAxis()).thenReturn(true);
        when(mStyleMock.getAxisThickness()).thenReturn(6.f);
        when(mStyleMock.getYLabelsPositioning()).thenReturn(AxisRenderer.LabelPosition.OUTSIDE);

        mYRndr.init(mData, mStyleMock);
        mYRndr.setInnerChartBounds(18.f, 0.f, 0.f, 0.f);
        float axisCoordinate = mYRndr.defineAxisPosition();
        float labelsCoordinate = mYRndr.defineStaticLabelsPosition(axisCoordinate, 6);

        assertEquals(6.f, labelsCoordinate, RESULT_THRESHOLD);
        assertTrue(labelsCoordinate < axisCoordinate);
    }


    @Test
    public void measureInnerChartLeft_LabelsNone_Result() {

        when(mStyleMock.hasYAxis()).thenReturn(true);
        when(mStyleMock.getAxisThickness()).thenReturn(6.f);
        when(mStyleMock.getYLabelsPositioning()).thenReturn(AxisRenderer.LabelPosition.OUTSIDE);
        when(mStyleMock.getLabelsPaint()).thenReturn(mMockPaint);
        when(mStyleMock.getAxisLabelsSpacing()).thenReturn(6);

        when(mMockPaint.measureText("0")).thenReturn(6.f);
        when(mMockPaint.measureText("1")).thenReturn(12.f);

        mYRndr.init(mData, mStyleMock);

        assertEquals(30.f, mYRndr.measureInnerChartLeft(6), RESULT_THRESHOLD);
    }


    @Test
    public void measureInnerChartBottom_HasAxisLabelsOutside_Result() {

        when(mStyleMock.hasXAxis()).thenReturn(true);
        when(mStyleMock.getAxisThickness()).thenReturn(6.f);
        when(mStyleMock.getFontMaxHeight()).thenReturn(18);
        when(mStyleMock.getAxisBorderSpacing()).thenReturn(6);
        when(mStyleMock.getYLabelsPositioning()).thenReturn(AxisRenderer.LabelPosition.INSIDE);

        mYRndr.init(mData, mStyleMock);

        assertEquals(15.f, mYRndr.measureInnerChartBottom(24), RESULT_THRESHOLD);
    }


    @Test
    public void parsePos_HandleValuesNoLabels_Result() {

        when(mStyleMock.getLabelsFormat()).thenReturn(new DecimalFormat());
        when(mStyleMock.hasXAxis()).thenReturn(false);
        when(mStyleMock.getYLabelsPositioning()).thenReturn(AxisRenderer.LabelPosition.NONE);

        mYRndr.setHandleValues(true);
        mYRndr.setInnerChartBounds(0, 0, 10, 10);
        mYRndr.init(mData, mStyleMock);
        mYRndr.dispose();

        assertTrue(mYRndr.parsePos(0, 0) > mYRndr.parsePos(0, 0.1));
        assertTrue(mYRndr.parsePos(0, 1) > mYRndr.parsePos(0, 111));
        assertTrue(mYRndr.parsePos(0, -1) < mYRndr.parsePos(0, -111));
        assertTrue(mYRndr.parsePos(0, -1) > mYRndr.parsePos(0, 1));
    }

}
