package com.mamh.clevermap.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
import com.google.android.material.tabs.TabLayout;
import com.mamh.clevermap.R;
import com.mamh.clevermap.listener.route.RouteSearchHelper;
import com.mamh.clevermap.overlay.MyRouteOverlay;
import com.mamh.clevermap.util.AmapUtilityTools;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.amap.api.services.route.RouteSearch.DRIVING_SINGLE_DEFAULT;

public class RouteActivity extends AppCompatActivity implements
        TabLayout.OnTabSelectedListener {
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
        Toolbar toolbar = findViewById(R.id.toolbar_route_choose);
        setSupportActionBar(toolbar);
        //添加默认的返回图标并设置返回键可用
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        TabLayout tabLayout = findViewById(R.id.tab_layout_route);
        tabLayout.addOnTabSelectedListener(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
        //FIXME:暂时不用，隐藏
        fab.setVisibility(View.GONE);

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
        routeMapView = findViewById(R.id.map_route);
        routeMapView.onCreate(savedInstanceState);
        setUpMap();
        configureMapSettings();

        //给marker添加点击响应，移动camera
        routeAMap.setOnMarkerClickListener(marker -> {
            routeAMap.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(marker.getPosition(), MyRouteOverlay.getBoundsZoom()));
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
        //清理地图上所有覆盖物
        routeAMap.clear();
        //默认为步行搜索，onResume就执行一次默认搜索
        searchWalkRoute();
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

    /**
     * 判断tabLayout点了哪个，执行响相应的搜索
     *
     * @param position 点击位置，默认为0
     */
    private void searchRoute(int position) {
        //清理地图上所有覆盖物
        routeAMap.clear();
        switch (position) {
            case 0:
                searchWalkRoute();
                break;
            case 1:
                searchRideRoute();
                break;
            case 2:
                searchBusRoute();
                break;
            case 3:
                searchDriveRoute();
                break;
            default:
                Log.w(TAG, "searchRoute: 传入参数有误，传入了" + position);
        }
    }

    private void searchWalkRoute() {
        RouteSearchHelper helper = new RouteSearchHelper(getApplicationContext());
        fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
        WalkRouteQuery walkRouteQuery = new RouteSearch.WalkRouteQuery(fromAndTo);
        helper.calculateWalkRouteAsyn(walkRouteQuery);
    }

    private void searchRideRoute() {
        RouteSearchHelper helper = new RouteSearchHelper(getApplicationContext());
        fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
        RouteSearch.RideRouteQuery rideRouteQuery = new RouteSearch.RideRouteQuery(fromAndTo);
        helper.calculateRideRouteAsyn(rideRouteQuery);
    }

    private void searchBusRoute() {
        RouteSearchHelper helper = new RouteSearchHelper(getApplicationContext());
        fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
        // fromAndTo包含路径规划的起点和终点，RouteSearch.BusLeaseWalk表示公交查询模式
        // 第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算,1表示计算
        //FIXME:起始位置设为济南了
        RouteSearch.BusRouteQuery busRouteQuery =
                new RouteSearch.BusRouteQuery(fromAndTo, RouteSearch.BUS_DEFAULT, "济南", 0);
        helper.calculateBusRouteAsyn(busRouteQuery);
    }

    private void searchDriveRoute() {
        RouteSearchHelper helper = new RouteSearchHelper(getApplicationContext());
        fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
        //FIXME:这里固定为默认路线了,可以考虑增加交互更新这里
        // 选择速度优先，不考虑当时路况，返回耗时最短的路线
        RouteSearch.DriveRouteQuery driveRouteQuery = new RouteSearch.DriveRouteQuery
                (fromAndTo, DRIVING_SINGLE_DEFAULT, null, null, "");
        helper.calculateDriveRouteAsyn(driveRouteQuery);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        searchRoute(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        searchRoute(tab.getPosition());
    }
}