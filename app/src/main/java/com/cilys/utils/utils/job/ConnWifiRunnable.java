package com.cilys.utils.utils.job;

import android.net.wifi.WifiManager;
import android.util.Log;

import com.cilys.utils.job.WorkJobRunnable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayDeque;

public class ConnWifiRunnable extends WorkJobRunnable {


    private int index = 0;
    private WifiManager wifiManager;
    private String ssid;
    private int type;
    private final int lastIndex;

    public ConnWifiRunnable(WifiManager wifiManager, String ssid, int type) {
        super("CONN_WIFI_RUNNABLE", null);
        this.wifiManager = wifiManager;
        this.ssid = ssid;
        this.type = type;
        this.lastIndex = 756;
    }

    @Override
    public void work() {
        ArrayDeque<String> pwdsQueue = new ArrayDeque<>();

        try {
            FileReader fileReader = new FileReader("/mnt/sdcard/pwds/common.txt");
            BufferedReader br = new BufferedReader(fileReader);

            String str;
            Log.i("TAG", "开启读取密码文件..");
            while ((str = br.readLine()) != null && isRunning()) {
                str = str.trim();
                pwdsQueue.offer(str);
            }
            Log.i("TAG", "读取密码成功，总共" + pwdsQueue.size() + "个密码..");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String pwd;
        while (isRunning() && (pwd = pwdsQueue.poll()) != null) {
            if (index < lastIndex) {

            } else {
                WiFiUtils.connectWifi(wifiManager, ssid, pwd, type);
                Log.i("TAG", "第" + index + "次尝试连接，密码：" + pwd);

                try {
                    Thread.sleep(8 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            index++;
        }
        Log.i("TAG", "任务结束：index = " + index);
        Log.i("TAG", "任务结束：isRunning = " + isRunning());
    }
}
