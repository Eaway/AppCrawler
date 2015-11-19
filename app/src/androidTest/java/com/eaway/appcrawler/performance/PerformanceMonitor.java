package com.eaway.appcrawler.performance;

import android.app.ActivityManager;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.util.Log;
import android.view.accessibility.AccessibilityWindowInfo;

import com.eaway.appcrawler.Config;
import com.eaway.appcrawler.FileLog;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * CPU and Memory monitor
 */
public class PerformanceMonitor {
    private static final String TAG = Config.TAG;
    private static final String TAG_MAIN = Config.TAG_MAIN;

    public static List<Integer> memList = new ArrayList<Integer>();
    public static List<Float> cpuList = new ArrayList<Float>();

    public static Float cpuLast = 0f;
    public static Float cpuPeak = 0f;
    public static int memLast = 0;
    public static int memPeak = 0;

    public static void reset() {
        memList.clear();
        cpuList.clear();
        memPeak = 0;
        memLast = 0;
        cpuPeak = 0f;
        cpuLast = 0f;
    }

    public static synchronized void init() {
        reset();
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(Config.sPerformanceLog, true),"UTF-8"));
            if (writer != null)
                writer.print("\uFEFF"); // byte-order marker (BOM)
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null)
                writer.close();
        }
        writeLog("Time,CPU%,Memory(KB),Screen");
    }

    public static synchronized void writeLog(String str) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(Config.sPerformanceLog, true),"UTF-8"));
            if (writer != null)
                writer.print(str + "\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    public static Float getAverageCpu() {
        if (cpuList.size() == 0)
            return 0f;

        Float total = 0f;
        Float average = 0f;
        for (int i = 0; i < cpuList.size(); i++) {
            total += cpuList.get(i);
        }
        average = total / cpuList.size();
        return average;
    }

    public static int getAverageMemory() {
        if (memList.size() == 0)
            return 0;

        int total = 0;
        int average = 0;
        for (int i = 0; i < memList.size(); i++) {
            total += memList.get(i);
        }
        average = total / memList.size();
        return average;
    }

    public static boolean record(String msg) {
        List<AccessibilityWindowInfo> list = InstrumentationRegistry.getInstrumentation().getUiAutomation().getWindows();
        for (AccessibilityWindowInfo win : list) {
            Log.i(TAG_MAIN, win.getClass().getName());
            Log.i(TAG_MAIN, win.getClass().getSimpleName());
            Log.i(TAG_MAIN, win.getClass().toString());
        }

        Context context = InstrumentationRegistry.getContext();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo app : appList) {
            if (0 == Config.sTargetPackage.compareToIgnoreCase(app.processName)) {
                // CPU
                cpuLast = CpuInfo.getProcessCpuRate(app.pid);
                cpuList.add(cpuLast);
                if (cpuLast > cpuPeak)
                    cpuPeak = cpuLast;

                // Memory
                android.os.Debug.MemoryInfo mem = MemInfo.getProcessMemInfo(app.pid);
                memLast = mem.getTotalPss();
                memList.add(memLast);
                if (mem.getTotalPss() > memPeak)
                    memPeak = mem.getTotalPss();

                String log = String.format("{Performance} package:%s, cpu:%.1f%%" +
                                ", memory total pss (KB):%d, total private dirty (KB):%d, total shared (KB):%d" +
                                ", dalvik private:%d, dalvik shared:%d, dalvik pss:%d" +
                                ", native private:%d, native shared:%d, native pss:%d" +
                                ", others private:%d, others shared:%d, others pss:%d",
                        app.processName, cpuLast,
                        mem.getTotalPss(), mem.getTotalPrivateDirty(), mem.getTotalSharedDirty(),
                        mem.dalvikPrivateDirty, mem.dalvikSharedDirty, mem.dalvikPss,
                        mem.nativePrivateDirty, mem.nativeSharedDirty, mem.nativePss,
                        mem.otherPrivateDirty, mem.otherSharedDirty, mem.otherPss);
                FileLog.i(TAG_MAIN, log);

                // Performance log
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
                writeLog(String.format("%s,%.1f%%,%d,%s", sdf.format(new Date()), cpuLast, memLast, msg));

                break;
            }
        }

        return true;
    }

}
