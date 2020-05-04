package com.aiqfome.aiqchart.listener;

import android.view.View;


/**
 * Interface to define a listener when an chart entry has been clicked
 */
public interface OnTooltipEventListener {

    /**
     * Called once tooltip is entering display.
     *
     * @param view View representing the tooltip which will be added to the display.
     */
    void onEnter(View view);

    /**
     * Called once tooltip is exiting display.
     *
     * @param view View representing tooltip which will be removed from the display.
     */
    void onExit(View view);

}
