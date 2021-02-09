package com.mamh.clevermap.util;

import android.util.Log;

import com.google.android.material.snackbar.Snackbar;

import static com.mamh.clevermap.activity.MainActivity.mapView;

public class ErrorHandler {
    private static final String TAG = "ErrorHandler成功";


    /**
     * 对返回的错误码进行处理,处理新版错误码（为1000左右）
     *
     * @param errorCode 传入的错误码
     * @return 简单的错误描述
     */
    public static String handleErrorCode(int errorCode) {
        String errorText;
        switch (errorCode) {
            case 1200:
                errorText = "请求参数非法,错误代码：";
                break;
            case 1804:
                errorText = "网络未连接，请检查网路是否畅通\n错误代码：";
                break;
            default:
                errorText = "出现未知错误\n错误代码：";
        }
        Log.e(TAG, "handleErrorCode: " + errorText + errorCode);
        Snackbar.make(mapView, errorText + errorCode,
                Snackbar.LENGTH_SHORT).show();
        return errorText + errorCode;
    }

    /**
     * 对返回的错误码进行处理,处理旧版错误码（为1-20左右的整数）和定位错误码
     *
     * @param errorCode 传入的错误码
     * @param errInfo   解析得到的错误详细信息
     */
    public static void handleLocateError(int errorCode, String errInfo) {
        String simpleErrString = "定位失败,错误码为：";
        switch (errorCode) {
            case 1:
                simpleErrString = simpleErrString + errorCode + "，一些重要参数为空";
                break;
            case 2:
                simpleErrString = simpleErrString + errorCode + "，由于仅扫描到单个wifi，且没有基站信息";
                break;
            case 3:
                simpleErrString = simpleErrString + errorCode + "，获取到的请求参数为空，可能获取过程中出现异常";
                break;
            case 4:
                simpleErrString = simpleErrString + errorCode + "，请求服务器过程中出现异常，请检查网络情况";
                break;
            case 7:
                simpleErrString = simpleErrString + errorCode + "，Key错误，请联系开发者或重试";
                break;
            case 12:
                simpleErrString = simpleErrString + errorCode + "，缺少定位权限或未开启获取位置信息功能，请到设置中开启";
                break;
            default:
                simpleErrString = simpleErrString + errorCode;
                break;
        }
        Log.e(TAG, simpleErrString + "\n" + errInfo);
        Snackbar.make(mapView, simpleErrString, Snackbar.LENGTH_SHORT).show();
    }
}
