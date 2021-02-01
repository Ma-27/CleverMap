package com.mamh.clevermap.listener;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.amap.api.maps.model.Marker;

public class SensorEventHelper implements SensorEventListener {
    //SensorManager可以控制传感器，但必须在使用后关闭传感器
    private final SensorManager mSensorManager;
    //一个传感器，这里传感屏幕纵横
    private final Sensor mSensor;
    private final Context mContext;
    //检测屏幕旋转角度，这个是原有角度
    private long lastTime = 0;
    private float mAngle;
    private Marker mMarker;

    public SensorEventHelper(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }

    /**
     * 获取当前屏幕旋转角度并换算为以下表示法
     *
     * @param context activity传入上下文参数
     * @return 0表示是竖屏; 90表示是左横屏; 180表示是反向竖屏; 270表示是右横屏
     */
    public static int getScreenRotationOnPhone(Context context) {
        //显示类对象，内含屏幕旋转的角度
        final Display display = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                return 0;

            case Surface.ROTATION_90:
                return 90;

            case Surface.ROTATION_180:
                return 180;

            case Surface.ROTATION_270:
                return -90;
        }
        return 0;
    }

    public void registerSensorListener() {
        mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unRegisterSensorListener() {
        mSensorManager.unregisterListener(this, mSensor);
    }

    public void setCurrentMarker(Marker marker) {
        mMarker = marker;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 啥也不干
    }

    //监听器检测到回调方法
    @Override
    public void onSensorChanged(SensorEvent event) {
        //现有角度的delta量
        int TIME_SENSOR = 100;
        if (System.currentTimeMillis() - lastTime < TIME_SENSOR) {
            return;
        }
        //旋转定位图标
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ORIENTATION: {
                float x = event.values[0];
                x += getScreenRotationOnPhone(mContext);
                x %= 360.0F;
                if (x > 180.0F)
                    x -= 360.0F;
                else if (x < -180.0F)
                    x += 360.0F;

                //求浮点数的绝对值
                if (Math.abs(mAngle - x) < 3.0f) {
                    break;
                }
                mAngle = Float.isNaN(x) ? 0 : x;
                if (mMarker != null) {
                    //为蓝点设置旋转角度
                    mMarker.setRotateAngle(360 - mAngle);
                }
                lastTime = System.currentTimeMillis();
            }
        }

    }
}
