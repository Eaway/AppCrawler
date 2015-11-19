package com.eaway.appcrawler.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.util.Log;

import com.eaway.appcrawler.Config;
import com.eaway.appcrawler.FileLog;

import java.io.File;
import java.util.Random;

/**
 * UiAutomator helper
 */
public class UiHelper {
    private static final String TAG = Config.TAG;
    private static final String TAG_MAIN = Config.TAG_MAIN;

    public static int sScreenshotIndex = 0;

    public static String sLastFilename;

    public static UiWatchers sUiWatchers = new UiWatchers();

    public static void registerAnrAndCrashWatchers() {
        sUiWatchers.registerAnrAndCrashWatchers();
    }

    public static boolean handleAndroidUi() {
        if (sUiWatchers.handleAnr()) {
            takeScreenshots("[ANR]");
            return true;
        } else if (sUiWatchers.handleAnr2()) {
            takeScreenshots("[ANR]");
            return true;
        } else if (sUiWatchers.handleCrash()) {
            takeScreenshots("[CRASH]");
            return true;
        } else if (sUiWatchers.handleCrash2()) {
            takeScreenshots("[CRASH]");
            return true;
        } else {
            // Something we don't know
        }

        return false;
    }

    public static boolean handleCommonDialog() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject button = null;
        for (String keyword : Config.COMMON_BUTTONS) {
            button = device.findObject(new UiSelector().text(keyword).enabled(true));
            if (button != null && button.exists()) {
                break;
            }
        }
        try {
            // sometimes it takes a while for the OK button to become enabled
            if (button != null && button.exists()) {
                button.waitForExists(5000);
                button.click();
                Log.i("AppCrawlerAction", "{Click} " + button.getText() + " Button succeeded");
                return true; // triggered
            }
        } catch (UiObjectNotFoundException e) {
            Log.w(TAG, "UiObject disappear");
        }
        return false; // no trigger
    }

    public static void inputRandomTextToEditText() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject edit = null;
        int i = 0;
        do {
            edit = null;
            edit = device.findObject(new UiSelector().className("android.widget.EditText").instance(i++));
            if (edit != null && edit.exists()) {
                try {
                    Random rand = new Random();
                    String text = Config.RANDOM_TEXT[rand.nextInt(Config.RANDOM_TEXT.length - 1)];
                    edit.setText(text);
                } catch (UiObjectNotFoundException e) {
                    // Don't worry
                }
            }
        } while (edit != null && edit.exists());
    }

    public static boolean isInTargetApp() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        String pkg = device.getCurrentPackageName();
        if (pkg != null && 0 == pkg.compareToIgnoreCase(Config.sTargetPackage)) {
            return true;
        }
        return false;
    }

    public static boolean isInIgnoredActivity(UiScreen screen) {
        return isInIgnoredActivity(screen.name);
    }

    public static boolean isInIgnoredActivity(String activityName) {
        for (String ignore : Config.IGNORED_ACTIVITY) {
            if (0 == ignore.compareTo(activityName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInTheSameScreen(UiScreen target) {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject root = device.findObject(new UiSelector().packageName(Config.sTargetPackage));
        if (root == null || !root.exists()) {
            Log.e(TAG, "Fail to get screen root object");
            return false;
        }
        UiScreen current = new UiScreen(null, null, root);
        boolean result = current.equals(target);
        return result;
    }

    public static boolean launchTargetApp() {
        if (launchApp(Config.sTargetPackage)) {
            return true;
        }

        return false;
    }

    public static void launchCrawlerApp() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.pressHome();
        FileLog.i(TAG_MAIN, "{Launch} " + Config.sCrawlerPackage);
        Context context = InstrumentationRegistry.getContext();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(Config.sCrawlerPackage);
        context.startActivity(intent);
    }

    public static boolean launchApp(String targetPackage) {
        FileLog.i(TAG_MAIN, "{Launch} " + targetPackage);

        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        String launcherPackage = device.getLauncherPackageName();
        if (launcherPackage.compareToIgnoreCase(targetPackage) == 0) {
            launchHome();
            return true;
        }
        Context context = InstrumentationRegistry.getContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(targetPackage);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // Make sure each launch is a new task
            context.startActivity(intent);
            device.wait(Until.hasObject(By.pkg(Config.sTargetPackage).depth(0)), Config.sLaunchTimeout);
        } else {
            String err = String.format("(%s) No launchable Activity.\n", targetPackage);
            Log.e(TAG, err);
            Bundle bundle = new Bundle();
            bundle.putString("ERROR", err);
            InstrumentationRegistry.getInstrumentation().finish(1, bundle);
        }
        return true;
    }

    public static void launchHome() {
        FileLog.i(TAG_MAIN, "{Press} Home");

        UiDevice uidevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        uidevice.pressHome();
        String launcherPackage = uidevice.getLauncherPackageName();
        uidevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), Config.sLaunchTimeout);
    }

    public static String toValidFileName(String input) {
        if (input == null)
            return "";
        return input.replaceAll("[:\\\\/*\"?|<>']", "_");
    }

    // Take screenshots in Landscape and Portrait
    // TODO: Take screenshot for both portrait and landscape
    public static void takeScreenshots(String message) {
        //Log.v(TAG, new Exception().getStackTrace()[0].getMethodName() + "()");

        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.waitForIdle(Config.sWaitIdleTimeout);

        String activity = device.getCurrentActivityName(); // FIXME: deprecated
        if (activity == null)
            activity = "No Activity name";
        if (activity.length() > 30) {
            activity = activity.substring(0, 29);
        }

        // Dump window hierarchy for debug, remove it for better performance
        //device.dumpWindowHierarchy(new File(String.format("%s/%d - %s.xml", sOutputDir, sScreenshotIndex, activity)));

        if (message.length() > 50) {
            message = message.substring(0, 49);
        }

        sLastFilename = "";
        if (message.length() > 0) {
            sLastFilename = String.format("(%d) %s %s.png",
                    sScreenshotIndex, toValidFileName(activity), toValidFileName(message));
        } else {
            sLastFilename = String.format("(%d) %s.png",
                    sScreenshotIndex, toValidFileName(activity));
        }
        device.takeScreenshot(new File(Config.sOutputDir + "/" + sLastFilename));
        sScreenshotIndex++;
        FileLog.i(TAG_MAIN, "{Screenshot} " + sLastFilename);
    }

}
