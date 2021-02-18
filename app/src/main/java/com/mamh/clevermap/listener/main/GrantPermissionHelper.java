package com.mamh.clevermap.listener.main;

import android.Manifest;

public class GrantPermissionHelper {
    public static final int PHONE_STATE_PERMISSION_CODE = 10;
    public static final int LOCATION_PERMISSION_CODE = 11;
    private static final String TAG = "GrantPermissionHelper成功";

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean IsEmptyOrNullString(String s) {
        return (s == null) || (s.trim().length() == 0);
    }

    public String getPermissionString(int requestCode) {
        String permission = "";
        switch (requestCode) {
            case LOCATION_PERMISSION_CODE:
                permission = Manifest.permission.ACCESS_FINE_LOCATION;
                break;
            case PHONE_STATE_PERMISSION_CODE:
                permission = Manifest.permission.READ_PHONE_STATE;
                break;
        }
        return permission;
    }
}
