package com.dengmin.location.utils.connect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import com.dengmin.location.adapter.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 *
 * 这是服务端
 */
public class AcceptThread extends Thread {

    private BluetoothAdapter mAdapter;
    private Handler handler;
    private BluetoothServerSocket mmServerSocket; //服务端

    private OutputStream output;
    private InputStream input;

    private ConnectedThread mConnectedThread; //读写

    //UUID是“Universally Unique Identifier”的简称，通用唯一识别码的意思。
    //这是蓝牙串口服务
    private final UUID myUUID = UUID.fromString(Constant.CONNECTTION_UUID);

    /**
     *
     * @param mAdapter
     * @param handler
     */
    public AcceptThread(BluetoothAdapter mAdapter, Handler handler) {
        this.mAdapter = mAdapter;
        this.handler = handler;

        try {
            //sdp服务器名称
            //例如：public BluetoothServerSocket listenUsingRfcommonWithServiceRecord(String name, UUID uuid);
            //作用 : 创建一个监听Rfcommon端口的蓝牙监听, 使用accept()方法监听, 并获取BluetoothSocket对象;
            //        该系统会根据一个服务名称(name)和唯一的识别码(uuid)来创建一个SDP服务,
            //        远程蓝牙设备可以根据唯一的UUID来连接这个SDP服务器;
            //参数 : name : SDP服务器名称, UUID, SDP记录下的UUID;
            //返回值 : 正在监听蓝牙端口;
            //权限 : BLUETOOTH;

            // 要建立一个ServerSocket对象，需要使用adapter.listenUsingRfcommWithServiceRecord方法
            // UUID可以在网上去申请
            mmServerSocket = mAdapter.listenUsingRfcommWithServiceRecord("BluetoothChat", myUUID);

        } catch (IOException e) {
        }

    }

    @Override
    public void run() {

        BluetoothSocket socket = null; //客户端
        while (true) {
            try {
                //结束监听
                handler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);
               // 阻塞宿主线程，直至收到客户端请求。返回BluetoothSocket对象
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                //出错
                handler.sendEmptyMessage(Constant.MSG_ERROR);
                break;
            }

            if (socket != null) {
                try {
                    //这是客户端 开启读写线程
                    managerSocket(socket);
                    //这是服务端关闭
                    mmServerSocket.close();
                    //结束监听
                    handler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }

        }
    }

    //蓝牙通讯是基于蓝牙Socket来完成。而Socket又分为2部分：客户端（BluetoothSocket） 和 服务端（BluetoothServerSocket） 。
    //这是客户端
    private void managerSocket(BluetoothSocket socket) {
        if (mConnectedThread != null) {
            mConnectedThread.cancel(); //客户端的关闭
        }

        handler.sendEmptyMessage(Constant.MSG_GOT_A_CLINET); //有客户端连接
        mConnectedThread = new ConnectedThread(socket, handler); //这是读写线程
        mConnectedThread.start();
    }

    //传输数据
    public void sendData(byte[] data) {
        mConnectedThread.write(data); //客户端的写
    }

    //关闭服务端
    public void cancel() {
        try {
            mmServerSocket.close();
            handler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);//结束监听
        } catch (IOException e) {
            handler.sendEmptyMessage(Constant.MSG_ERROR);
            e.printStackTrace();
        }
    }

}
