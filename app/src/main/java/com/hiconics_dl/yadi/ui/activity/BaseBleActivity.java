package com.hiconics_dl.yadi.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.bleyadeasmartbikelib.BleService;
import com.hiconics_dl.yadi.R;
import com.hiconics_dl.yadi.base.BaseToolBarActivity;
import com.hiconics_dl.yadi.ui.dialog.BleConnectDialog;

/**
 * Created by chenjun on 2018/6/11.
 */

public abstract class BaseBleActivity extends BaseToolBarActivity{

    private ImageView ivBleConnect;
    protected BleService bleService = MainActivity.sBleService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ivBleConnect = new ImageView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp2px(18), dp2px(18));
        ivBleConnect.setLayoutParams(layoutParams);
        addRightView(ivBleConnect);
        if(bleService.mConnectionState == BleService.STATE_CONNECTED) {
            ivBleConnect.setImageResource(R.mipmap.bluetooth_blue);
        } else {
            ivBleConnect.setImageResource(R.mipmap.bluetooth_gray);
        }
        ivBleConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BleConnectDialog bleConnectDialog = new BleConnectDialog(context);
                bleConnectDialog.show();
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);

        LocalBroadcastManager.getInstance(context).registerReceiver(BLEStatusChangeReceiver, intentFilter);
    }

    private final BroadcastReceiver BLEStatusChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BleService.ACTION_GATT_CONNECTED)) {
                showToast("已连接上蓝牙设备");
                bleService.mConnectionState = BleService.STATE_CONNECTED;
                ivBleConnect.setImageResource(R.mipmap.bluetooth_blue);
                onConnectedBle();
            } else if(action.equals(BleService.ACTION_GATT_DISCONNECTED)) {
                showToast("已断开蓝牙设备");
                bleService.mConnectionState = BleService.STATE_DISCONNECTED;
                ivBleConnect.setImageResource(R.mipmap.bluetooth_gray);
                onDisconnectBle();
            }
        }
    };

    protected void onConnectedBle() {
    }

    protected void onDisconnectBle() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(BLEStatusChangeReceiver);
    }
}
