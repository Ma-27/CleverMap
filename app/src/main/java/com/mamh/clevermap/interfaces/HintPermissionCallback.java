package com.mamh.clevermap.interfaces;

public interface HintPermissionCallback {
    //用户授权点击确定时
    void doPositiveClick(int requestCode);

    //用户授权点击取消时
    void doNegativeClick(int requestCode);
}
