package com.mamh.clevermap.util;

import android.util.Log;

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
            case 20011:
                errorText = "出现错误，这可能是在虚拟机上定位引发的，定位到了旧金山硅谷附近";
                break;
            case 1002:
                errorText = "开发者的Key不正确或过期";
                break;
            case 1008:
                errorText = "MD5安全码未通过验证,请检查SHA1、包名是否正确";
                break;
            case 1009:
                errorText = "请求Key与绑定平台不符";
                break;
            case 1013:
                errorText = "Key被删除,请反馈给开发者";
                break;
            case 1903:
                errorText = "空指针异常,为SDK内部错误";
                break;
            case 1200:
                errorText = "请求参数非法(目前暂时不支持搜索公交线路）";
                break;
            case 1804:
                errorText = "网络未连接，请检查网路是否畅通";
                break;
            case 3000:
                errorText = "规划点（包括起点、终点、途经点）不在中国陆地范围内";
                break;
            case 3003:
                errorText = "要去的地方太远啦emm，换种交通方式试试吧";
                break;
            default:
                errorText = "出现未知错误";
        }
        Log.e(TAG, "handleErrorCode: " + errorText + "\n错误代码：" + errorCode);
        return errorText + "\n错误代码：" + errorCode;
    }

    /**
     * 对返回的错误码进行处理,处理旧版错误码（为1-20左右的整数）和定位错误码
     *
     * @param errorCode 传入的错误码
     * @param errInfo   解析得到的错误详细信息
     */
    public static String handleLocateError(int errorCode, String errInfo) {
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
        return simpleErrString;
    }

    public static void handleException(String exceptionDescription) {
        Log.e(TAG, "handleException: " + exceptionDescription);
    }
}
