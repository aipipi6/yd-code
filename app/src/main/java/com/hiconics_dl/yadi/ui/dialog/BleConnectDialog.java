package com.hiconics_dl.yadi.ui.dialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.example.bleyadeasmartbikelib.BleService;
import com.hiconics_dl.yadi.R;
import com.freelink.library.dialog.BaseNormalDialog;
import com.hiconics_dl.yadi.ui.activity.MainActivity;

public class BleConnectDialog extends BaseNormalDialog{

    private static final String TAG = BleConnectDialog.class.getSimpleName();
    private static final String BLE_DEV_NAME_PARAMS = "ble_dev_name";
    private EditText etName;
    private Button btnConnect;
    private BleService bleService = MainActivity.sBleService;

    private static final String[] BTN_TEXTs = {
            "未连接，点击连接", "正在连接中...", "已连接，点击断开"
    };

    public BleConnectDialog(@NonNull Context context) {
        super(context);
        setCanceledOnTouchOutside(false);
        etName = findViewById(R.id.et_ble_dialog_name);
        btnConnect = findViewById(R.id.btn_ble_dialog_connect);
        findViewById(R.id.iv_ble_dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        if(bleService.mConnectionState == BleService.STATE_DISCONNECTED){
            String devName = SPUtils.getInstance().getString(BLE_DEV_NAME_PARAMS);
            if(StringUtils.isEmpty(devName)) {
                devName = "YD04160000";
            }
            etName.setText(devName);
            btnConnect.setText(BTN_TEXTs[0]);
        } else if(bleService.mConnectionState == BleService.STATE_CONNECTING) {
            etName.setText(bleService.DeviceName);
            btnConnect.setText(BTN_TEXTs[1]);
        }  else if(bleService.mConnectionState == BleService.STATE_CONNECTED) {
            etName.setText(bleService.DeviceName);
            btnConnect.setText(BTN_TEXTs[2]);
        }
        etName.setSelection(etName.getText().length());

        btnConnect.setOnClickListener(onConnectListener);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(BLEStatusChangeReceiver, intentFilter);

    }

    @Override
    public void dismiss() {
        super.dismiss();
        bleService.cancelConnect();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(BLEStatusChangeReceiver);
    }

    private final BroadcastReceiver BLEStatusChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BleService.ACTION_GATT_CONNECTED)) {
                LogUtils.e("ACTION_GATT_CONNECTED\n" +
                        "Name:" + bleService.DeviceName + "\n" +
                        "Addr:" + bleService.DeviceMacAddress);
                btnConnect.setText(BTN_TEXTs[2]);
                SPUtils.getInstance().put(BLE_DEV_NAME_PARAMS, bleService.DeviceName);
                bleService.cancelConnect();
                dismiss();
            } else if(action.equals(BleService.ACTION_GATT_DISCONNECTED)) {
                LogUtils.e("ACTION_GATT_DISCONNECTED");
                btnConnect.setText(BTN_TEXTs[0]);
                bleService.cancelConnect();
                bleService.refreshDeviceCache();
                bleService.close();
                bleService.mDevice = null;
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_ble_connect;
    }

    View.OnClickListener onConnectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (bleService.mConnectionState) {
                case BleService.STATE_DISCONNECTED:
                    String devName = etName.getText().toString();
                    if(StringUtils.isEmpty(devName)) {
                        etName.setError("不能为空");
                        return;
                    }
                    btnConnect.setText(BTN_TEXTs[1]);
                    bleService.connectDevice(devName);
                    bleService.mConnectionState = BleService.STATE_CONNECTING;
                    Log.i(TAG, "start connectDevice");
                    break;

                case BleService.STATE_CONNECTING:
//                    Log.i(TAG, "start cancelConnect");
//                    sBleService.cancelConnect();
//                    btnConnect.setText(BTN_TEXTs[0]);
                    break;

                case BleService.STATE_CONNECTED:
                    Log.i(TAG, "start disconnect");
                    bleService.disconnect();
                    break;

                default:break;
            }
        }
    };
}
