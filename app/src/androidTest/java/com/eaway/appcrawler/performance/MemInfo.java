package com.eaway.appcrawler.performance;

import android.app.ActivityManager;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

/**
 * Memory Info
 */
public class MemInfo {

    public static android.os.Debug.MemoryInfo getProcessMemInfo (int pid) {
        Context context = InstrumentationRegistry.getContext();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int[] pids = new int[1];
        pids[0] = pid;
        android.os.Debug.MemoryInfo[] mems = am.getProcessMemoryInfo(pids);
        return mems[0];
    }
}
