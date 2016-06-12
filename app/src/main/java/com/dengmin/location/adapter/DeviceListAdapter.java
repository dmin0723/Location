
package com.dengmin.location.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * 显示设备的列表的适配器
 */
public class DeviceListAdapter extends BaseAdapter {

    private List<BluetoothDevice> data;
    private Context context;

    public DeviceListAdapter(List<BluetoothDevice> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;

        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        TextView line1 = (TextView) itemView.findViewById(android.R.id.text1);
        TextView line2 = (TextView) itemView.findViewById(android.R.id.text2);

        BluetoothDevice device = (BluetoothDevice) getItem(position);

        //显示名称
        line1.setText(device.getName());
        //显示地址
        line2.setText(device.getAddress());

        return itemView;
    }

    //刷新相应的设备列表
    public void refresh(List<BluetoothDevice> data) {
        this.data = data;
        notifyDataSetChanged();
    }
}
