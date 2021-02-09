package com.mamh.clevermap.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.google.android.material.snackbar.Snackbar;
import com.mamh.clevermap.R;
import com.mamh.clevermap.fragment.ChooseMapTypeDialogFragment;
import com.mamh.clevermap.fragment.HintDialogFragment;
import com.mamh.clevermap.interfaces.HintPermissionCallback;
import com.mamh.clevermap.listener.main.GrantPermissionHelper;
import com.mamh.clevermap.listener.main.PoiSearchBottomSheetHelper;
import com.mamh.clevermap.listener.main.PoiViewBottomSheetHelper;
import com.mamh.clevermap.listener.main.SensorEventHelper;
import com.mamh.clevermap.util.ErrorHandler;

import org.jetbrains.annotations.NotNull;

import static com.mamh.clevermap.listener.main.GrantPermissionHelper.IsEmptyOrNullString;
import static com.mamh.clevermap.listener.main.GrantPermissionHelper.LOCATION_PERMISSION_CODE;
import static com.mamh.clevermap.listener.main.GrantPermissionHelper.PHONE_STATE_PERMISSION_CODE;
import static com.mamh.clevermap.listener.main.PoiSearchHelper.getPoiLatLng;

public class MainActivity extends FragmentActivity implements LocationSource,
        AMapLocationListener, HintPermissionCallback {
    //地图的缩放范围，值越高范围越小。默认设为17.5
    public static float MAP_ZOOM = 17.5f;
    public static MapView mapView;
    public static AMap aMap;
    public static final String LOCATION_MARKER_FLAG = "您当前的位置";
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private static final String TAG = "MainActivity成功";
    //权限控制类对象
    GrantPermissionHelper permissionHelper;
    public static LatLng location;
    //BottomSheet控件
    public static BottomSheetBehavior<LinearLayout> viewPoiSheetBehaviour, searchPoiSheetBehaviour;
    //定位范围圆形
    private Circle mCircle;
    //控制蓝点旋转的传感器
    private SensorEventHelper mSensorHelper;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private boolean mFirstLocate = true;
    //定位标记,和另一个大头针Marker，记录一些位置
    public static Marker locMarker = null, marker = null;
    //承载BottomSheet的linear layout布局
    private LinearLayout poiSheetLayout, searchSheetLayout;
    //自定义的针对POI的BottomSheet，处理BottomSheet类的滑动操作
    private PoiViewBottomSheetHelper viewPoiSheetHelper = null;
    private PoiSearchBottomSheetHelper searchPoiSheetHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.map);
        //授予所需权限
        permissionHelper = new GrantPermissionHelper();
        checkStatePermission();
        checkLocationPermission();

        // 此方法须覆写，虚拟机需要在很多情况下保存地图绘制的当前状态。
        mapView.onCreate(savedInstanceState);
        setUpMap();
        configureMapSettings();

        //选择map类型的处理和监听
        FloatingActionButton switchMapType = findViewById(R.id.switchMapType);
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
        poiSheetLayout = findViewById(R.id.poi_sheet_linear_layout);
        viewPoiSheetBehaviour = BottomSheetBehavior.from(poiSheetLayout);
        viewPoiSheetHelper = new PoiViewBottomSheetHelper
                (getApplicationContext(), poiSheetLayout);
        viewPoiSheetBehaviour.addBottomSheetCallback(viewPoiSheetHelper);
        viewPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);

        //处理搜索poi事件
        searchSheetLayout = findViewById(R.id.search_sheet_linear_layout);
        searchPoiSheetBehaviour = BottomSheetBehavior.from(searchSheetLayout);
        searchPoiSheetHelper = new PoiSearchBottomSheetHelper
                (getApplicationContext(), searchSheetLayout, poiSheetLayout);
        searchPoiSheetBehaviour.addBottomSheetCallback(searchPoiSheetHelper);
        //设为默认露出搜索框
        searchPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();

        //反馈发现aMap在onResume时可能为空，这里如果为空则重新初始化
        if (aMap == null) {
            aMap = mapView.getMap();

            //初始化map,设置默认显示的地图类型
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);
            aMap.setTrafficEnabled(true);
        }

        if (mSensorHelper == null) {
            //注册传感器
            mSensorHelper = new SensorEventHelper(getApplicationContext());
        }
        mSensorHelper.registerSensorListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
        deactivate();
        mFirstLocate = true;
        if (marker != null) {
            marker.destroy();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSensorHelper != null) {
            mSensorHelper.unRegisterSensorListener();
            mSensorHelper.setCurrentMarker(null);
            mSensorHelper = null;
        }
        if (locMarker != null) {
            locMarker.destroy();
        }
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
        if (null != mLocationClient) {
            mLocationClient.onDestroy();
        }

        //移除POI响应点击
        if (viewPoiSheetBehaviour != null && poiSheetLayout != null) {
            viewPoiSheetBehaviour.onDetachedFromLayoutParams();

            poiSheetLayout.setVisibility(View.GONE);
            poiSheetLayout = null;
            viewPoiSheetBehaviour = null;
        }

        //移除SearchBottomSheet
        if (searchPoiSheetBehaviour != null && searchSheetLayout != null) {
            searchPoiSheetBehaviour.setHideable(true);
            searchSheetLayout.setVisibility(View.GONE);
            poiSheetLayout = null;
            searchPoiSheetBehaviour = null;
        }
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
            //检查是否有残留Marker，如果有则清除掉
            if (marker != null) {
                marker.destroy();
            }
            //如果有键盘，则隐藏
            InputMethodManager inputManager = (InputMethodManager)
                    getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(searchSheetLayout.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }

            LatLng position = poi.getCoordinate();
            marker = aMap.addMarker(new MarkerOptions().position(position));
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, MAP_ZOOM));
            try {
                viewPoiSheetHelper.setPoi(poi);
                viewPoiSheetHelper.updatePOIText();
                //设置搜索和查看poi的BottomSheet状态
                viewPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                //设置BottomSheet不可隐藏（避免误会和吐槽）
                viewPoiSheetBehaviour.setHideable(false);

                searchPoiSheetBehaviour.setHideable(true);
                searchPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
            } catch (NullPointerException nullPointerException) {
                Log.e(TAG, "setUpMap: 处理POI onClick中遭遇空指针异常");
                nullPointerException.printStackTrace();
            } catch (IllegalArgumentException argumentException) {
                Log.e(TAG, "setUpMap: 处理POI onClick中遭遇参数异常");
                argumentException.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "setUpMap: 处理POI onClick中遭遇异常");
                e.printStackTrace();
            }
        });
        //单纯的点地图，不是点poi;点poi触发上面的响应
        aMap.addOnMapClickListener(poi -> {
            if (viewPoiSheetBehaviour != null && poiSheetLayout != null) {
                viewPoiSheetBehaviour.setHideable(true);
                //不点了就藏起来吧,把搜索框显示出来
                searchPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                viewPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
            //清除当前已设立的标记
            marker.destroy();
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
            if (aMapLocation.getErrorCode() == 0) {
                location = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                if (mFirstLocate) {
                    mFirstLocate = false;
                    addLocCircle(location, aMapLocation.getAccuracy());//添加定位精度圆
                    addLocationMarker(location);//添加定位图标
                    mSensorHelper.setCurrentMarker(locMarker);//定位图标旋转
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, MAP_ZOOM));
                } else {
                    mCircle.setCenter(location);
                    mCircle.setRadius(aMapLocation.getAccuracy());
                    locMarker.setPosition(location);
                }
            } else {
                Log.e(TAG, "onLocationChanged: 定位遭遇服务器报错，查看详情：");
                ErrorHandler.handleLocateError(aMapLocation.getErrorCode(), aMapLocation.getErrorInfo());
            }
        }
    }

    /**
     * 点击地图上的定位小图标才开启这个
     *
     * @param onLocationChangedListener 当location发生改变时重设listener为这个
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {
            //初始化定位
            mLocationClient = new AMapLocationClient(this);
            //初始化定位参数
            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
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
            //当激活完成后再去检查权限，避免定位在先，引发崩溃
            checkLocationPermission();
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    /**
     * 开始定位
     */
    private void startLocation() {
        // 启动定位
        try {
            mLocationClient.startLocation();
        } catch (NullPointerException nullPointerException) {
            Log.e(TAG, "startLocation: 启动定位时出现空指针异常");
            nullPointerException.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "startLocation: 启动定位时出现异常");
        }
    }

    private void addLocCircle(LatLng latlng, double radius) {
        CircleOptions options = new CircleOptions();
        options.strokeWidth(1f);
        options.fillColor(FILL_COLOR);
        options.strokeColor(STROKE_COLOR);
        options.center(latlng);
        options.radius(radius);
        mCircle = aMap.addCircle(options);
    }

    private void addLocationMarker(LatLng latlng) {
        if (locMarker == null) {
            MarkerOptions options = new MarkerOptions();
            options.icon(BitmapDescriptorFactory
                    .fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                            R.mipmap.navi_map_gps_locked)));
            options.anchor(0.5f, 0.5f);
            options.position(latlng);
            locMarker = aMap.addMarker(options);
            locMarker.setTitle(LOCATION_MARKER_FLAG);
        }
    }

    @SuppressLint({"ResourceType", "NonConstantResourceId"})
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void setMapType(View view) {
        int id = view.getSourceLayoutResId();
        try {
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
        } catch (NullPointerException nullPointerException) {
            Log.e(TAG, "setDefaultMapType: 未成功，aMap对象为空,布局ID为：" + id);
            nullPointerException.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "setDefaultMapType: 未成功，遇到未知异常" + id);
            e.printStackTrace();
        }
    }

    public void checkStatePermission() {
        // 检查是否有定位权限
        // 检查权限的方法: ContextCompat.checkSelfPermission()两个参数分别是Context和权限名.
        // 返回PERMISSION_GRANTED是有权限，PERMISSION_DENIED没有权限
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            //没有权限，向系统申请该权限。
            Log.i(TAG, "没有权限");
            requestPermission(PHONE_STATE_PERMISSION_CODE);
        } else {
            Log.d(TAG, "已授予读取手机状态权限");
        }
    }

    public void checkLocationPermission() {
        // 检查是否有定位权限
        // 检查权限的方法: ContextCompat.checkSelfPermission()两个参数分别是Context和权限名.
        // 返回PERMISSION_GRANTED是有权限，PERMISSION_DENIED没有权限
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //没有权限，向系统申请该权限。
            Log.i(TAG, "精确定位没有权限");
            requestPermission(LOCATION_PERMISSION_CODE);
        } else {
            Log.d(TAG, "已授予手机定位权限");
            //已经获得权限，则执行定位请求。
            startLocation();
        }
    }

    public void requestPermission(int permissionCode) {
        //通过编码获取权限的名字
        String permission = permissionHelper.getPermissionString(permissionCode);
        //没有权限就去要权限，如果权限被拒绝
        if (!IsEmptyOrNullString(permission)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission)) {
                /* Show an expanation to the user *asynchronously* -- don't block
                this thread waiting for the user's response! After the user
                sees the explanation, try again to request the permission.*/
                if (permissionCode == LOCATION_PERMISSION_CODE) {
                    DialogFragment newFragment = HintDialogFragment
                            .newInstance(R.string.location_description_title,
                                    R.string.location_description_permission_reason,
                                    permissionCode);
                    newFragment.show(this.getFragmentManager(),
                            HintDialogFragment.class.getSimpleName());

                } else if (permissionCode == PHONE_STATE_PERMISSION_CODE) {
                    DialogFragment newFragment =
                            HintDialogFragment.newInstance(R.string.state_description_title,
                                    R.string.state_description_permission_reason,
                                    permissionCode);

                    newFragment.show(this.getFragmentManager(), HintDialogFragment.class.getSimpleName());
                }
            } else {
                //勾选不解释，直接要权限，一般直接执行这个
                ActivityCompat.requestPermissions(this,
                        new String[]{permission}, permissionCode);
            }
        }
    }

    /**
     * 弹出对话框询问是否需要权限，这里是当授予权限时的处理函数
     *
     * @param requestCode 询问的权限
     */
    @Override
    public void doPositiveClick(int requestCode) {
        String permission = permissionHelper.getPermissionString(requestCode);
        if (!IsEmptyOrNullString(permission)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        requestCode);
            } else {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivity(intent);
                startLocation();
            }
        }
    }

    /**
     * 当拒绝权限时的做法
     *
     * @param requestCode 传递的状态码
     */
    @Override
    public void doNegativeClick(int requestCode) {
        Snackbar.make(mapView, "请授予权限，否则地图无法正常定位", Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Button的OnClick响应，只能在activity中使用显式intent
     *
     * @param view 传入button的view对象
     */
    public void launchRouteActivity(View view) {
        Intent intent = new Intent(this, RouteActivity.class);
        try {
            intent.putExtra("current_location_latitude", location.latitude);
            intent.putExtra("current_location_longitude", location.longitude);
            intent.putExtra("poi_latitude", getPoiLatLng().latitude);
            intent.putExtra("poi_longitude", getPoiLatLng().longitude);
        } catch (NullPointerException nullPointerException) {
            Log.e(TAG, "launchRouteActivity: 获得poi经纬度时出现空指针");
            nullPointerException.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "launchRouteActivity: 出现未知异常");
            e.printStackTrace();
        } finally {
            //打开路线activity
            startActivity(intent);
        }
    }
}