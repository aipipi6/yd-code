package com.hiconics_dl.yadi.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SDCardUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.freelink.library.dialog.BottomChoiceDialog;
import com.freelink.library.dialog.CustomAlertDialog;
import com.hiconics_dl.yadi.R;
import com.hiconics_dl.yadi.base.BaseToolBarActivity;
import com.hiconics_dl.yadi.bean.AudioItem;
import com.hiconics_dl.yadi.ui.adapter.FileManageAdapter;
import com.hiconics_dl.yadi.ui.dialog.AudioUpdateDialog;
import com.hiconics_dl.yadi.utils.CommonUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenjun on 2018/6/7.
 * 文件管理器
 */

public class FileManageActivity extends BaseToolBarActivity {

    private static final String TAG = FileManageActivity.class.getSimpleName();
    private static final String ARG_AUDIO_ITEM = "AUDIO_ITEM";

    @BindView(R.id.recyclerview)
    RecyclerView recyclerview;

    private AudioItem audioItem;
    private FileManageAdapter fileManageAdapter;
    private File currentDirectory;
    private View headerView;
    private List<File> sdCardFiles = new ArrayList<>();
    private MediaPlayer mediaPlayer = new MediaPlayer();


    public static void startActivity(Context context, AudioItem audioItem) {
        Intent intent = new Intent(context, FileManageActivity.class);
        intent.putExtra(ARG_AUDIO_ITEM, audioItem);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_file_manage;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        showBackIcon(true);
        audioItem = getIntent().getParcelableExtra(ARG_AUDIO_ITEM);
        setTitleText("修改" + audioItem.getName());

        headerView = findViewById(R.id.rl_last_dir);
        headerView.setOnClickListener(onLastDirectoryClickListener);

        fileManageAdapter = new FileManageAdapter();
        recyclerview.setLayoutManager(new LinearLayoutManager(context));
        recyclerview.setAdapter(fileManageAdapter);

        List<String> sdCardPaths = SDCardUtils.getSDCardPaths();
        for (String path : sdCardPaths) {
            Log.e(TAG, path);
            File file = new File(path);
            if (file.exists()) {
                sdCardFiles.add(file);
            }
        }
        headerView.setVisibility(View.GONE);
        fileManageAdapter.setNewData(sdCardFiles);

        fileManageAdapter.setOnItemClickListener(onItemClickListener);
        fileManageAdapter.setOnItemChildClickListener(onItemChildClickListener);
    }

    private boolean changeLastDirectory() {
        if (currentDirectory != null) {
            boolean isSdCardFile = false;
            for (File f : sdCardFiles) {
                if (f.getAbsolutePath().equals(currentDirectory.getAbsolutePath())) {
                    isSdCardFile = true;
                    break;
                }
            }
            if (!isSdCardFile) {
                changeDirectory(currentDirectory.getParent());
                return true;
            } else {
                headerView.setVisibility(View.GONE);
                fileManageAdapter.setNewData(sdCardFiles);
                currentDirectory = null;
                return true;
            }
        }
        return false;
    }

    View.OnClickListener onLastDirectoryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            changeLastDirectory();
        }
    };

    BaseQuickAdapter.OnItemChildClickListener onItemChildClickListener = new BaseQuickAdapter.OnItemChildClickListener() {

        @Override
        public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
            onItemClickListener.onItemClick(adapter, view, position);
        }
    };
    BaseQuickAdapter.OnItemClickListener onItemClickListener = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            File file = fileManageAdapter.getItem(position);
            final String filePath = file.getAbsolutePath();
            if (file.isDirectory()) {
                changeDirectory(filePath);
            } else {

                BottomChoiceDialog dialog = new BottomChoiceDialog(context);
                dialog.setItems("试听", "选择");
                dialog.setOnChoiceListener(new BottomChoiceDialog.OnChoiceListener() {
                    @Override
                    public void onChoice(int index, String item) {
                        if (index == 0) {
                            playAudio(filePath);
                        } else {
                            choiceAudio(filePath);
                        }
                    }
                });
                dialog.show();
            }
        }
    };

    private void changeDirectory(String path) {

        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return !pathname.isHidden() &&
                            (pathname.isDirectory() || CommonUtil.isAudioFile(pathname));
                }
            });
            if (files != null && files.length > 0) {
                List<File> fileList = Arrays.asList(files);
                Collections.sort(fileList, new FileComparator());
                headerView.setVisibility(View.VISIBLE);
                fileManageAdapter.setNewData(fileList);
                currentDirectory = file;
            } else {
                showToast("该文件夹为空");
            }
        }
    }

    private void playAudio(String audioPath) {

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        super.onDestroy();
    }

    private void choiceAudio(final String audioPath) {
        CustomAlertDialog alertDialog = new CustomAlertDialog(context);
        alertDialog.content("确定修改" + audioItem.getName() + "?")
                .left("取消")
                .right("确定")
                .setOnClickListener(new CustomAlertDialog.OnClickListener() {
                    @Override
                    public void onLeftClick(CustomAlertDialog dialog) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onRightClick(CustomAlertDialog dialog) {
                        dialog.dismiss();
                        startUpdate(audioPath);
                    }
                }).show();
    }

    AudioUpdateDialog updateDialog;
    private void startUpdate(String audioPath) {
        updateDialog = new AudioUpdateDialog(context);
        updateDialog.show();
        updateDialog.init(audioItem.getNum(), audioPath);
        updateDialog.startUpdate();
        updateDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateDialog = null;
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!changeLastDirectory()) {
                if (getApplicationInfo().targetSdkVersion
                        >= Build.VERSION_CODES.ECLAIR) {
                    event.startTracking();
                } else {
                    onBackPressed();
                }
            }
            return true;
        }

        return true;
    }

    class FileComparator implements Comparator<File> {

        @Override
        public int compare(File file1, File file2) {
            if (file1.isDirectory() && file2.isDirectory()) {
                return file1.getName().compareTo(file2.getName());
            } else if (file1.isDirectory() && !file2.isDirectory()) {
                return 1;
            } else if (!file1.isDirectory() && file2.isDirectory()) {
                return -1;
            }
            return 0;
        }
    }


}
