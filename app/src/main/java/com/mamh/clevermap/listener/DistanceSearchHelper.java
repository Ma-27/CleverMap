package com.mamh.clevermap.listener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DistanceItem;
import com.amap.api.services.route.DistanceResult;
import com.amap.api.services.route.DistanceSearch;

import java.util.ArrayList;
import java.util.List;

/**
 * 该类负责搜索当前定位和poi之间的距离，并在BottomSheet中显示距离
 */
public class DistanceSearchHelper extends DistanceSearch
        implements DistanceSearch.OnDistanceSearchListener {
    private static final String TAG = "DistanceSearchHelper成功";
    private final DistanceQuery distanceQuery;
    private final DistanceSearch distanceSearch;
    private final TextView distanceTextView;
    private List<DistanceItem> distanceResultList;
    private float distance, distanceSum = 0.0f;
    private int num = 0;

    public DistanceSearchHelper(Context context, TextView distanceTextView) {
        super(context);
        distanceSearch = new DistanceSearch(context);
        this.distanceTextView = distanceTextView;
        distanceSearch.setDistanceSearchListener(this);
        distanceQuery = new DistanceQuery();
    }

    /**
     * 测量距离，支持有多个起点的情况
     *
     * @param startPoints 多个起点（LatLng)对象一次传入
     * @param destination 目的地
     */
    public void distanceQuery(LatLng destination, LatLng... startPoints) {
        List<LatLonPoint> latLonPoints = new ArrayList<LatLonPoint>();
        for (LatLng position : startPoints) {
            latLonPoints.add(new LatLonPoint(position.latitude, position.longitude));
        }
        distanceQuery.setOrigins(latLonPoints);
        distanceQuery.setDestination(new LatLonPoint(destination.latitude, destination.longitude));
        //设置测量方式，支持直线和驾车(这里为直线）
        distanceQuery.setType(DistanceSearch.TYPE_DISTANCE);
        //发送请求
        distanceSearch.calculateRouteDistanceAsyn(distanceQuery);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onDistanceSearched(DistanceResult distanceResult, int i) {
        distanceResult.getDistanceResults();
        try {
            //算个平均值
            for (DistanceItem item :
                    distanceResult.getDistanceResults()) {
                distanceSum = item.getDistance() + distanceSum;
                num++;
            }
            distance = distanceSum / num;

            distanceTextView.setText("当前位置距该点\n" + distance + "米");
            Log.d(TAG, "onDistanceSearched: " + distance);
        } catch (NullPointerException nullPointerException) {
            nullPointerException.printStackTrace();
            distanceTextView.setText("空指针异常");
        } catch (Exception e) {
            distanceTextView.setText("遇到未知异常，api返回查询距离状态码" + i);
            e.printStackTrace();
        }
    }
}
