package com.hiconics_dl.yadi.ble;

import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.example.bleyadeasmartbikelib.BleService;
import com.hiconics_dl.yadi.ui.activity.MainActivity;
import com.hiconics_dl.yadi.utils.StringUtil;

/**
 * Created by chenjun on 2018/6/12.
 */

public class BleMsgControl {

    private static final String TAG = BleMsgControl.class.getSimpleName();
    public static BleService bleService = MainActivity.sBleService;

    public static boolean isConnnectBle() {
        return bleService != null
                && bleService.mConnectionState == BleService.STATE_CONNECTED;
    }

    public static boolean fingerprintAdd(int num) {
        if(!isConnnectBle()) {
            return false;
        }
        byte[] datas = new byte[3];
        datas[0] = BleMsgConstant.FINGERPRINT_ADD_CMD;
        datas[1] = (byte) datas.length;
        datas[2] = (byte) num;

        bleService.selfWriteCommand(datas);

        return true;
    }

    public static boolean fingerprintQuery() {
        if(!isConnnectBle()) {
            return false;
        }
        byte[] datas = new byte[2];
        datas[0] = BleMsgConstant.FINGERPRINT_QUERY_CMD;
        datas[1] = (byte) datas.length;

        bleService.selfWriteCommand(datas);

        return true;
    }

    public static boolean fingerprintDelete(int num) {
        if(!isConnnectBle()) {
            return false;
        }
        byte[] datas = new byte[3];
        datas[0] = BleMsgConstant.FINGERPRINT_DELETE_CMD;
        datas[1] = (byte) datas.length;
        datas[2] = (byte) num;

        bleService.selfWriteCommand(datas);

        return true;
    }

    public static void audioPlay(int audioNum) {
        if(!isConnnectBle()) {
            return;
        }
        byte[] datas = new byte[3];
        datas[0] = BleMsgConstant.AUDIO_PLAY_CMD;
        datas[1] = (byte) datas.length;
        datas[2] = (byte) audioNum;

        bleService.selfWriteCommand(datas);
    }

    public static void audioStop(int audioNum) {
        if(!isConnnectBle()) {
            return;
        }
        byte[] datas = new byte[3];
        datas[0] = BleMsgConstant.AUDIO_STOP_CMD;
        datas[1] = (byte) datas.length;
        datas[2] = (byte) audioNum;

        bleService.selfWriteCommand(datas);
    }

    public static void audioUpdateControl(int audioNum, int audioType, int fileSize, int offset) {
        if(!isConnnectBle()) {
            return;
        }

        byte[] datas = new byte[12];
        datas[0] = BleMsgConstant.AUDIO_UPDATE_CONTROL_CMD;
        datas[1] = (byte) datas.length;
        datas[2] = (byte) audioNum;
        datas[3] = (byte) audioType;

        datas[4] = (byte) ((fileSize >> 24) & 0xFF);
        datas[5] = (byte) ((fileSize >> 16) & 0xFF);
        datas[6] = (byte) ((fileSize >>  8) & 0xFF);
        datas[7] = (byte) ((fileSize      ) & 0xFF);

        datas[8]  = (byte) ((offset >> 24) & 0xFF);
        datas[9]  = (byte) ((offset >> 16) & 0xFF);
        datas[10] = (byte) ((offset >>  8) & 0xFF);
        datas[11] = (byte) ((offset      ) & 0xFF);

        LogUtils.e(StringUtil.byteArray2HexString(datas));
        bleService.selfWriteCommand(datas);
    }

    public static void audioUpdateSendData(byte[] datas) {
        if(!isConnnectBle()) {
            return;
        }
        bleService.selfWriteData(datas);
    }

    public static void audioUpdateStop(int audioNum) {
        if(!isConnnectBle()) {
            return;
        }
        byte[] datas = new byte[3];
        datas[0] = BleMsgConstant.AUDIO_UPDATE_STOP_CMD;
        datas[1] = (byte) datas.length;
        datas[2] = (byte) audioNum;

        bleService.selfWriteCommand(datas);
    }


}
