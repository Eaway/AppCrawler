// https://android.googlesource.com/platform/frameworks/testing/+/master/uiautomator_test_libraries/src/com/android/uiautomator/common/UiWatchers.java
/*
 * Copyright (C) 2013 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.eaway.appcrawler.common;

import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.UiWatcher;
import android.util.Log;

import com.eaway.appcrawler.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UiWatchers {
    private static final String TAG = Config.TAG;
    private static UiDevice sDevice;
    private final List<String> mErrors = new ArrayList<String>();

    /**
     * We can use the UiDevice registerWatcher to register a small script to be
     * executed when the framework is waiting for a control to appear. Waiting may
     * be the cause of an unexpected dialog on the screen and it is the time when
     * the framework runs the registered watchers. This is a sample watcher
     * looking for ANR and crashes. it closes it and moves on. You should create
     * your own watchers and handle error logging properly for your type of tests.
     */
    public void registerAnrAndCrashWatchers() {
        sDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        sDevice.registerWatcher("ANR", new UiWatcher() {
            @Override
            public boolean checkForCondition() {
                return handleAnr();
            }
        });

        sDevice.registerWatcher("ANR2", new UiWatcher() {
            @Override
            public boolean checkForCondition() {
                return handleAnr2();
            }
        });

        sDevice.registerWatcher("CRASH", new UiWatcher() {
            @Override
            public boolean checkForCondition() {
                return handleCrash();
            }
        });

        sDevice.registerWatcher("CRASH2", new UiWatcher() {
            @Override
            public boolean checkForCondition() {
                return handleCrash2();
            }
        });

        //sDevice.registerWatcher("COMMONDIALOG", new UiWatcher() {
        //    @Override
        //    public boolean checkForCondition() {
        //        return handleCommonDialog();
        //    }
        //});

        Log.i(TAG, "Registed GUI Exception watchers");
    }

    public boolean handleAnr() {
        UiObject window = sDevice.findObject(new UiSelector()
                .className("com.android.server.am.AppNotRespondingDialog"));
        String errorText = null;
        if (window.exists()) {
            try {
                errorText = window.getText();
            } catch (UiObjectNotFoundException e) {
                Log.e(TAG, "dialog gone?", e);
            }
            onAnrDetected(errorText);
            postHandler();
            return true; // triggered
        }
        return false; // no trigger
    }

    public boolean handleAnr2() {
        UiObject window = sDevice.findObject(new UiSelector().packageName("android")
                .textContains("isn't responding."));
        if (!window.exists()) {
            window = sDevice.findObject(new UiSelector().packageName("android")
                    .textContains("沒有回應"));
        }
        if (window.exists()) {
            String errorText = null;
            try {
                errorText = window.getText();
            } catch (UiObjectNotFoundException e) {
                Log.e(TAG, "dialog gone?", e);
            }
            onAnrDetected(errorText);
            postHandler();
            return true; // triggered
        }
        return false; // no trigger
    }

    public boolean handleCrash() {
        UiObject window = sDevice.findObject(new UiSelector()
                .className("com.android.server.am.AppErrorDialog"));
        if (window.exists()) {
            String errorText = null;
            try {
                errorText = window.getText();
            } catch (UiObjectNotFoundException e) {
                Log.e(TAG, "dialog gone?", e);
            }
            onCrashDetected(errorText);
            postHandler();
            return true; // triggered
        }
        return false; // no trigger
    }

    public boolean handleCrash2() {
        UiObject window = sDevice.findObject(new UiSelector().packageName("android")
                .textContains("has stopped"));
        if (!window.exists()) {
            window = sDevice.findObject(new UiSelector().packageName("android")
                    .textContains("已停止運作"));
        }
        if (window.exists()) {
            String errorText = null;
            try {
                errorText = window.getText();
            } catch (UiObjectNotFoundException e) {
                Log.e(TAG, "dialog gone?", e);
            }
            UiHelper.takeScreenshots("[CRASH]");
            onCrashDetected(errorText);
            postHandler();
            return true; // triggered
        }
        return false; // no trigger
    }

    public void onAnrDetected(String errorText) {
        mErrors.add(errorText);
    }

    public void onCrashDetected(String errorText) {
        mErrors.add(errorText);
    }

    public void reset() {
        mErrors.clear();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(mErrors);
    }

    /**
     * Current implementation ignores the exception and continues.
     */
    public void postHandler() {
        // TODO: Add custom error logging here

        String formatedOutput = String.format("UI Exception Message: %-20s\n",
                sDevice.getCurrentPackageName());
        Log.e(TAG, formatedOutput);

        UiObject buttonOK = sDevice.findObject(new UiSelector().text("OK").enabled(true));
        if (!buttonOK.exists()) {
            buttonOK = sDevice.findObject(new UiSelector().text("確定").enabled(true));
        }

        try {
            // sometimes it takes a while for the OK button to become enabled
            buttonOK.waitForExists(5000);
            buttonOK.click();
        } catch (UiObjectNotFoundException e) {
            Log.e(TAG, "Exception", e);
        }
    }
}