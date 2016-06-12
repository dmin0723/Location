package com.dengmin.location.utils.connect;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import com.dengmin.location.adapter.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * 这是一个线程专门用于数据的读或写的线程
 * 数据传输的大致流程如下：
 首先，分别通过getInputStream()和getOutputStream()获得管理数据传输的InputStream和OutputStream。
 然后，开辟一个线程专门用于数据的读或写。这是非常重要的，因为read(byte[])和write(byte[])方法都是
 阻塞调用。read(byte[])从输入流(InputStream)中读取数据。write(byte[])将数据写入到OutputStream流中去，
 这个方法一般不会阻塞，但当远程设备的中间缓冲区已满而对方没有及时地调用read(byte[])时将会一直阻塞。
 所以，新开辟的线程中的主循环将一直用于从InputStream中读取数据。

 getInputStream()——获得一个可读的流，该流在连接不成功的情况下依旧可以获得，但是对其操作的话就
 会报IOException的异常。需要从外部获取的数据都从该流中获取。

 getOutputStrem()——获得一个可写的流，该流在连接不成功的情况下依旧可以获得，但是对其操作的话就
 会报IOException的异常。需要往外部传输的数据都可以写到该流中传输出去。

 数据的传输的相关操作最重要的其实是多线程的操作和数据流的操作
 */
public class ConnectedThread extends Thread {

    private Handler handler;
    private BluetoothSocket mmSocket;//客户端

    private InputStream inputStream;
    private OutputStream outputStream;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {

        this.handler = handler;

        this.mmSocket = socket;

    }

    @Override
    public void run() {
        // 不断循环以读取数据
        while (true) {
            try {
                // buffer store for the stream
                byte[] buffer = new byte[1024];
                inputStream = mmSocket.getInputStream();
                if (inputStream.available() > 0) {
                    // 读取数据
                    inputStream.read(buffer);
                    // 将读取的数据信息发送至UI线程并显示数据
                    Message msg = new Message();
                    msg.obj = buffer;
                    msg.what = Constant.MSG_GOT_DATA;//获取到数据
                    handler.sendMessage(msg);
                }
            } catch (IOException e) {
                try {
                    mmSocket.close();
                    break;
                } catch (IOException e1) {
                }

            }
        }
    }

    //客户端的写
    //写入数据
    public void write(byte[] buffer) {
        try {
            outputStream = mmSocket.getOutputStream();
            outputStream.write(buffer);

        } catch (IOException e) {
            handler.sendEmptyMessage(Constant.MSG_ERROR);
        }
    }

    //客户端的关闭
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
