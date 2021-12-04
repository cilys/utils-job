package com.cilys.utils.utils.job;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.cilys.utils.job.WorkJobRunnable;
import com.cilys.utils.job.impl.WorkResultImpl;

import java.util.List;

public class ScanWifiRunnable extends WorkJobRunnable<List<ScanResult>> {
    private WifiManager wifiManager;

    public ScanWifiRunnable(WifiManager wifiManager, WorkResultImpl<List<ScanResult>> impl) {
        super("SCAN_WIFI", impl);
        this.wifiManager = wifiManager;
    }

    @Override
    public void work() {
        if (wifiManager == null) {
            onFailure("-10001", "WifiManager is null..");
            return;
        }
        if (!wifiManager.isWifiEnabled()) {
            onFailure("-10002", "The wifi is disable..");
            return;
        }
        List<ScanResult> results = wifiManager.getScanResults();
        onSuccess(results);
    }
}
