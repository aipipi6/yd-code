package com.hiconics_dl.yadi.utils;


import com.blankj.utilcode.util.StringUtils;

/**
 * Created by chenjun on 2017/12/5.
 */

public class StringUtil extends StringUtils {


    private StringUtil() {
        super();
    }

    public static String double2String(double d) {
        return d == (int)d ? String.valueOf((int)d) : String.valueOf(d);
    }

    public static String float2String(float d) {
        return d == (int)d ? String.valueOf((int)d) : String.valueOf(d);
    }

    private static final String[] hexTextArray =
            {"0", "1", "2", "3", "4", "5", "6", "7", "8","9", "A", "B", "C", "D", "E", "F"};
    public static String byteArray2HexString(byte[] bytes) {
        return byteArray2HexString(0, bytes.length, bytes);
    }
    public static String byteArray2HexString(int start, int len, byte[] bytes) {
        if(start + len <= bytes.length) {
            StringBuffer stringBuffer = new StringBuffer();
            for(byte b : bytes) {
                int b1 = (b >> 4) & 0xF;
                int b2 = b & 0xF;
                stringBuffer.append(hexTextArray[b1]);
                stringBuffer.append(hexTextArray[b2]);
                stringBuffer.append(' ');
            }
            return stringBuffer.toString();
        } else {
            throw new IllegalArgumentException("out of bound of array");
        }
    }

    public static byte[] stringHex2ByteArray(String s) {
        return stringHex2ByteArray(s, " ");
    }
    public static byte[] stringHex2ByteArray(String s, String split) {
        String[] ss = s.split(split);
        byte[] bytes = new byte[ss.length];

        for(int i = 0; i < ss.length; i++) {
            bytes[i] = (byte) Integer.parseInt(ss[i], 16);
        }

        return bytes;
    }

}
