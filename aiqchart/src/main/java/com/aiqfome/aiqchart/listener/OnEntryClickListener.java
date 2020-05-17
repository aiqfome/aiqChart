package com.aiqfome.aiqchart.listener;

import android.graphics.Rect;


/**
 * Interface to define a listener when an chart entry has been clicked
 */
public interface OnEntryClickListener {

    /**
     * Abstract method to define the code when an entry has been clicked
     *
     * @param setIndex   index of the entry set clicked
     * @param entryIndex index of the entry within set(setIndex)
     * @param rect       a Rect covering the entry area.
     */
    void onClick(int setIndex, int entryIndex, Rect rect);

}
