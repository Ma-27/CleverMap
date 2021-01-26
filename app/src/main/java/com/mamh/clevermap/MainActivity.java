package com.mamh.clevermap;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;

public class MainActivity extends FragmentActivity {
    //地图的缩放范围，值越高范围越小。默认设为17
    public static float MAP_ZOOM = 17.5f;
    MapView mapView;
    AMap aMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.map);
        // 此方法须覆写，虚拟机需要在很多情况下保存地图绘制的当前状态。
        mapView.onCreate(savedInstanceState);
        //初始化控制单元
        if (aMap == null) {
            aMap = mapView.getMap();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时
        // 执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }
}