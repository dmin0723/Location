package com.dengmin.location.utils.controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

/**
 * 这是蓝牙的配置
 */
public class BluetoothController {

    private BluetoothAdapter mAdapter;//本地设备

    /**
     * 蓝牙适配器类构造函数
     */
    public BluetoothController() {
        // 初始化 获取本地的蓝牙适配器实例
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * 获取蓝牙适配器（BluetoothAdapter）
     *
     * @return BluetoothAdapter
     */
    public BluetoothAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * 打开蓝牙
     *
     * @param activity    调用函数前的activity
     * @param requestCode 响应码
     */

    //通过这个方法来请求打开我们的蓝牙设备
    public void turnOnBlueTooth(Activity activity, int requestCode) {
        //会以Dialog样式显示一个Activity ， 我们可以在onActivityResult()方法去处理返回值
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 设置可见性
     *出现对话框
     * @param context
     */
    //发送ACTION_REQUEST_DISCOVERABLE广播, 同时在EXTRA_DISCOVERABLE_DURATION附加域中加入可见时间, 单位是秒;
    //发送这个广播, 会弹出一个对话框, 显示是否可见300秒;
    public void enableVisibly(Context context) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        ///300这个参数代表的是蓝牙设备能在多少秒内被发现
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        context.startActivity(intent);
    }


    /**
     * 查找设备
     */
    //开始查找远程蓝牙设备, 先进行12秒的查询扫描(被动可见), 之后进行页面扫描(主动搜索);
    // 搜索过成功不能尝试对远程设备的连接, 同时已连接的设备的带宽也会被压缩, 等待时间变长;
    // 使用cancelDiscovery()可以终止搜索;
    public void findDevice() {
        //断言当前设备具备蓝牙功能
        assert (mAdapter != null);
        mAdapter.startDiscovery();
    }


    /**
     * 获取已绑定的蓝牙设备
     *
     * @return  List
     * 通过getBondedDevices方法来获取已经与本设备配对的设备
     */
    //启用了蓝牙功能之后，可以通过调用 getBondedDevices()方法来获取配对设备列表。它返回一组的蓝牙设备。
    public List<BluetoothDevice> getBondedDeviceList() {
        return new ArrayList<>(mAdapter.getBondedDevices());
    }

}
