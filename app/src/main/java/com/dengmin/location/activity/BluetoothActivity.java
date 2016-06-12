package com.dengmin.location.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dengmin.location.R;
import com.dengmin.location.adapter.Constant;
import com.dengmin.location.adapter.DeviceListAdapter;
import com.dengmin.location.utils.controller.BluetoothController;
import com.dengmin.location.utils.controller.ChatController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 这是蓝牙模块
 */

public class BluetoothActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 0;
    // 获取本地的蓝牙适配器实例
    private BluetoothController mController = new BluetoothController();

    //显示设备
    private List<BluetoothDevice> mDeviceList = new ArrayList<>();
    //显示绑定的设备
    private List<BluetoothDevice> mBondedDeviceList = new ArrayList<>();

    private ListView mListview;
    //自定义的adapter
    private DeviceListAdapter mAdapter;
    private Toast mToast;

    private Button mSendBt;
    private EditText mInputBox;
    private TextView mChatContent;
    private RelativeLayout chatPanel;

    private StringBuilder mChatText = new StringBuilder();

    private MyHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bluetooth);

        //这是显示对话框和设别列表的ui部分
        initUI();

        //这是设置设备列表和对话列表的可见性
        BluetoothConfigMode();

        //登记蓝牙接收器
        //要在onDestory（）中取消绑定，不然出现线程阻塞
        registerBlueToothReceiver();
        //通过这个方法来请求打开我们的蓝牙设备
        mController.turnOnBlueTooth(this, REQUEST_CODE);
    }

    //这是显示对话框和设别列表的ui部分
    private void initUI() {

        handler = new MyHandler();
        //显示设备的列表的适配器
        mAdapter = new DeviceListAdapter(mDeviceList, this);
        mListview = (ListView) findViewById(R.id.device_list);
        mSendBt = (Button) findViewById(R.id.bt_send);
        mSendBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //时间的显示格式
                SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                String ext = "[" + df.format(new Date()) + "] "
                        + mController.getAdapter().getName()    //显示当前蓝牙设备的名字
                        + "\n" + mInputBox.getText().toString();    //显示输入的文字
                //发送信息
                ChatController.getInstance().sendMessage(ext);

                mChatText.append(ext + "\n");
                mChatContent.setText(mChatText);
                //将输入端设置为空
                mInputBox.setText("");
            }
        });

        //对话框的
        chatPanel = (RelativeLayout) findViewById(R.id.chat_panel);
        mInputBox = (EditText) findViewById(R.id.chat_edit);
        mChatContent = (TextView) findViewById(R.id.chat_content);

        //这是设备的列表
        mListview.setAdapter(mAdapter);
        //绑定相应的设备
        mListview.setOnItemClickListener(bindDeviceClick);
    }

    //登记蓝牙接收器
    //我们通过Filter来过滤ACTION_FOUND这个 Intent动作以获取每个远程设备的详细信息，
    // 通过Intent字段EXTRA_DEVICE 和 EXTRA_CLASS可以获得包含了每个BluetoothDevice 对象
    // 和对象的该设备类型 BluetoothClass。
    private void registerBlueToothReceiver() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //要在onDestory（）中取消绑定，不然出现线程阻塞
        registerReceiver(mReceiver, intentFilter);

    }

    //使用了广播接收器
    //注册一个 BroadcastReceiver 对象来接收查找到的蓝牙设备信息
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                setProgressBarIndeterminateVisibility(true);
                //初始化数据列表
                mDeviceList.clear();
                mAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //找到一个，添加一个
                mDeviceList.add(device);
                mAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0);
                if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    setProgressBarIndeterminateVisibility(true);
                } else {
                    setProgressBarIndeterminateVisibility(false);
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (remoteDevice == null) {
                    showToast("no device");
                    return;
                }
                int status = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);
                if (status == BluetoothDevice.BOND_BONDED) {
                    showToast("Bonded " + remoteDevice.getName());
                } else if (status == BluetoothDevice.BOND_BONDING) {
                    showToast("Bonding " + remoteDevice.getName());
                } else if (status == BluetoothDevice.BOND_NONE) {
                    showToast("Not bond " + remoteDevice.getName());
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChatController.getInstance().stopChat();
        //要在onDestory（）中取消绑定，不然出现线程阻塞
        unregisterReceiver(mReceiver);
    }

    //可以在Activity中的onActivityResult()方法中处理结果,
    // 如果蓝牙模块打开成功, 则返回结果吗RESULT_OK;
    // 如果蓝牙模块打开失败, 则返回结果码RESULT_CANCELED;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode != RESULT_OK)
                finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_bluetooth, menu);
        return true;
    }

    //处理相应的menu事件及点击
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //这是设置本机的蓝牙可见 时间是300s
        if (id == R.id.enable_visiblity) {
            mController.enableVisibly(this);

        } else if (id == R.id.find_device) {
            //查找设备
            //刷新设备相应的列表
            mAdapter.refresh(mDeviceList);
            //开始查找设备
            mController.findDevice();
            //点击后，绑定相应的设备
            mListview.setOnItemClickListener(bindDeviceClick);
            BluetoothConfigMode();
        }

        //查看已绑定的设备
        if (id == R.id.bonded_device) {
            //绑定设备,返回一组的蓝牙设备
            mBondedDeviceList = mController.getBondedDeviceList();
            //刷新已绑定的设备列表
            mAdapter.refresh(mBondedDeviceList);
            mListview.setOnItemClickListener(connectDeviceClick);
            BluetoothConfigMode();
        }
        //等待连接
        if (id == R.id.listening) {
            //创建一个服务端
            ChatController.getInstance().acceptFriend(mController.getAdapter(), handler);
        }
        //停止等待
        if (id == R.id.stop_listening) {
            ChatController.getInstance().stopChat();
        }
        if (id == R.id.disconnect) {
            BluetoothConfigMode();
            mChatContent.setText("");
        }

        return super.onOptionsItemSelected(item);
    }

    //toast的提示功能
    private void showToast(String text) {

        if (mToast == null) {
            mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        } else {
            mToast.setText(text);
        }
        mToast.show();
    }

    //显示对话框
    private void ChatMode() {
        mListview.setVisibility(View.GONE);
        chatPanel.setVisibility(View.VISIBLE);

    }

    //这是显示设备列表
    private void BluetoothConfigMode() {
        chatPanel.setVisibility(View.GONE);
        mListview.setVisibility(View.VISIBLE);
    }

    //绑定相应的设备
    private AdapterView.OnItemClickListener bindDeviceClick = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = mDeviceList.get(position);
            //至少4.4 即api19
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                device.createBond();
            }
        }
    };

    //这是已绑定的设备，点击设备后创建一个客户端
    private AdapterView.OnItemClickListener connectDeviceClick = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = mBondedDeviceList.get(position);
            //创建一个客户端
            ChatController.getInstance().chatWithFriend(mController.getAdapter(), device, handler);
        }
    };

    //处理
    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.MSG_CONNECTED_TO_SERVER:
                    showToast("Connection Success");
                    ChatMode();
                    break;
                case Constant.MSG_GOT_A_CLINET:
                    showToast("Got a client");
                    ChatMode();
                    break;
                case Constant.MSG_FINISH_LISTENING:
                    //cancelDiscovery()取消本地蓝牙设备的搜索操作，如果本地设备正在进行搜索，那么调用该方法后将停止搜索操作。
                    mController.getAdapter().cancelDiscovery();
                    break;
                case Constant.MSG_START_LISTENING:
                    showToast("Waiting for Client");
                    break;
                // MSG_GOT_DATA 获取到数据
                case Constant.MSG_GOT_DATA:
                    //编码
                    String ext = ChatController.getInstance().decodeMessage((byte[]) msg.obj);
                    mChatText.append(ext + "\n");
                    mChatContent.setText(mChatText);
                    break;
            }
        }
    }
}
