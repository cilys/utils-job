package com.cilys.utils.utils.job;

import java.io.Serializable;

public class WiFIBean implements Serializable, Comparable<WiFIBean> {
    public final static int TYPE_NOPASS = 0;
    public final static int TYPE_WEP = 1;
    public final static int TYPE_WPA = 2;
    public String SSID;
    public int type;
    public int level;

    @Override
    public int compareTo(WiFIBean o) {
        int n = this.level - o.level;

        if (n > 0) {
            return -1;
        } else if (n < 0) {
            return 1;
        } else {
            return 0;
        }
    }
}
