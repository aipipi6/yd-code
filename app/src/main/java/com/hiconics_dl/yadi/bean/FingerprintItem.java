package com.hiconics_dl.yadi.bean;

/**
 * Created by chenjun on 2018/6/20.
 */

public class FingerprintItem {
    private String name;
    private int num;

    public FingerprintItem(int num) {
        this(num, "指纹" + num);
    }

    public FingerprintItem(int num, String name) {
        this.name = name;
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
