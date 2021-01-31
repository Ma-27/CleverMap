package com.mamh.clevermap.listener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Poi;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mamh.clevermap.R;
import com.mamh.clevermap.interfaces.OnPoiChange;

public class PoiViewBottomSheetHandler extends BottomSheetBehavior.BottomSheetCallback
        implements OnPoiChange {
    private static final String TAG = "BottomSheetEventHandler成功";
    private final Context context;
    //布局和View
    private final View rootLayout;
    private final TextView titleTextView;
    private final TextView distanceTextView;
    private Poi poi = null;
    private LatLng currentLocation = null;

    public PoiViewBottomSheetHandler(Context context, View rootLayout) {
        this.context = context;
        //设置TextView
        titleTextView = rootLayout.findViewById(R.id.textView_poi_title);
        distanceTextView = rootLayout.findViewById(R.id.poi_distance);
        this.rootLayout = rootLayout;
    }

    /**
     * 当BottomSheet上拉，加载Poi详细信息
     *
     * @param bottomSheet poi响应的卡片
     * @param newState    转换时的新状态
     */
    @SuppressLint("LongLogTag")
    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
            try {
                //设置参数并开始搜索
                PoiSearchHelper poiSearchHelper = new PoiSearchHelper(context, null, rootLayout);
                poiSearchHelper.searchPOIIdAsyn(poi.getPoiId());// 异步搜索
            } catch (Exception nullPointerException) {
                nullPointerException.printStackTrace();
            }
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

    }

    public void setPoi(Poi poi) {
        this.poi = poi;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void updatePOIText() {
        try {
            titleTextView.setText(poi.getName());
            LatLng position = poi.getCoordinate();
            new DistanceSearchHelper(context, distanceTextView).distanceQuery(position, currentLocation);
        } catch (NullPointerException nullPointerException) {
            nullPointerException.printStackTrace();
            titleTextView.setText("出现空指针异常");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Main activity中设置当前位置
     *
     * @param Location 由MainActivity传入当前位置
     */
    public void setCurrentLocation(LatLng Location) {
        currentLocation = Location;
    }
}
