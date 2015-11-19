package com.eaway.appcrawler.common;

import android.support.test.uiautomator.UiObject;

/**
 * UiWidgets is an android view in the UiScreen that we are interested in test (e.g. Button)
  *      - Clickable:
 *      - EditView:
 *      - Scrollable:
 */
public class UiWidget implements Cloneable {
    public UiObject uiObject;
    private boolean mFinished = false; // It has been tested or not

    public UiWidget(UiObject object) {
        uiObject = object;
        mFinished = false;
    }

    protected Object clone() throws CloneNotSupportedException {
        UiWidget clone = (UiWidget) super.clone();
        return clone;
    }

    public void setFinished(boolean finished) {
        mFinished = finished;
    }

    public boolean isFinished() {
        return mFinished;
    }
}
