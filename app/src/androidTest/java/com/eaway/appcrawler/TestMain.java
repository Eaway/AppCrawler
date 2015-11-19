/*
 *  Android App Crawler using UiAutomator 2.0
 *
 *  AppCrawler is an automatic app UI crawler test tool, it scan your app screens, find testable views, and test them.
 *
 * Features:
 *     - Powerful screenshot tool for test, review and other reference.
 *     - No need of writing test script.
 *     - Able to detect ANR and Crash.
 *     - Report with  logcat, Screenshot and Human reproducible steps (compare to monkey)
 *     - Can dismiss common popups, including system and 3rd party.
 *
 * Screenshot folder:
 *     /sdcard/AppCrawler/<package>/
 *
 * Run test from command line:
 *     adb shell am instrument -e target [package] -w com.eaway.appcrawler.test/android.support.test.runner.AndroidJUnitRunner
 *
 *     [Options]
 *         -e target [package]                         package to be tested
 *         -e max-steps [number]                   maximum test steps (e.g. click, scroll), default 999
 *         -e max-depth [number]                  maximum depth from the root activity (default launchable activity), default 30
 *         -e max-screenshot [number]           maximum screenshot file, default 999
 *         -e max-screenloop [number]          maximum screens loop to avoid infinite loop, default 20
 *         -e max-runtime [second]                maximum run time in second, default 3600
 *         -e capture-steps [true|false]             take screenshot for every steps, this generate more screenshots (may duplicated), default false.
 *         -e random-text [true|false]              input some random text to EditText if any, default true.
 *         -e launch-timeout [millisecond]      timeout millisecond for launch app package, default 5000
 *         -e waitidle-timeout [millisecond]    timeout millisecond for wait app idle, default 100
 *
 *     [Examples]
 *         # adb shell am instrument -e target com.google.android.youtube -w com.eaway.appcrawler.test/android.support.test.runner.AndroidJUnitRunner
 *
 *         Please refer to the link below for other am instrument flags:
 *             http://developer.android.com/intl/zh-tw/tools/testing/testing_otheride.html#AMSyntax
 */

package com.eaway.appcrawler;

import android.os.Bundle;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.Configurator;
import android.util.Log;

import com.eaway.appcrawler.common.UiHelper;
import com.eaway.appcrawler.performance.PerformanceMonitor;
import com.eaway.appcrawler.strategy.Crawler;
import com.eaway.appcrawler.strategy.DepthFirstCrawler;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

