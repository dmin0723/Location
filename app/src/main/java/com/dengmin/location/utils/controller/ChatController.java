package com.dengmin.location.utils.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import com.dengmin.location.utils.connect.AcceptThread;
import com.dengmin.location.utils.connect.ConnectThread;
import com.dengmin.location.utils.connect.Protocol;
import java.io.UnsupportedEncodingException;

/**
 * 这是聊天
 */
public class ChatController {

    private ConnectThread mConnectThread = null;//这是客户端
    private AcceptThread mAcceptThread = null; //这是服务端

    //这是编解码的协议
    class ChatProtocol implements Protocol {
        //解码
        @Override
        public byte[] decodeString(String str) {

            byte[] buffer = null;

            if (str == null)
                return new byte[0];

            try {
                buffer = str.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return new byte[0];
            }

            return buffer;

        }

        //编码
        @Override
        public String encodeString(byte[] buffer) {
            if (buffer == null)
                return "";
            String str = "";
            try {
                str = new String(buffer, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "";
            }
            return str;
        }
    }

    ChatProtocol chatProtocol = new ChatProtocol();

    //创建一个客户端
    public void chatWithFriend(BluetoothAdapter mAdapter, BluetoothDevice mDevice, Handler handler) {
        mConnectThread = new ConnectThread(mAdapter, mDevice, handler);
        mConnectThread.start();
    }

    //创建一个服务端
    public void acceptFriend(BluetoothAdapter mAdapter, Handler handler) {
        mAcceptThread = new AcceptThread(mAdapter, handler);
        mAcceptThread.start();
    }

    //发送信息
    public void sendMessage(String msg) {

        if (mConnectThread != null) {
            //传输数据
            mConnectThread.sendData(chatProtocol.decodeString(msg));
        } else if (mAcceptThread != null) {
            //传输数据 客户端的读写
            mAcceptThread.sendData(chatProtocol.decodeString(msg));
        }
    }

    //编码
    public String decodeMessage(byte[] data) {
        return chatProtocol.encodeString(data);
    }

    //停止等待 关闭客户端和服务端
    public void stopChat() {
        if (mConnectThread != null) {
            //关闭客户端
            mConnectThread.cancel();
        } else if (mAcceptThread != null) {
            //关闭服务端
            mAcceptThread.cancel();
        }
    }

    private static ChatController chatController = new ChatController();

    public static ChatController getInstance() {
        return chatController;
    }

}
