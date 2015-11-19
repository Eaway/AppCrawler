package com.eaway.appcrawler;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {

    public static final String TAG = "AppCrawlerTool";

    private static final String APP_NAME = "name";
    private static final String APP_PKG = "pkg";
    private static final String APP_ICON = "icon";

    private static final String PKG_PREFIX_ANDROID = "com.android";
    private static final String PKG_PREFIX_GOOGLE = "com.google";

    private ListView mListView;
    private RadioButton mRadioBtnPackage;
    private RadioButton mRadioBtnName;
    private CheckBox mCheckBoxHideAndroid;
    private CheckBox mCheckBoxHideGoogle;

    private PackageManager mPkgMgr;
    private PackageInfo mPkgInfo;

    public static List<PackageInfo> sPkgInfoList;
    public static List<TargetApp> sSelectedAppList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPkgMgr = getPackageManager();
        try {
            mPkgInfo = mPkgMgr.getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        mListView = (ListView) findViewById(R.id.appList);
        mRadioBtnPackage = (RadioButton) findViewById(R.id.radioPackage);
        mRadioBtnName = (RadioButton) findViewById(R.id.radioName);
        mCheckBoxHideAndroid = (CheckBox) findViewById(R.id.checkBoxHideAndroid);
        mCheckBoxHideGoogle = (CheckBox) findViewById(R.id.checkBoxHideGoogle);

        refreshAppListView();
    }

    public void onHideButtonClick(View view) {
        refreshAppListView();
    }

    public void onSortButtonClick(View view) {
        if (sPkgInfoList != null) {
            if (mRadioBtnPackage.isChecked()) {
                Collections.sort(sPkgInfoList, new Comparator<PackageInfo>() {
                    public int compare(PackageInfo p1, PackageInfo p2) {
                        return p1.packageName.compareTo(p2.packageName);
                    }
                });
            } else {
                Collections.sort(sPkgInfoList, new Comparator<PackageInfo>() {
                    public int compare(PackageInfo p1, PackageInfo p2) {
                        String n1 = (String) p1.applicationInfo.loadLabel(mPkgMgr);
                        String n2 = (String) p2.applicationInfo.loadLabel(mPkgMgr);
                        return n1.compareTo(n2);
                    }
                });
            }
            refreshAppListView();
        }
    }

    public void onStartButtonClick(View view) {

        // Get selected app list
        sSelectedAppList = new ArrayList<TargetApp>();
        for (int i = 0; i < mListView.getChildCount(); i++) {
            LinearLayout itemLayout = (LinearLayout) mListView.getChildAt(i);
            CheckBox cb = (CheckBox) itemLayout.findViewById(R.id.checkBox);
            if (cb.isChecked()) {
                TextView pkg = (TextView) itemLayout.findViewById(R.id.appPackage);
                TextView name = (TextView) itemLayout.findViewById(R.id.appName);
                TargetApp app = new TargetApp((String) name.getText(), (String) pkg.getText());
                sSelectedAppList.add(app);
            }
        }

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private boolean refreshAppListView() {

        // Get installed packages
        ArrayList<HashMap<String, Object>> appList = new ArrayList<HashMap<String, Object>>();
        if (sPkgInfoList == null) {
            sPkgInfoList = mPkgMgr.getInstalledPackages(0);
        }

        for (PackageInfo pkg : sPkgInfoList) {

            // Skip Ourself
            if (pkg.packageName.equalsIgnoreCase(getPackageName()))
                continue;

            // Skip Android packages
            if (mCheckBoxHideAndroid.isChecked()) {
                if (pkg.packageName.contains(PKG_PREFIX_ANDROID))
                    continue;
            }

            // Skip Google packages
            if (mCheckBoxHideGoogle.isChecked()) {
                if (pkg.packageName.contains(PKG_PREFIX_GOOGLE))
                    continue;
            }

            HashMap<String, Object> mapApp = new HashMap<String, Object>();
            mapApp.put(APP_PKG, pkg.packageName);
            mapApp.put(APP_NAME, pkg.applicationInfo.loadLabel(mPkgMgr));
            mapApp.put(APP_ICON, pkg.applicationInfo.loadIcon(mPkgMgr));
            appList.add(mapApp);
        }

        // Bind ListView with content adapter
        SimpleAdapter appAdapter = new SimpleAdapter(this, appList, R.layout.app_list_item,
                new String[] {
                        APP_NAME, APP_PKG, APP_ICON
                },
                new int[] {
                        R.id.appName, R.id.appPackage, R.id.appIcon
                });

        appAdapter.setViewBinder(new ViewBinder() {
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView && data instanceof Drawable) {
                    ImageView iv = (ImageView) view;
                    iv.setImageDrawable((Drawable) data);
                    return true;
                }
                else
                    return false;
            }
        });

        mListView.setAdapter(appAdapter);

        return true;
    }

}