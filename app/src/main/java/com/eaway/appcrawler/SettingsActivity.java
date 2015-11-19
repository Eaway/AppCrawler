package com.eaway.appcrawler;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class SettingsActivity extends AppCompatActivity {
    public static final String TAG = "AppCrawlerTool";
    private EditText mEditTextWaitTimeout;
    private EditText mEditTextMaxDepth;
    private FileWriter mLogWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mEditTextWaitTimeout = (EditText) findViewById(R.id.editTextWaitTimeout);
        mEditTextMaxDepth = (EditText) findViewById(R.id.editTextMaxDepth);

    }

    public void onStartButtonClick(View view) {
        // Start thread
        new Thread() {
            public void run() {
                Log.d(TAG, "onStartButtonClick.Thread.run");
                for (TargetApp app : MainActivity.sSelectedAppList) {
                    try {
                        String filePath = "/sdcard/AppCrawler/" + app.pkg + "instrument.log";
                        File logFile = new File(filePath);
                        logFile.createNewFile();
                        mLogWriter = new FileWriter(logFile);

                        String cmd = String.format("am instrument -e target %s -w com.eaway.appcrawler.test/android.support.test.runner.AndroidJUnitRunner", app.pkg);
                        Log.d(TAG, cmd);
                        Process p = Runtime.getRuntime().exec(cmd);

                        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        String line = null;
                        while ((line = in.readLine()) != null) {
                            line += "\r\n";
                            mLogWriter.write(line += "\r\n");
                        }

                        mLogWriter.close();
                    } catch (IOException e) {
                    }
                }
            }
        }.start();
    }
}
