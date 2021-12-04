package com.cilys.utils.utils.job;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class WiFiListAdapter extends BaseAdapter {
    private List<WiFIBean> datas;

    public WiFiListAdapter(List<WiFIBean> datas) {
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas == null ? 0 : datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        VH vh;
        if (v == null) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wifi, parent, false);
            vh = new VH();
            vh.name = v.findViewById(R.id.name);
            vh.level = v.findViewById(R.id.level);
            v.setTag(vh);
        } else {
            vh = (VH)v.getTag();
        }
        vh.name.setText(datas.get(position).SSID + "(" + type(datas.get(position).type) + ")");
        vh.level.setText(datas.get(position).level + "");
        return v;
    }

    private String type(int type){
        if (type == WiFIBean.TYPE_WEP) {
            return "WEP";
        } else if (type == WiFIBean.TYPE_WPA) {
            return "WPA/WPA2";
        }
        return "NOPASS";
    }

    private static class VH {
        TextView name, level;
    }
}
