package com.hiconics_dl.yadi.ui.adapter;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hiconics_dl.yadi.R;
import com.hiconics_dl.yadi.bean.FingerprintItem;


public class FingerprintAdapter extends BaseQuickAdapter<FingerprintItem, BaseViewHolder>{
    public FingerprintAdapter() {
        super(R.layout.item_fingerprint);
    }

    @Override
    protected void convert(BaseViewHolder holder, FingerprintItem item) {
        holder.setText(R.id.tv_fingerprint_name, item.getName());
        holder.addOnClickListener(R.id.iv_fingerprint_delete);
    }
}
