package com.hiconics_dl.yadi.utils;

import java.io.File;


public class CommonUtil {

    /**
     * 判断文件是否是音频文件
     * @param file
     * @return
     */
    public static boolean isAudioFile(File file) {
        String path = file.getAbsolutePath().toLowerCase();
        return  path.endsWith(".wav") || path.endsWith(".mp3")/* || path.endsWith(".aac")*/;
    }
}
