package com.hiconics_dl.yadi.ui.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;

import com.blankj.utilcode.util.LogUtils;
import com.example.bleyadeasmartbikelib.BleService;
import com.hiconics_dl.yadi.R;
import com.hiconics_dl.yadi.base.BaseToolBarActivity;
import com.hiconics_dl.yadi.ui.dialog.BleConnectDialog;
import com.hiconics_dl.yadi.utils.StringUtil;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseToolBarActivity {

    public static BleService sBleService;

    @BindView(R.id.iv_ble_connect)
    ImageView ivBleConnect;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        showToolBar(false);

        Intent intent = new Intent(context, BleService.class);
        bindService(intent, bleServiceConnection, BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_SELF_COMMAND_AVAILABLE);
        intentFilter.addAction(BleService.ACTION_GATT_HAS_TRANSED);
        intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);

        LocalBroadcastManager.getInstance(context).registerReceiver(BLEStatusChangeReceiver, intentFilter);
    }


    private final BroadcastReceiver BLEStatusChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BleService.ACTION_GATT_CONNECTED)) {
                showToast("已连接上蓝牙设备");
                sBleService.mConnectionState = BleService.STATE_CONNECTED;
                ivBleConnect.setImageResource(R.mipmap.bluetooth_blue);
            } else if(action.equals(BleService.ACTION_GATT_DISCONNECTED)) {
                showToast("已断开蓝牙设备");
                sBleService.mConnectionState = BleService.STATE_DISCONNECTED;
                ivBleConnect.setImageResource(R.mipmap.bluetooth_gray);
            } else if(action.equals(BleService.ACTION_GATT_SELF_COMMAND_AVAILABLE)) {
                byte[] resultDatas = intent.getByteArrayExtra(BleService.EXTRA_DATA_SELF_COMMAND);
//                LogUtils.e(StringUtil.byteArray2HexString(resultDatas));
            } else if(action.equals(BleService.ACTION_GATT_HAS_TRANSED)) {
//                int num = intent.getIntExtra("EXTRA_HAS_TRANSED_PACKETNUMBERS", 0);
//                LogUtils.e(num);
            } else if(action.equals(BleService.ACTION_GATT_SERVICES_DISCOVERED)) {
                sBleService.enableSmartBikeService();
            }
        }
    };


    private ServiceConnection bleServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BleService.LocalBinder binder = (BleService.LocalBinder) service;
            sBleService = binder.getService();
            if (!sBleService.initialize()) {
                showToast("蓝牙服务初始化失败");
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sBleService = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(bleServiceConnection);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(BLEStatusChangeReceiver);
    }

    @OnClick({R.id.tabview_fingerprint, R.id.tabview_voice, R.id.iv_ble_connect})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tabview_fingerprint:
                startActivity(FingerprintActivity.class);
                break;

            case R.id.tabview_voice:
                startActivity(AudioManageActivity.class);
                break;

            case R.id.iv_ble_connect:
                BleConnectDialog bleConnectDialog = new BleConnectDialog(context);
                bleConnectDialog.show();
                break;

            default:
                break;
        }
    }

    @OnClick({R.id.button1, R.id.button2, R.id.button3,
    R.id.button4})
    public void onTestButtonClick(View view) {

        switch (view.getId()) {
            case R.id.button1:
                if(sBleService.mConnectionState != BleService.STATE_CONNECTED) {
                    showToast("请先连接蓝牙设备");
                    return;
                }
                sBleService.selfWriteCommand(getTestDatas());
                break;

            case R.id.button2:
                if(sBleService.mConnectionState != BleService.STATE_CONNECTED) {
                    showToast("请先连接蓝牙设备");
                    return;
                }
                sBleService.setAlarm(2);
                break;

            case R.id.button3:
                if(sBleService.mConnectionState != BleService.STATE_CONNECTED) {
                    showToast("请先连接蓝牙设备");
                    return;
                }
                byte[] bytes = new byte[16];
                for(int i = 0; i < bytes.length; i++) {
                    bytes[i] = (byte) i;
                }
                sBleService.selfWriteData(bytes);
                break;

//            case R.id.button4:
//                startActivity(TestActiviy.class);
//                break;

            default:break;
        }
    }

    public byte[] getTestDatas() {
        byte[] bytes = new byte[18];
        bytes[0] = 0x10;
        bytes[1] = (byte) (bytes.length & 0xFF);
        for(int i = 2; i < bytes.length; i++) {
            bytes[i] = (byte) (i & 0xFF);
        }

        LogUtils.e(StringUtil.byteArray2HexString(bytes));

        return bytes;
    }


    private long exitTime = 0;

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            showToast("再按一次退出程序");
            exitTime = System.currentTimeMillis();
        } else {
            // 2s内连续点击back两次可退出程序
            super.onBackPressed();
        }
    }
}
