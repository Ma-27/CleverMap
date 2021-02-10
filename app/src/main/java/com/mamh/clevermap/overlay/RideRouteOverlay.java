package com.mamh.clevermap.overlay;

import android.content.Context;
import android.util.Log;

import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideStep;
import com.mamh.clevermap.util.AmapUtilityTools;

import java.util.List;

/**
 * 该类负责在地图中描绘骑行路线
 */
public class RideRouteOverlay extends MyRouteOverlay {
    private static final String TAG = "RideRouteOverlay成功";
    private RidePath ridePath;
    //在marker上的小图标，点按它可以提示路径信息
    private BitmapDescriptor rideStationDescriptor = null;
    private PolylineOptions polylineOptions;

    /**
     * 通过此构造函数创建骑行路线图层。
     *
     * @param context    context 当前activity。
     * @param ridePath   骑行路线规划的一个方案。
     * @param startPoint 起点。
     * @param endPoint   终点。
     */
    public RideRouteOverlay(Context context, RidePath ridePath,
                            LatLonPoint startPoint, LatLonPoint endPoint) {
        super(context);
        this.ridePath = ridePath;
        this.start = AmapUtilityTools.convertToLatLng(startPoint);
        this.end = AmapUtilityTools.convertToLatLng(endPoint);
    }

    public RideRouteOverlay(Context context) {
        super(context);
    }

    public void addToMap() {
        initLineAndDotOptions();
        try {
            List<RideStep> rideSteps = ridePath.getSteps();
            polylineOptions.add(start);
            for (int i = 0; i < rideSteps.size(); i++) {
                RideStep rideStep = rideSteps.get(i);
                LatLng latLng = AmapUtilityTools.convertToLatLng(rideStep
                        .getPolyline().get(0));
                //加步行图标和线，addMarker直接在地图上展示marker
                addRideStationMarkers(rideStep, latLng);
                //在线性表中添加线段
                addRidePolyLines(rideStep);
            }
            polylineOptions.add(end);
            //添加起点和终点
            addStartAndEndMarker();
            //在地图中描线
            drawPolyLine(polylineOptions);
        } catch (NullPointerException nullPointerException) {
            Log.e(TAG, "addToMap: 空指针异常");
            nullPointerException.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "addToMap: 出现未知异常");
            e.printStackTrace();
        }
    }

    private void initLineAndDotOptions() {
        if (rideStationDescriptor == null) {
            rideStationDescriptor = getRideBitmapDescriptor();
        }
        polylineOptions = new PolylineOptions();
        //使用了指示图来设置填充，没有用到单一线来填充
        polylineOptions.setCustomTexture(getLineBitmapDescriptor());
    }

    /**
     * 加入拐角marker
     *
     * @param rideStep 单个路径的信息
     * @param position 加入marker的位置信息
     */
    private void addRideStationMarkers(RideStep rideStep, LatLng position) {
        addStationMarker(new MarkerOptions()
                .position(position)
                .snippet(rideStep.getInstruction()).visible(true)
                .anchor(0.5f, 0.5f).icon(rideStationDescriptor));
    }

    /**
     * 在options中添加线段，将线段添加到线性表中
     *
     * @param rideStep 路线及路线提示，并非在图上显示的路线
     */
    private void addRidePolyLines(RideStep rideStep) {
        polylineOptions.addAll(AmapUtilityTools
                .convertToLatLngArrList(rideStep.getPolyline()));
    }
}
