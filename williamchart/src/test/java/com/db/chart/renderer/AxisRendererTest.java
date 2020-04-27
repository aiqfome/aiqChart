package com.db.chart.renderer;

import android.test.suitebuilder.annotation.MediumTest;

import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.model.ChartSet;
import com.db.chart.view.ChartView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
@MediumTest
public class AxisRendererTest {

    @Mock
    ChartView.Style mStyleMock;

    private XRenderer mXRndr;

    private ArrayList<ChartSet> mData;


    @Before
    public void setUp() {

        mXRndr = new XRenderer();
        BarSet set = new BarSet();
        for (int i = 0; i < 2; i++)
            set.addBar(new Bar(Integer.toString(i), (float) i));
        mData = new ArrayList<>();
        mData.add(set);
    }


    @Test
    public void init_HandleValues_NoBordersAndNoStep() {

        when(mStyleMock.getLabelsFormat()).thenReturn(new DecimalFormat());

        mXRndr.setHandleValues(true);
        mXRndr.init(mData, mStyleMock);

        ArrayList<String> labels = mXRndr.labels;
        assertEquals("0", labels.get(0));
        assertEquals("0.333", labels.get(1));
        assertEquals(0.333, mXRndr.getStep(), 0.001);
    }


    @Test
    public void init_HandleValues_NoBordersAndStep() {

        when(mStyleMock.getLabelsFormat()).thenReturn(new DecimalFormat());

        mXRndr.setHandleValues(true);
        mXRndr.setStep(9);
        mXRndr.init(mData, mStyleMock);

        ArrayList<String> labels = mXRndr.labels;
        assertEquals("0", labels.get(0));
        assertEquals("9", labels.get(1));
    }


    @Test
    public void init_HandleValues_BordersAndNoStep() {

        when(mStyleMock.getLabelsFormat()).thenReturn(new DecimalFormat());

        mXRndr.setHandleValues(true);
        mXRndr.setBorderValues(0, 9);
        mXRndr.init(mData, mStyleMock);

        assertEquals(3, mXRndr.getStep(), 0);
    }


    @Test
    public void defineMandatoryBorderSpacing_Mandatory_Result() {

        when(mStyleMock.getLabelsFormat()).thenReturn(new DecimalFormat());
        when(mStyleMock.getAxisBorderSpacing()).thenReturn(1);

        mXRndr.init(mData, mStyleMock);
        mXRndr.setMandatoryBorderSpacing(true);

        mXRndr.defineMandatoryBorderSpacing(0, 4);

        assertEquals(0.5f, mXRndr.mandatoryBorderSpacing, 0.f);
    }


    @Test
    public void defineMandatoryBorderSpacing_NotMandatory_Result() {

        when(mStyleMock.getLabelsFormat()).thenReturn(new DecimalFormat());
        when(mStyleMock.getAxisBorderSpacing()).thenReturn(1);

        mXRndr.init(mData, mStyleMock);
        mXRndr.setMandatoryBorderSpacing(false);

        mXRndr.defineMandatoryBorderSpacing(0, 4);

        assertEquals(0.f, mXRndr.mandatoryBorderSpacing, 0.f);
    }


    @Test
    public void defineLabelsPosition_Nominal_ScreenStep2() {

        when(mStyleMock.getLabelsFormat()).thenReturn(new DecimalFormat());
        when(mStyleMock.getAxisBorderSpacing()).thenReturn(1);

        mXRndr.init(mData, mStyleMock);

        mXRndr.defineLabelsPosition(0, 4);

        assertEquals(2.f, mXRndr.screenStep, 0.f);
    }


    @Test
    public void defineLabelsPosition_Nominal_LabelsPos13() {

        when(mStyleMock.getLabelsFormat()).thenReturn(new DecimalFormat());
        when(mStyleMock.getAxisBorderSpacing()).thenReturn(1);

        mXRndr.init(mData, mStyleMock);

        mXRndr.defineLabelsPosition(0, 4);

        ArrayList<Float> toAssert = new ArrayList<>();
        toAssert.add(1.f);
        toAssert.add(3.f);
        assertEquals(toAssert, mXRndr.labelsPos);
    }


    @Test
    public void convertToLabelsFormat_Integer_StringXXDB() {

        ArrayList<Float> values = new ArrayList<>();
        values.add(3.f);
        DecimalFormat format = new DecimalFormat("#'DB'");

        assertEquals("3DB", mXRndr.convertToLabelsFormat(values, format).get(0));
    }


    @Test
    public void extractLabels_ChartSets_Result() {

        ArrayList<String> toAssert = new ArrayList<>();
        toAssert.add("0");
        toAssert.add("1");
        assertEquals(toAssert, mXRndr.extractLabels(mData));
    }

    @Test
    public void findBorders_NoStep() {

        assertArrayEquals(new float[]{0, 1}, mXRndr.findBorders(mData), 0);
    }

    @Test
    public void findBorders_BiggerStep_Result() {

        assertArrayEquals(new float[]{0, 9}, mXRndr.findBorders(mData, 9), 0);
    }


    @Test
    public void findBorders_MaxMinEqual_MaxPlusStep() {

        BarSet set = new BarSet();
        for (int i = 0; i < 2; i++)
            set.addBar(new Bar(Integer.toString(i), (float) 0));
        mData = new ArrayList<>();
        mData.add(set);

        assertArrayEquals(new float[]{0, 9}, mXRndr.findBorders(mData, 9), 0);
    }


    @Test
    public void calculateValues_Nominal_Result() {

        ArrayList<Float> toAssert = new ArrayList<>();
        toAssert.add(0.f);
        toAssert.add(5.f);
        toAssert.add(10.f);
        assertEquals(toAssert, mXRndr.calculateValues(0, 8, 5));
    }


    @Test(expected = IllegalArgumentException.class)
    public void setBorderValues_MinGreaterMax_ThrowException() {

        mXRndr.setBorderValues(2, 1, 1);
    }


    @Test
    public void setBorderValues_Nominal_StepLargestDivisor() {

        XRenderer spyXRndr = spy(mXRndr);
        doReturn(false).when(spyXRndr).hasStep();

        spyXRndr.setBorderValues(3, 26);
        assertEquals(7.666, spyXRndr.getStep(), 0.001);
        spyXRndr.setBorderValues(3, 30);
        assertEquals(9, spyXRndr.getStep(), 0);
        spyXRndr.setBorderValues(3, 18);
        assertEquals(5, spyXRndr.getStep(), 0);
        spyXRndr.setBorderValues(3, 23);
        assertEquals(6.666, spyXRndr.getStep(), 0.001);
        spyXRndr.setBorderValues(-3, 17);
        assertEquals(6.666, spyXRndr.getStep(), 0.001);
    }

}
