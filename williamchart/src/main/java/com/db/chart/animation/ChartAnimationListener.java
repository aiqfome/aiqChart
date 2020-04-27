package com.db.chart.animation;

import com.db.chart.model.ChartSet;

import java.util.ArrayList;


/**
 * Interface used by {@link Animation} to interact with {@link com.db.chart.view.ChartView}
 */
public interface ChartAnimationListener {

    /**
     * Callback to let {@link com.db.chart.view.ChartView} know when to invalidate and present new data.
     *
     * @param data Chart data to be used in the next view invalidation.
     * @return True if {@link com.db.chart.view.ChartView} accepts the call, False otherwise.
     */
    boolean onAnimationUpdate(ArrayList<ChartSet> data);
}
