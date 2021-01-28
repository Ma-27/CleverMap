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

public class BottomSheetEventHandler extends BottomSheetBehavior.BottomSheetCallback
        implements OnPoiChange {
    private static final String TAG = "BottomSheetEventHandler成功";
    private final Context context;
    private final TextView titleTextView;
    private final TextView distanceTextView;
    //Toast.makeText(relatedComponentContext,"触发",Toast.LENGTH_SHORT).show();
    private Poi poi = null;
    private LatLng currentLocation = null;

    public BottomSheetEventHandler(Context context, View rootLayout) {
        this.context = context;
        //设置TextView
        titleTextView = rootLayout.findViewById(R.id.poi_name);
        distanceTextView = rootLayout.findViewById(R.id.poi_distance);
    }

    /**
     * 当BottomSheet上下拉动，发生变化时处理状态
     *
     * @param bottomSheet poi响应的卡片
     * @param newState    转换时的新状态
     */
    @SuppressLint("LongLogTag")
    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {

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
     * @param Location
     */
    public void setCurrentLocation(LatLng Location) {
        currentLocation = Location;
    }
}
