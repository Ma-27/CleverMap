package com.mamh.clevermap.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mamh.clevermap.R;
import com.mamh.clevermap.listener.route.RouteSearchHelper;
import com.mamh.clevermap.util.AmapUtilityTools;

import org.jetbrains.annotations.NotNull;

import static com.mamh.clevermap.activity.MainActivity.MAP_ZOOM;

public class RouteActivity extends AppCompatActivity {
    private static final String TAG = "RouteActivity成功";
    public static MapView routeMapView;
    public static AMap routeAMap;
    private LatLonPoint startPoint, endPoint;
    private RouteSearch.FromAndTo fromAndTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //activity初始化设置
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        //获取传来的位置信息,默认起始点为百花公园->山大附中
        Intent intent = getIntent();
        double currentLocationLatitude = intent.getDoubleExtra
                ("current_location_latitude", 36.6761);
        double currentLocationLongitude = intent.getDoubleExtra
                ("current_location_longitude", 117.070446);

        double poiLatitude = intent.getDoubleExtra("poi_latitude", 36.686515);
        double poiLongitude = intent.getDoubleExtra("poi_longitude", 117.064894);
        startPoint = AmapUtilityTools.convertToLatLonPoint
                (AmapUtilityTools.convertToLatLng
                        (currentLocationLatitude, currentLocationLongitude));

        endPoint = AmapUtilityTools.convertToLatLonPoint
                (AmapUtilityTools.convertToLatLng(poiLatitude, poiLongitude));

        //设置地图初始化
        routeMapView = findViewById(R.id.route_map);
        routeMapView.onCreate(savedInstanceState);
        setUpMap();
        configureMapSettings();

        //给marker添加点击响应，移动camera
        routeAMap.setOnMarkerClickListener(marker -> {
            routeAMap.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(marker.getPosition(), MAP_ZOOM));
            return false;
        });
    }

    /**
     * 方法必须重写
     * NOTIFY:在这里搜索路线了
     */
    @Override
    protected void onResume() {
        super.onResume();
        routeMapView.onResume();
        searchRoute();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        routeMapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        routeMapView.onDestroy();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        routeMapView.onSaveInstanceState(outState);
    }

    private void setUpMap() {
        routeAMap = routeMapView.getMap();
        routeAMap.setMapType(AMap.MAP_TYPE_NORMAL);
        routeAMap.setTrafficEnabled(true);
    }

    private void configureMapSettings() {
        //实例化UiSettings类对象
        UiSettings myUiSettings = routeAMap.getUiSettings();
        // 设置默认定位按钮是否显示
        routeAMap.getUiSettings().setMyLocationButtonEnabled(false);
        // 可触发定位并显示当前位置
        routeAMap.setMyLocationEnabled(false);
        //显示罗盘
        myUiSettings.setCompassEnabled(true);
        //控制比例尺控件是否显示
        myUiSettings.setScaleControlsEnabled(true);
        //不显示比例控制按钮
        myUiSettings.setZoomControlsEnabled(false);
    }

    private void searchRoute() {
        RouteSearchHelper helper = new RouteSearchHelper(getApplicationContext());
        fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
        WalkRouteQuery walkRouteQuery = new RouteSearch.WalkRouteQuery(fromAndTo);
        helper.calculateWalkRouteAsyn(walkRouteQuery);
    }
}