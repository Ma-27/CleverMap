package com.mamh.clevermap.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mamh.clevermap.R;
import com.mamh.clevermap.fragment.ChooseMapTypeDialogFragment;
import com.mamh.clevermap.listener.BottomSheetEventHandler;
import com.mamh.clevermap.listener.SensorEventHelper;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends FragmentActivity implements LocationSource,
        AMapLocationListener {
    //地图的缩放范围，值越高范围越小。默认设为17.5
    public static float MAP_ZOOM = 17.5f;
    MapView mapView;
    AMap aMap;
    public static final String LOCATION_MARKER_FLAG = "您当前的位置";
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private static final String TAG = "MainActivity成功";
    LatLng location;
    //定位标记
    private Marker mLocMarker;
    //定位范围圆形
    private Circle mCircle;
    //控制蓝点旋转的传感器
    private SensorEventHelper mSensorHelper;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private boolean mFirstLocate = true;

    //随便一个大头针Marker，记录一些位置
    private Marker marker = null;

    private FloatingActionButton switchMapType;

    //承载ButtomSheet的linear layout布局
    private LinearLayout buttomSheetLayout;
    //ButtomSheet控件
    private BottomSheetBehavior viewPoiSheetBehaviour;
    //自定义的针对POI的BottomSheet，处理BottomSheet类的滑动操作
    private BottomSheetEventHandler bottomSheetEventHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.map);
        // 此方法须覆写，虚拟机需要在很多情况下保存地图绘制的当前状态。
        mapView.onCreate(savedInstanceState);
        setUpMap();
        configureMapSettings();
        //授予所需权限
        //checkAndGrantPermission();
        //选择map类型的处理
        switchMapType = findViewById(R.id.switchMapType);
        switchMapType.setOnClickListener(v -> {
            ChooseMapTypeDialogFragment fragment = ChooseMapTypeDialogFragment.newInstance();
            // 获得fragmentManager并开始交接
            FragmentManager fragmentManager = getSupportFragmentManager();
            //开始交接
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction.add(fragment, "CHOOSE_MAP")
                    .addToBackStack(null)
                    .commit();
        });
        //处理点击poi的事件
        buttomSheetLayout = findViewById(R.id.poi_sheet_linear_layout);
        viewPoiSheetBehaviour = BottomSheetBehavior.from(buttomSheetLayout);
        bottomSheetEventHandler = new BottomSheetEventHandler(
                getApplicationContext(), buttomSheetLayout);
        viewPoiSheetBehaviour.setBottomSheetCallback(bottomSheetEventHandler);
        viewPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();

        //注册传感器
        mSensorHelper = new SensorEventHelper(getApplicationContext());
        if (mSensorHelper != null) {
            mSensorHelper.registerSensorListener();
        } else {
            Toast.makeText(MainActivity.this, "注册传感器出错", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onResume: 注册传感器出错");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
        deactivate();
        mFirstLocate = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSensorHelper != null) {
            mSensorHelper.unRegisterSensorListener();
            mSensorHelper.setCurrentMarker(null);
            mSensorHelper = null;
        }
        if (mLocMarker != null) {
            mLocMarker.destroy();
        }
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
        if (null != mLocationClient) {
            mLocationClient.onDestroy();
        }

        //移除POI响应点击
        aMap.removeOnPOIClickListener(poi -> {
            if (viewPoiSheetBehaviour != null && buttomSheetLayout != null) {
                viewPoiSheetBehaviour.setHideable(true);
                buttomSheetLayout.setVisibility(View.INVISIBLE);
                buttomSheetLayout = null;
                viewPoiSheetBehaviour = null;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时
        // 执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 用于初始化aMap对象，安排对地图的各种控制操作。
     */
    private void setUpMap() {
        //初始化控制单元
        if (aMap == null) {
            aMap = mapView.getMap();

            //初始化map,设置默认显示的地图类型
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);
            aMap.setTrafficEnabled(true);
        }
        //地图上POI的点击响应
        aMap.addOnPOIClickListener(poi -> {
            LatLng position = poi.getCoordinate();
            marker = aMap.addMarker(new MarkerOptions().position(position));
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, MAP_ZOOM));
            if (bottomSheetEventHandler != null) {
                bottomSheetEventHandler.setPoi(poi);
                bottomSheetEventHandler.updatePOIText();
            }
            if (viewPoiSheetBehaviour != null && buttomSheetLayout != null) {
                //设置BottomSheet状态
                viewPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                //设置BottomSheet不可隐藏（避免误会和吐槽）
                viewPoiSheetBehaviour.setHideable(false);
                //检查
            }
        });
        //单纯的点地图，不是点poi;点poi触发上面的响应
        aMap.addOnMapClickListener(poi -> {
            if (viewPoiSheetBehaviour != null && buttomSheetLayout != null) {
                viewPoiSheetBehaviour.setHideable(true);
                //不点了就藏起来吧
                viewPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
            //清除当前已设立的标记
            marker = null;
        });
    }

    private void configureMapSettings() {
        //实例化UiSettings类对象
        UiSettings myUiSettings = aMap.getUiSettings();

        // 设置定位监听
        aMap.setLocationSource(this);
        // 设置默认定位按钮是否显示
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        // 可触发定位并显示当前位置
        aMap.setMyLocationEnabled(true);
        //显示罗盘
        myUiSettings.setCompassEnabled(true);
        //控制比例尺控件是否显示
        myUiSettings.setScaleControlsEnabled(true);
        //不显示比例控制按钮
        myUiSettings.setZoomControlsEnabled(false);
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null
                    && aMapLocation.getErrorCode() == 0) {

                LatLng location = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                if (mFirstLocate) {
                    mFirstLocate = false;
                    addCircle(location, aMapLocation.getAccuracy());//添加定位精度圆
                    addMarker(location);//添加定位图标
                    mSensorHelper.setCurrentMarker(mLocMarker);//定位图标旋转
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, MAP_ZOOM));
                } else {
                    //mSensorHelper.setCurrentMarker(mLocMarker);//定位图标旋转
                    mCircle.setCenter(location);
                    mCircle.setRadius(aMapLocation.getAccuracy());
                    mLocMarker.setPosition(location);
                }
                //为ButtomSheetDialog设置当前位置
                if (bottomSheetEventHandler != null) {
                    bottomSheetEventHandler.setCurrentLocation(location);
                }
            } else {
                String errText = "定位失败,错误码为：" + aMapLocation.getErrorCode();
                Log.e(TAG, errText + aMapLocation.getErrorInfo());
                //Snackbar.make(mapView,errText,Snackbar.LENGTH_SHORT);
                Toast.makeText(MainActivity.this, errText, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {
            //初始化定位
            mLocationClient = new AMapLocationClient(this);
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            //目前使用多次定位，设置定位间隔为10s
            MyLocationStyle myLocationStyle = new MyLocationStyle();
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
            // mLocationOption.setOnceLocation(true);
            //设置定位参数
            myLocationStyle.interval(10000);
            aMap.setMyLocationStyle(myLocationStyle);
            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            //mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    private void addCircle(LatLng latlng, double radius) {
        CircleOptions options = new CircleOptions();
        options.strokeWidth(1f);
        options.fillColor(FILL_COLOR);
        options.strokeColor(STROKE_COLOR);
        options.center(latlng);
        options.radius(radius);
        mCircle = aMap.addCircle(options);
    }

    private void addMarker(LatLng latlng) {
        if (mLocMarker != null) {
            return;
        }
        MarkerOptions options = new MarkerOptions();
        options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.navi_map_gps_locked)));
        options.anchor(0.5f, 0.5f);
        options.position(latlng);
        mLocMarker = aMap.addMarker(options);
        mLocMarker.setTitle(LOCATION_MARKER_FLAG);
    }

    private void checkAndGrantPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, 0);
    }

    @SuppressLint({"ResourceType", "NonConstantResourceId"})
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void setMapType(View view) {
        int id = view.getSourceLayoutResId();
        Log.d(TAG, "setMapType: " + id);
        if (aMap != null) {
            switch (id) {
                //当选中地图为导航时
                case R.layout.choose_map_item_navigation:
                    aMap.setMapType(AMap.MAP_TYPE_NAVI);
                    break;
                //当选中地图为夜间模式时
                case R.layout.choose_map_item_night:
                    aMap.setMapType(AMap.MAP_TYPE_NIGHT);
                    break;
                //当选中地图为卫星地图模式时
                case R.layout.choose_map_item_satellite:
                    aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                    break;
                //当选中地图为默认时
                case R.layout.choose_map_item_default:
                    //默认切换为原图
                default:
                    aMap.setMapType(AMap.MAP_TYPE_NORMAL);
                    //普通地图道路交通状况默认可见
                    aMap.setTrafficEnabled(true);
                    break;
            }
        } else {
            Log.e(TAG, "setDefaultMapType: ,未成功，aMap对象为空,布局ID为：" + id);
        }
    }
}