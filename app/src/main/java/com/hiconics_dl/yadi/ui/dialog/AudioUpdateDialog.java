package com.hiconics_dl.yadi.ui.dialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.bleyadeasmartbikelib.BleService;
import com.freelink.library.dialog.BaseNormalDialog;
import com.freelink.library.dialog.CustomAlertDialog;
import com.freelink.library.widget.CircularProgressView;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hiconics_dl.yadi.R;
import com.hiconics_dl.yadi.ble.BleMsgConstant;
import com.hiconics_dl.yadi.ble.BleMsgControl;
import com.hiconics_dl.yadi.utils.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenjun on 2018/6/12.
 */

public class AudioUpdateDialog extends BaseNormalDialog {

    private final static int FRAME_LEN = 16;
    private final static int FRAME_COUNT = 16;

    public AudioUpdateDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_audio_update;
    }

    CircularProgressView circularProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(false);
        circularProgressView = findViewById(R.id.CircularProgressView);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_SELF_COMMAND_AVAILABLE);
        intentFilter.addAction(BleService.ACTION_GATT_HAS_TRANSED);
        LocalBroadcastManager.getInstance(context).registerReceiver(bleReceiver, intentFilter);

    }

    @Override
    public void dismiss() {
        super.dismiss();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(bleReceiver);
        if (!isUpdateCompleted) {
            BleMsgControl.audioUpdateStop(audioNum);
        }

        if(stopDialog != null && stopDialog.isShowing()) {
            stopDialog.dismiss();
            stopDialog = null;
        }
    }

    private static final int TIMEOUT = 1000;
    private File audioFile;
    private int audioNum;
    private int audioType;
    private int fileLength;
    private int fileOffset;
    private byte[] fileDatas;
    private ExecutorService threadPool;
    private boolean isUpdateCompleted = false;
    private CustomAlertDialog stopDialog;

    public void init(int audioNum, String audioPath) {
        audioFile = new File(audioPath);
        if (audioFile.exists()) {
            init(audioNum, audioFile);
        }
    }

    public void init(int audioNum, File audioFile) {
        this.audioNum = audioNum;
        this.audioFile = audioFile;
        this.audioType = audioFile.getName().toLowerCase().endsWith(".wav") ? 0 : 1;
        this.fileLength = (int) (audioFile.length() & 0xFFFFFFFF);
        this.fileOffset = 0;
        this.fileDatas = new byte[fileLength];
        this.isUpdateCompleted = false;
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("AudioUpdate-pool-%d").build();
        threadPool = new ThreadPoolExecutor(2, 2,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    public void startUpdate() {
        threadPool.execute(readFileTask);
    }

    private void sendFileData() {
        threadPool.execute(sendDataTask);
    }

    private void sendControlMsg() {
        BleMsgControl.audioUpdateControl(audioNum, audioType, fileLength, fileOffset);
        handler.postDelayed(timeoutTask, TIMEOUT);
    }

    Runnable readFileTask = new Runnable() {
        @Override
        public void run() {
            try {
                FileInputStream fis = new FileInputStream(audioFile);
                int lenCount = 0;
                while (lenCount < fileDatas.length) {
                    int readLen = fis.read(fileDatas, lenCount, fileDatas.length - lenCount);
                    lenCount += readLen;
                }
                fis.close();

                fileOffset = 0;
                sendControlMsg();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    Runnable sendDataTask = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < FRAME_COUNT; i++) {
                int sendLen = fileLength - fileOffset;
                if (sendLen <= 0) {
                    break;
                }
                sendLen = sendLen > FRAME_LEN ? FRAME_LEN : sendLen;
                byte[] sendDatas = new byte[sendLen];
                System.arraycopy(fileDatas, fileOffset, sendDatas, 0, sendLen);

                BleMsgControl.audioUpdateSendData(sendDatas);
                try {
                    Thread.sleep(4);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                fileOffset += sendLen;
            }
            sendControlMsg();
        }
    };

    private int timeoutCount = 0;
    Runnable timeoutTask = new Runnable() {
        @Override
        public void run() {
            timeoutCount++;
            if (timeoutCount > 3) {
                dismiss();
                ToastUtils.showShort("升级超时");
            } else {
                sendControlMsg();
            }
        }
    };

    private final BroadcastReceiver bleReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BleService.ACTION_GATT_SELF_COMMAND_AVAILABLE)) {
                byte[] resultDatas = intent.getByteArrayExtra(BleService.EXTRA_DATA_SELF_COMMAND);
                LogUtils.e(StringUtil.byteArray2HexString(resultDatas));

                if (resultDatas[0] == BleMsgConstant.AUDIO_UPDATE_CONTROL_ACK
                        || resultDatas.length == 12) {
                    int offset = 0;
                    offset |= (resultDatas[8] & 0xFF) << 24;
                    offset |= (resultDatas[9] & 0xFF) << 16;
                    offset |= (resultDatas[10] & 0xFF) << 8;
                    offset |= (resultDatas[11] & 0xFF);

                    handler.removeCallbacks(timeoutTask);
                    timeoutCount = 0;

                    if (offset < fileLength) {
                        isUpdateCompleted = false;
                        if (offset == fileOffset) {
                            float progress = (float) fileOffset / (float) fileLength;
                            LogUtils.e(progress);
                            circularProgressView.setProgress(progress);
                        } else if (offset < fileOffset) {
                            fileOffset = offset;
                        }
                        sendFileData();
                    } else if (offset == fileLength) {
                        LogUtils.e("发送完成");
                        ToastUtils.showShort("发送完成");
                        isUpdateCompleted = true;
                        circularProgressView.setProgress(1f);
                        dismiss();
                    }
                }

            } else if (action.equals(BleService.ACTION_GATT_HAS_TRANSED)) {
                int num = intent.getIntExtra("EXTRA_HAS_TRANSED_PACKETNUMBERS", 0);
//                LogUtils.e(num);
            }
        }
    };

    @Override
    public void onBackPressed() {
        stopDialog = new CustomAlertDialog(context);
        stopDialog.content("确定终止音频文件更新?")
                .left("继续")
                .right("终止")
                .setOnClickListener(new CustomAlertDialog.OnClickListener() {
                    @Override
                    public void onLeftClick(CustomAlertDialog dialog) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onRightClick(CustomAlertDialog dialog) {
                        dialog.dismiss();
                        dismiss();
                    }
                }).show();
        stopDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                stopDialog = null;
            }
        });
    }
}
