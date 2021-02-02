package com.mamh.clevermap.listener;

import android.annotation.SuppressLint;
import android.content.Context;
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
    public static float distance = 0.0f;
    private static final String TAG = "DistanceSearchHelper成功";
    private final DistanceQuery distanceQuery;
    private final DistanceSearch distanceSearch;
    private final TextView distanceTextView;
    private float distanceSum = 0.0f;
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
        List<LatLonPoint> latLonPoints = new ArrayList<>();
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
        //检查网络和服务是否正常
        if (i != 1000) {
            distanceTextView.setText("网络或服务器出错啦，错误代码" + i);
        } else {
            try {
                //算个平均值
                for (DistanceItem item :
                        distanceResult.getDistanceResults()) {
                    distanceSum = item.getDistance() + distanceSum;
                    num++;
                }
                distance = distanceSum / num;
                //更新距离,要是比1000米多，就显示千米
                if (distance / 1000 > 1) {
                    distanceTextView.setText("距你直线\n" + distance / 1000.0f + "千米");
                } else {
                    distanceTextView.setText("距你直线\n" + distance + "米");
                }
            } catch (NullPointerException nullPointerException) {
                nullPointerException.printStackTrace();
                distanceTextView.setText("空指针异常");
            } catch (Exception e) {
                distanceTextView.setText("遇到未知异常，api返回查询距离状态码" + i);
                e.printStackTrace();
            }
        }
    }
}
