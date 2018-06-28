package com.hiconics_dl.yadi.ui.adapter;

import android.support.annotation.Nullable;

import com.blankj.utilcode.util.FileUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hiconics_dl.yadi.R;
import com.hiconics_dl.yadi.utils.CommonUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;


public class FileManageAdapter extends BaseQuickAdapter<File, BaseViewHolder> {


    public FileManageAdapter() {
        super(R.layout.item_file_manage);
    }

    @Override
    protected void convert(BaseViewHolder holder, File file) {
        holder.setText(R.id.tv_item_file_name, file.getName());

        if (file.isDirectory()) {
            holder.setImageResource(R.id.iv_item_file_icon, R.mipmap.type_dir);
            holder.setVisible(R.id.iv_item_file_more, true);
            holder.setImageResource(R.id.iv_item_file_more, R.mipmap.next_666);
            holder.addOnClickListener(R.id.iv_item_file_more);

            File[] files = file.listFiles();
            int fileCount = 0;
            int dirCount = 0;
            if(files != null) {
                for (File f : files) {
                    if(f.isHidden()) {
                        continue;
                    }
                    if (f.isDirectory()) {
                        dirCount++;
                    } else if(CommonUtil.isAudioFile(f)){
                        fileCount++;
                    }
                }
            }
            holder.setText(R.id.tv_item_file_info,
                    "音频文件：" + fileCount + "，文件夹：" + dirCount);
        } else {
            if (CommonUtil.isAudioFile(file)) {
                holder.setImageResource(R.id.iv_item_file_icon, R.mipmap.type_music);
                holder.setVisible(R.id.iv_item_file_more, true);
                holder.setImageResource(R.id.iv_item_file_more, R.mipmap.more);
                holder.addOnClickListener(R.id.iv_item_file_more);
            } else {
                holder.setImageResource(R.id.iv_item_file_icon, R.mipmap.type_unknow);
                holder.setVisible(R.id.iv_item_file_more, false);
            }
            long modifiedTime = file.lastModified();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd HH:mm:ss");
            String result = formatter.format(modifiedTime);

            String fileSize = FileUtils.getFileSize(file);
            holder.setText(R.id.tv_item_file_info, result + "  " + fileSize);
        }
    }



}
