package com.mamh.clevermap.listener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.mamh.clevermap.R;
import com.mamh.clevermap.activity.MainActivity;
import com.mamh.clevermap.adapter.PoiSearchLayoutAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.mamh.clevermap.activity.MainActivity.MAP_ZOOM;
import static com.mamh.clevermap.activity.MainActivity.aMap;
import static com.mamh.clevermap.activity.MainActivity.mapView;
import static com.mamh.clevermap.activity.MainActivity.marker;
import static com.mamh.clevermap.activity.MainActivity.searchPoiSheetBehaviour;
import static com.mamh.clevermap.activity.MainActivity.viewPoiSheetBehaviour;

public class PoiSearchHelper extends PoiSearch implements PoiSearch.OnPoiSearchListener {
    private static final String TAG = "PoiSearchHelper成功";
    private final Context context;
    private final View rootView;
    private TextView titleView = null, item1View = null, item2View = null,
            telView = null, distanceView = null;
    private RecyclerView recyclerView;

    /**
     * 响应来自poi的搜索请求
     *
     * @param context        传入上下文，继承自父类
     * @param query          传入的搜索请求，继承自父类
     * @param layoutRootView 根视图，用于加载搜索结果信息
     */
    @SuppressLint("NonConstantResourceId")
    public PoiSearchHelper(Context context, Query query, View layoutRootView) {
        super(context, query);
        setOnPoiSearchListener(this);
        this.context = context;
        this.rootView = layoutRootView;
        titleView = rootView.findViewById(R.id.textView_poi_title);
        item1View = rootView.findViewById(R.id.poi_item1);
        item2View = rootView.findViewById(R.id.poi_item2);
        telView = rootView.findViewById(R.id.poi_info_tel);
        distanceView = rootView.findViewById(R.id.poi_distance);
    }

    public PoiSearchHelper(Context context, Query query,
                           View layoutRootView, RecyclerView recyclerView) {
        super(context, query);
        setOnPoiSearchListener(this);
        this.context = context;
        rootView = layoutRootView;
        this.recyclerView = recyclerView;
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        //解析result获取POI信息
        if (i == 1000) {
            //正确的处理信息
            ArrayList<PoiItem> poiSets = poiResult.getPois();
            //为适配器初始化，由于list的值在变化，故每次需要重新初始化
            if (poiSets != null) {
                PoiSearchLayoutAdapter poiSearchLayoutAdapter = new PoiSearchLayoutAdapter(context, poiSets, rootView);
                recyclerView.setAdapter(poiSearchLayoutAdapter);
            }
        } else {
            //对返回的错误码进行处理
            switch (i) {
                case 1200:
                    Snackbar.make(mapView, "请求参数非法\n错误代码：" + i, Snackbar.LENGTH_SHORT).show();
                    handleSearchError(i);
                    break;
                case 1804:
                    Snackbar.make(mapView, "网络未连接，请检查网路是否畅通\n错误代码：" + i, Snackbar.LENGTH_SHORT).show();
                    handleSearchError(i);
                    break;
                default:
                    Snackbar.make(mapView, "出现未知错误\n错误代码：" + i, Snackbar.LENGTH_SHORT).show();
                    handleSearchError(i);
            }
        }
    }

    /**
     * 当poi 单个 item反馈搜索结果，调用该函数
     *
     * @param poiItem 单个poiItem，解析该对象得到item信息
     * @param i       状态码，1000代表正常，其他的去官网查询
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onPoiItemSearched(@NotNull PoiItem poiItem, int i) {
        //返回信息正常设置为这个，否则处理异常信息
        if (i == 1000) {
            //将searchBottomSheet设为可以隐藏
            if (searchPoiSheetBehaviour.getState() == BottomSheetBehavior.STATE_SETTLING) {
                searchPoiSheetBehaviour.setHideable(true);
                searchPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
            //将viewBottomSheet设为合适的高度
            if (viewPoiSheetBehaviour.getState() == BottomSheetBehavior.STATE_SETTLING) {
                viewPoiSheetBehaviour.setHideable(false);
                viewPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            }
            //隐藏键盘
            InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(rootView.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
            //对数据进行处理并将其显示在BottomSheet中
            try {
                titleView.setText(poiItem.getTitle());
                item1View.setText(poiItem.getSnippet());
                item2View.setText(poiItem.getTypeDes());
                if (!poiItem.getTel().equals("") && poiItem.getTel() != null) {
                    telView.setText("联系电话：" + poiItem.getTel());
                } else {
                    telView.setText("暂缺该地点的联系电话");
                }

                LatLonPoint poiPoint = poiItem.getLatLonPoint();
                LatLng poiLatLng = new LatLng(poiPoint.getLatitude(), poiPoint.getLongitude());
                //移动视角
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(poiLatLng, MAP_ZOOM));
                //放置大头针
                marker = aMap.addMarker(new MarkerOptions().position(poiLatLng));
                //获得距离，前一个为解析出来的的点击的poi，后一个为当前位置
                new DistanceSearchHelper(context, distanceView)
                        .distanceQuery(poiLatLng, MainActivity.location);
            } catch (NullPointerException nullPointerException) {
                nullPointerException.printStackTrace();
                Log.e(TAG, "onPoiItemSearched: ，搜索发现空指针异常");
                titleView.setText("程序发现空指针异常");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "onPoiItemSearched: ，搜索发现异常");
                titleView.setText("发现未知异常");
            }
        } else {
            //对返回的错误码进行处理
            switch (i) {
                case 1200:
                    Snackbar.make(mapView, "请求参数非法\n错误代码：" + i, Snackbar.LENGTH_SHORT).show();
                    handleSearchError(i);
                    break;
                case 1804:
                    Snackbar.make(mapView, "网络未连接，请检查网路是否畅通\n错误代码：" + i, Snackbar.LENGTH_SHORT).show();
                    handleSearchError(i);
                    break;
                default:
                    Snackbar.make(mapView, "出现未知错误\n错误代码：" + i, Snackbar.LENGTH_SHORT).show();
                    handleSearchError(i);
            }
        }
    }

    private void handleSearchError(int errorCode) {
        try {
            viewPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            searchPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            Log.e(TAG, "onPoi(item)Searched: 异常错误代码：" + errorCode);
            titleView.setText("");
            item2View.setText("");
            item1View.setText("");
            telView.setText("");
        } catch (Exception nullPointerException) {
            nullPointerException.printStackTrace();
        }
    }
}
