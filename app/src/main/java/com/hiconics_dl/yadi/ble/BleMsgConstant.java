package com.hiconics_dl.yadi.ble;


/**
 * Created by chenjun on 2018/6/12.
 */

public class BleMsgConstant {
    //    fingerprint
    public static final byte FINGERPRINT_ADD_CMD = (byte) 0x10;
    public static final byte FINGERPRINT_ADD_ACK = (byte) 0x90;

    public static final byte FINGERPRINT_QUERY_CMD = (byte) 0x11;
    public static final byte FINGERPRINT_QUERY_ACK = (byte) 0x91;

    public static final byte FINGERPRINT_DELETE_CMD = (byte) 0x12;
    public static final byte FINGERPRINT_DELETE_ACK = (byte) 0x92;

    public static final byte AUDIO_UPDATE_CONTROL_CMD = (byte) 0x15;
    public static final byte AUDIO_UPDATE_CONTROL_ACK = (byte) 0x95;

    public static final byte AUDIO_UPDATE_STOP_CMD = (byte) 0x16;
    public static final byte AUDIO_UPDATE_STOP_ACK = (byte) 0x96;

    public static final byte AUDIO_PLAY_CMD = (byte) 0x17;
    public static final byte AUDIO_PLAY_ACK = (byte) 0x97;

    public static final byte AUDIO_STOP_CMD = (byte) 0x18;
    public static final byte AUDIO_STOP_ACK = (byte) 0x98;


}
