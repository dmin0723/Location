package com.dengmin.location.utils.connect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import com.dengmin.location.adapter.Constant;

import java.io.IOException;
import java.util.UUID;


/**
 *
 * 这是客户端的连接线程
 */
public class ConnectThread extends Thread {

    private BluetoothAdapter mAdapter; //本地设备
    private BluetoothDevice mDevice; //远程设备
    private BluetoothSocket mmSocket; //这是客户端
    private Handler handler;
    private ConnectedThread mConnectedThread;//读写线程

    //UUID是“Universally Unique Identifier”的简称，通用唯一识别码的意思。
    //这是蓝牙串口服务
    //UUID(Universally Unique Identifier)是一个128位的字符串ID，被用于唯一标识我们的蓝牙服务。
    private UUID myUUID = UUID.fromString(Constant.CONNECTTION_UUID);

    public ConnectThread(BluetoothAdapter mAdapter, BluetoothDevice mDevice, Handler handler) {
        this.mAdapter = mAdapter;
        this.mDevice = mDevice; //通过构造函数来传入一个BluetoothDevice实例
        this.handler = handler;

        try {
            //通过BluetoothDevice实例的createRfcommSocketToServiceRecord方法可以返回一个带有UUID的BluetoothSocket实例
            mmSocket = mDevice.createRfcommSocketToServiceRecord(myUUID); //这是创建一个客户端
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        //取消本地蓝牙设备的搜索操作，如果本地设备正在进行搜索，那么调用该方法后将停止搜索操作。
        mAdapter.cancelDiscovery();

        try {
            //主动向服务端（监听端）发起连接请求。
            mmSocket.connect();
        } catch (Exception connectException) {
            handler.sendMessage(handler.obtainMessage(Constant.MSG_ERROR, connectException));
            try {
                //关闭BluetoothSocket请求端。
                mmSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        //连接到服务器 开启读写线程
        ManagerSocket(mmSocket);
    }

    public void sendData(byte[] data) {
        //这是一个线程专门用于数据的写的线程
        mConnectedThread.write(data); //客户端的写
    }

    //连接到服务器 开启读写线程
    private void ManagerSocket(BluetoothSocket mmSocket) {
        handler.sendEmptyMessage(Constant.MSG_CONNECTED_TO_SERVER);//连接到服务器
        mConnectedThread = new ConnectedThread(mmSocket,  handler);//这是读写线程
        mConnectedThread.start();
    }

    //这是客户端的关闭
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
        }
    }
}
