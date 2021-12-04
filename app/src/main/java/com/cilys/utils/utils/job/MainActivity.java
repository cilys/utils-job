package com.cilys.utils.utils.job;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.cilys.utils.job.JobService;
import com.cilys.utils.job.impl.WorkResultListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private WifiManager wifiManager;
    List<WiFIBean> datas = new ArrayList<>();
    WiFiListAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DbUtils.init(this, true);

        final Button btn = findViewById(R.id.createPwd);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JobService.getInstance().addTask(new CreatePwdRunnable(new WorkResultListener() {
                    @Override
                    public void onSuccess(String s, Object o) {

                    }

                    @Override
                    public void onFailure(String s, String s1, String s2) {

                    }

                    @Override
                    public void inProgress(String key, final long currentProgress, final long totalProgress) {
                        super.inProgress(key, currentProgress, totalProgress);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn.setText(currentProgress + " / " + totalProgress);
                            }
                        });
                    }
                }));
            }
        });

        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        connectedWifi();

                findViewById(R.id.scan).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                scan();
            }
        });
        adapter = new WiFiListAdapter(datas);

        ListView lv = findViewById(R.id.wifi_result);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                WiFiUtils.connectWifi(wifiManager, datas.get(position).SSID, "12345678", datas.get(position).type);
                selectedNeedSSID = datas.get(position).SSID;
                conn(datas.get(position).SSID, datas.get(position).type);
            }
        });

        registerReceiver();
    }

    private String selectedNeedSSID;

    private ConnWifiRunnable connWifiRunnable;
    private void conn(String ssid, int type){
        connWifiRunnable = new ConnWifiRunnable(wifiManager, ssid, type);
        JobService.getInstance().addTask(connWifiRunnable);
    }

    private void connectedWifi(){
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info != null && wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            Log.i("TAG", "connected ssid = " + info.getSSID());
        } else {
            Log.w("TAG", "None wifi ..");
        }
    }

    private void scan(){
        ScanWifiRunnable runnable = new ScanWifiRunnable(wifiManager, new WorkResultListener<List<ScanResult>>() {
            @Override
            public void onSuccess(String s, List<ScanResult> scanResults) {
                if (scanResults == null || scanResults.size() < 1) {
                    Log.i("TAG", "Wifi list is null..");
                    return;
                }
                for (ScanResult r : scanResults) {
                    Log.i("TAG", "ssid：" + r.SSID);
                    Log.i("TAG", "BSSID：" + r.BSSID);
                    Log.i("TAG", "capabilities：" + r.capabilities);
                    Log.i("TAG", "frequency：" + r.frequency);
                    Log.i("TAG", "level：" + r.level);
                    Log.i("TAG", "------------------------------------------");
                }

                if (datas == null) {
                    datas = new ArrayList<>();
                }
                datas.clear();

                Iterator<ScanResult> it = scanResults.iterator();
                while (it.hasNext()) {
                    ScanResult r = it.next();
                    if (r.SSID == null) {
                        it.remove();
                        continue;
                    }
                    WiFIBean bean = new WiFIBean();
                    bean.SSID = r.SSID.equals("") ? r.BSSID : r.SSID;
                    bean.level = r.level;
                    bean.type = type(r.capabilities);

                    datas.add(bean);
                }
                Collections.sort(datas);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onFailure(String s, final String s1, final String s2) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(s2);
                    }
                });
            }
        });
        JobService.getInstance().addTask(runnable);
    }

    private int type(String capabilities){
        if (capabilities.contains("WEP")) {
            return WiFIBean.TYPE_WEP;
        } else if (capabilities.contains("WPA") || capabilities.contains("PSK")) {
            return WiFIBean.TYPE_WPA;
        }
        return WiFIBean.TYPE_NOPASS;
    }

    private void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private BroadcastReceiver receiver;
    private void registerReceiver(){
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra("wifi_state", 0);
                NetworkInfo info = intent.getParcelableExtra("networkInfo");
                if (info.getState() == NetworkInfo.State.CONNECTED){
                    Log.i("TAG", "成功连接到：" + info.getExtraInfo());

                    if (selectedNeedSSID != null && selectedNeedSSID.equals(info.getExtraInfo().replace("\"", ""))) {
                        if (connWifiRunnable != null) {
                            connWifiRunnable.stop();
                        }
                    }

                    connectedWifi();
                } else {
                    Log.w("TAG", "未成功连接到：" + info.getExtraInfo());
                }
            }
        };
        IntentFilter filter = new IntentFilter();
//        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
//        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
//        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        this.registerReceiver(receiver, filter);
    }

    private void unregister(){
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregister();
    }
}
