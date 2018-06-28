package com.hiconics_dl.yadi.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.bleyadeasmartbikelib.BleService;
import com.hiconics_dl.yadi.R;
import com.hiconics_dl.yadi.bean.AudioItem;
import com.hiconics_dl.yadi.ble.BleMsgControl;
import com.hiconics_dl.yadi.ui.adapter.AudioAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 声音设置
 */

public class AudioManageActivity extends BaseBleActivity {
    @BindView(R.id.recyclerview)
    RecyclerView recyclerview;

    AudioAdapter voiceAdapter;


    @Override
    protected int getLayoutId() {
        return R.layout.layout_recyclerview;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        showBackIcon(true);
        setTitleText("声音设置");

        voiceAdapter = new AudioAdapter(AudioItem.sAudioItems);
        View headerView = LayoutInflater.from(context).inflate(R.layout.layout_common_top, null);
        voiceAdapter.addHeaderView(headerView);

        recyclerview.setLayoutManager(new LinearLayoutManager(context));
        recyclerview.setAdapter(voiceAdapter);

        voiceAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if(bleService.mConnectionState != BleService.STATE_CONNECTED) {
                    showToast("请先连接蓝牙设备");
                    return;
                }

                AudioItem audioItem = voiceAdapter.getItem(position);
                int viewId = view.getId();
                if(viewId == R.id.iv_voice_edit) {
                    FileManageActivity.startActivity(context, audioItem);
                } else if(viewId == R.id.iv_voice_play) {
                    BleMsgControl.audioPlay(audioItem.getNum());
                } else if(viewId == R.id.iv_voice_recover) {

                }
            }
        });
    }

}
