package com.hiconics_dl.yadi.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.bleyadeasmartbikelib.BleService;
import com.hiconics_dl.yadi.R;
import com.hiconics_dl.yadi.bean.FingerprintItem;
import com.hiconics_dl.yadi.ble.BleMsgConstant;
import com.hiconics_dl.yadi.ble.BleMsgControl;
import com.hiconics_dl.yadi.ui.adapter.FingerprintAdapter;
import com.hiconics_dl.yadi.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 指纹管理
 */

public class FingerprintActivity extends BaseBleActivity implements View.OnClickListener {

    @BindView(R.id.recyclerview)
    RecyclerView recyclerview;

    private FingerprintAdapter fingerprintAdapter;
    private boolean[] fingerprintStatusArray = new boolean[32];

    @Override
    protected int getLayoutId() {
        return R.layout.layout_recyclerview;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        showBackIcon(true);
        setTitleText("指纹管理");

        fingerprintAdapter = new FingerprintAdapter();
        View headerView = LayoutInflater.from(context).inflate(R.layout.layout_common_top, null);
        fingerprintAdapter.addHeaderView(headerView);

        View footerView = LayoutInflater.from(context).inflate(R.layout.layout_fingerprint_bottom, null);
        footerView.findViewById(R.id.tv_fingerprint_add).setOnClickListener(this);
        footerView.findViewById(R.id.tv_fingerprint_all_delete).setOnClickListener(this);
        fingerprintAdapter.addFooterView(footerView);

        fingerprintAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                FingerprintItem item = fingerprintAdapter.getItem(position);
                BleMsgControl.fingerprintDelete(item.getNum());
                showLoadingDialog("删除指纹中");
            }
        });

        recyclerview.setLayoutManager(new LinearLayoutManager(context));
        recyclerview.setAdapter(fingerprintAdapter);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_SELF_COMMAND_AVAILABLE);
        intentFilter.addAction(BleService.ACTION_GATT_HAS_TRANSED);
        LocalBroadcastManager.getInstance(context).registerReceiver(bleReceiver, intentFilter);


        if(BleMsgControl.fingerprintQuery()) {
            showLoadingDialog();
        } else {
            showToast("请先连接蓝牙设备");
        }
    }

    @Override
    protected void onConnectedBle() {
        showLoadingDialog();
        BleMsgControl.fingerprintQuery();
    }

    private final BroadcastReceiver bleReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BleService.ACTION_GATT_SELF_COMMAND_AVAILABLE)) {
                byte[] resultDatas = intent.getByteArrayExtra(BleService.EXTRA_DATA_SELF_COMMAND);
                LogUtils.e(StringUtil.byteArray2HexString(resultDatas));

                if (resultDatas[0] == BleMsgConstant.FINGERPRINT_QUERY_ACK
                        && resultDatas.length == 6) {
                    int fingerprintData  = 0;
                    fingerprintData |= (resultDatas[2] & 0xFF) << 24;
                    fingerprintData |= (resultDatas[3] & 0xFF) << 16;
                    fingerprintData |= (resultDatas[4] & 0xFF) << 8;
                    fingerprintData |= (resultDatas[5] & 0xFF);

                    List<FingerprintItem> fingerprintList = new ArrayList<>();
                    for(int i = 0; i < 32; i++) {
                        boolean valid = ((fingerprintData >> i) & 0x1) == 1;
                        fingerprintStatusArray[i] = valid;
                        if(valid) {
                            FingerprintItem item = new FingerprintItem(i + 1);
                            fingerprintList.add(item);
                        }
                    }
                    hideLoadingDialog();
                    fingerprintAdapter.setNewData(fingerprintList);
                } else if(resultDatas[0] == BleMsgConstant.FINGERPRINT_ADD_ACK
                        && resultDatas.length == 3) {
                    if(resultDatas[2] == 0) {
                        BleMsgControl.fingerprintQuery();
                    } else {
                        showToast("添加指纹失败");
                    }
                } else if(resultDatas[0] == BleMsgConstant.FINGERPRINT_DELETE_ACK
                        && resultDatas.length == 3) {
                    if(resultDatas[2] == 0) {
                        BleMsgControl.fingerprintQuery();
                    } else {
                        showToast("删除指纹失败");
                    }
                }
            } else if (action.equals(BleService.ACTION_GATT_HAS_TRANSED)) {
                int num = intent.getIntExtra("EXTRA_HAS_TRANSED_PACKETNUMBERS", 0);
//                LogUtils.e(num);
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_fingerprint_add:

                for(int i = 0; i < fingerprintStatusArray.length; i++) {
                    if(!fingerprintStatusArray[i]) {
                        if(BleMsgControl.fingerprintAdd(i + 1)) {
                            showLoadingDialog("添加指纹中");
                        } else {
                            showToast("请先连接蓝牙设备");
                            return;
                        }
                        return;
                    }
                }
                ToastUtils.showShort("最多添加32组指纹");

                break;

            case R.id.tv_fingerprint_all_delete:
                if(!BleMsgControl.fingerprintDelete(0)) {
                    showToast("请先连接蓝牙设备");
                }

//                fingerprintAdapter.getData().clear();
//                fingerprintAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }
}
