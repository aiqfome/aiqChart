package com.aiqfome.aiqchart.animation;

import com.aiqfome.aiqchart.model.ChartSet;

import java.util.ArrayList;


/**
 * Interface used by {@link Animation} to interact with {@link com.aiqfome.aiqchart.view.ChartView}
 */
public interface ChartAnimationListener {

    /**
     * Callback to let {@link com.aiqfome.aiqchart.view.ChartView} know when to invalidate and present new data.
     *
     * @param data Chart data to be used in the next view invalidation.
     * @return True if {@link com.aiqfome.aiqchart.view.ChartView} accepts the call, False otherwise.
     */
    boolean onAnimationUpdate(ArrayList<ChartSet> data);
}
