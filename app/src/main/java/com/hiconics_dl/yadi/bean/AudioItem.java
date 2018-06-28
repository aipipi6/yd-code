package com.hiconics_dl.yadi.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjun on 2018/6/13.
 */

public class AudioItem implements Parcelable {
    int num;
    String name;

    public AudioItem() {
    }

    public AudioItem(int num, String name) {
        this.num = num;
        this.name = name;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.num);
        dest.writeString(this.name);
    }

    protected AudioItem(Parcel in) {
        this.num = in.readInt();
        this.name = in.readString();
    }

    public static final Parcelable.Creator<AudioItem> CREATOR = new Parcelable.Creator<AudioItem>() {
        @Override
        public AudioItem createFromParcel(Parcel source) {
            return new AudioItem(source);
        }

        @Override
        public AudioItem[] newArray(int size) {
            return new AudioItem[size];
        }
    };


    public static List<AudioItem> sAudioItems = new ArrayList<>();

    static {
        sAudioItems.add(new AudioItem(1,  "上电提示声音"));
        sAudioItems.add(new AudioItem(2,  "开机声音"));
        sAudioItems.add(new AudioItem(3,  "开坐垫声音"));
        sAudioItems.add(new AudioItem(4,  "寻车声音"));
        sAudioItems.add(new AudioItem(5,  "待机声音"));
        sAudioItems.add(new AudioItem(6,  "报警声音"));
        sAudioItems.add(new AudioItem(7,  "提示音1"));
        sAudioItems.add(new AudioItem(8,  "提示音2"));
        sAudioItems.add(new AudioItem(9,  "放脚掌声音"));
        sAudioItems.add(new AudioItem(10, "收脚掌声音"));
        sAudioItems.add(new AudioItem(11, "解防声音"));
        sAudioItems.add(new AudioItem(12, "设防声音"));
        sAudioItems.add(new AudioItem(13, "转向声音"));
        sAudioItems.add(new AudioItem(14, "预报警声音"));
    }
}