/**
 * AppCrawler test using Android UiAutomator 2.0
 */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class TestMain {
    private static final String TAG = Config.TAG;
    private static final String TAG_MAIN = Config.TAG_MAIN;
    private static final String TAG_DEBUG = Config.TAG_DEBUG;

    @BeforeClass
    public static void beforeClass() throws Exception {
        Log.v(TAG, new Exception().getStackTrace()[0].getMethodName() + "()");

        // Get command line parameters
        getArguments();

        // Create screenshot folder
        File path = Environment.getExternalStorageDirectory();
        Config.sOutputDir = new File(String.format("%s/AppCrawler/%s", path.getAbsolutePath(), Config.sTargetPackage));
        deleteRecursive(Config.sOutputDir);
        if (!Config.sOutputDir.exists()) {
            if (!Config.sOutputDir.mkdirs()) {
                Log.d(TAG, "Failed to create screenshot folder: " + Config.sOutputDir.getPath());
            }
        }

        // Init File log
        Config.sFileLog = Config.sOutputDir + "/" + Config.TAG + ".log";
        FileLog.i(TAG_MAIN, "Version: " + Config.VERSION);

        // Init Performance log
        Config.sPerformanceLog = Config.sOutputDir + "/Performance.csv";
        PerformanceMonitor.init();

        //  Set timeout longer so we can see the ANR dialog?
        Configurator conf = Configurator.getInstance();
        conf.setActionAcknowledgmentTimeout(200L); // Generally, this timeout should not be modified, default 3000
        conf.setScrollAcknowledgmentTimeout(100L); // Generally, this timeout should not be modified, default 200
        conf.setWaitForIdleTimeout(0L);
        conf.setWaitForSelectorTimeout(0L);
        //conf.setKeyInjectionDelay(0L);
        logConfiguration();

        // Register UiWatchers: ANR, CRASH, ....
        UiHelper.registerAnrAndCrashWatchers();

        // Good practice to start from the home screen (launcher)
        UiHelper.launchHome();
    }

    @AfterClass
    public static void afterClass() {
        Log.v(TAG, new Exception().getStackTrace()[0].getMethodName() + "()");

        //UiHelper.launchCrawlerApp();
    }

    @Before
    public void setUp() {
        Log.v(TAG, new Exception().getStackTrace()[0].getMethodName() + "()");

        //UiHelper.launchApp(Config.sTargetPackage);

    }

    @After
    public void tearDown() throws Exception {
        Log.v(TAG, new Exception().getStackTrace()[0].getMethodName() + "()");

        //saveLogcat();
    }

    @Test
    public void testMain() {
        Log.v(TAG, new Exception().getStackTrace()[0].getMethodName() + "()");

        Crawler crawler = new DepthFirstCrawler();

        try {
            crawler.run();
        } catch (IllegalStateException e) {
            Log.v(TAG, "IllegalStateException: UiAutomation not connected!");
        }
    }

    public static void getArguments() {
        Bundle arguments = InstrumentationRegistry.getArguments();
        if (arguments.getString("target") != null) {
            Config.sTargetPackage = arguments.getString("target");
        }
        if (arguments.getString("max-steps") != null) {
            Config.sMaxSteps = Integer.valueOf(arguments.getString("max-steps"));
        }
        if (arguments.getString("max-depth") != null) {
            Config.sMaxDepth = Integer.valueOf(arguments.getString("max-depth"));
        }
        if (arguments.getString("max-runtime") != null) {
            Config.sMaxRuntime = Integer.valueOf(arguments.getString("max-runtime"));
        }
        if (arguments.getString("max-screenshot") != null) {
            Config.sMaxScreenshot = Integer.valueOf(arguments.getString("max-screenshot"));
        }
        if (arguments.getString("max-screensloop") != null) {
            Config.sMaxScreenLoop = Integer.valueOf(arguments.getString("max-screenloop"));
        }
        if (arguments.getString("launch-timeout") != null) {
            Config.sLaunchTimeout = Integer.valueOf((arguments.getString("launch-timeout")));
        }
        if (arguments.getString("waitidle-timeout") != null) {
            Config.sWaitIdleTimeout = Integer.valueOf((arguments.getString("waitidle-timeout")));
        }
        if (arguments.getString("capture-steps") != null) {
            Config.sCaptureSteps = (arguments.getString("capture-steps").compareTo("true") == 0);
        }
        if (arguments.getString("random-text") != null) {
            Config.sRandomText = (arguments.getString("random-text").compareTo("true") == 0);
        }
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        boolean delete = fileOrDirectory.delete();
    }

    public static void logConfiguration() {
        Configurator conf = Configurator.getInstance();

        String log = String.format("ActionAcknowledgmentTimeout:%d," +
                        " KeyInjectionDelay:%d, " +
                        "ScrollAcknowledgmentTimeout:%d," +
                        " WaitForIdleTimeout:%d," +
                        " WaitForSelectorTimeout:%d",
                conf.getActionAcknowledgmentTimeout(),
                conf.getKeyInjectionDelay(),
                conf.getScrollAcknowledgmentTimeout(),
                conf.getWaitForIdleTimeout(),
                conf.getWaitForSelectorTimeout());

        FileLog.i(TAG_MAIN, log);

        FileLog.i(TAG_MAIN, "TargetPackage: " + Config.sTargetPackage +
                ", Debug: " + Config.sDebug +
                ", MaxSteps: " + Config.sMaxSteps +
                ", MaxDepth: " + Config.sMaxDepth +
                ", MaxRuntime: " + Config.sMaxRuntime +
                ", MaxScreenshot: " + Config.sMaxScreenshot +
                ", MaxScreenLoop: " + Config.sMaxScreenLoop +
                ", ScreenSignatueLength: " + Config.sScreenSignatueLength +
                ", RandomText: " + Config.sRandomText +
                ", CaptureSteps: " + Config.sCaptureSteps +
                ", LaunchTimeout: " + Config.sLaunchTimeout +
                ", WaitIdleTimeout: " + Config.sWaitIdleTimeout);
    }

    public static void saveLogcat() {
        File file = new File(Config.sOutputDir + "/AppCrawlerLogcat.log");
        try {
            boolean newFile = file.createNewFile();
            if (newFile) {
                String cmd = "logcat -d -s -v time -f " + file.getAbsolutePath() + " " + TAG;
                Runtime.getRuntime().exec(cmd);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
