package com.eaway.appcrawler.performance;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CPU info
 */
public class CpuInfo {

    static float totalCpuTime1 = -1f;
    static float processCpuTime1 = -1f;

    public static float getProcessCpuRate(int pid) {

        if (totalCpuTime1 == -1f) {
            totalCpuTime1 = getTotalCpuTime();
            processCpuTime1 = getAppCpuTime(pid);
            try {
                Thread.sleep(360);
            } catch (Exception e) {
            }
        }

        float totalCpuTime2 = getTotalCpuTime();
        float processCpuTime2 = getAppCpuTime(pid);

        float cpuRate = 100 * (processCpuTime2 - processCpuTime1) / (totalCpuTime2 - totalCpuTime1);

        totalCpuTime1 = totalCpuTime2;
        processCpuTime1 = processCpuTime2;

        return cpuRate;
    }

    public static long getTotalCpuTime() {
        String[] cpuInfos = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        long totalCpu = Long.parseLong(cpuInfos[2]) + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4]) + Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5]) + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
        return totalCpu;
    }

    public static long getAppCpuTime(int pid) {
        String[] cpuInfos = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/" + pid + "/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        long appCpuTime = Long.parseLong(cpuInfos[13]) + Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15]) + Long.parseLong(cpuInfos[16]);
        return appCpuTime;
    }
}
