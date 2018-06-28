package com.hiconics_dl.yadi.ui.adapter;

import android.support.annotation.LayoutRes;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hiconics_dl.yadi.R;
import com.hiconics_dl.yadi.bean.AudioItem;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Line;


public class AudioAdapter extends BaseQuickAdapter<AudioItem, BaseViewHolder>{

    public AudioAdapter(List<AudioItem> datas) {
        super(R.layout.item_voice, datas);
    }

    @Override
    protected void convert(BaseViewHolder holder, AudioItem item) {
        holder.setText(R.id.tv_voice_name, item.getName());
        holder.addOnClickListener(R.id.iv_voice_edit);
        holder.addOnClickListener(R.id.iv_voice_play);
        holder.addOnClickListener(R.id.iv_voice_recover);
    }


}
