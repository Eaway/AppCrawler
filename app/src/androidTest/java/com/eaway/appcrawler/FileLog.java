package com.eaway.appcrawler;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * File Log
 *
 * Save log to file and logcat.
 * Why not use logcat only: "logcat -f file" will result the log been cleaned, but we want to keep them.
 */
public class FileLog {
    private static synchronized void write(char lv, String tag, String msg) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(Config.sFileLog, true)));
            if (writer != null)
                writer.println(String.format("%s %c/%s: %s", sdf.format(new Date()), lv, tag, msg));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    public static int v(String tag, String msg) {
        write('v', tag, msg);
        return Log.v(tag, msg);
    }

    public static int w(String tag, String msg) {
        write('w', tag, msg);
        return Log.w(tag, msg);
    }

    public static int e(String tag, String msg) {
        write('e', tag, msg);
        return Log.e(tag, msg);
    }

    public static int i(String tag, String msg) {
        write('i', tag, msg);
        return Log.i(tag, msg);
    }

    public static int d(String tag, String msg) {
        write('d', tag, msg);
        return Log.d(tag, msg);
    }

}
