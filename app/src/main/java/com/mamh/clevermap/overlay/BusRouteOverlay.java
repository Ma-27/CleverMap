package com.mamh.clevermap.overlay;

import android.content.Context;
import android.util.Log;

import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteBusWalkItem;
import com.amap.api.services.route.WalkStep;
import com.mamh.clevermap.util.AmapUtilityTools;

import java.util.List;

public class BusRouteOverlay extends MyRouteOverlay {
    private static final String TAG = "BusRouteQuery成功";
    private BusPath busPath;
    //在marker上的小图标，点按它可以提示路径信息
    private BitmapDescriptor busStationDescriptor, walkStationDescriptor;
    private PolylineOptions polylineOptions;

    public BusRouteOverlay(Context context) {
        super(context);
    }

    /**
     * 通过此构造函数创建公交路线图层。
     *
     * @param context    当前activity。
     * @param busPath    公交路线规划的一个方案。
     * @param startPoint 起点。
     * @param endPoint   终点。
     */
    public BusRouteOverlay(Context context, BusPath busPath,
                           LatLonPoint startPoint, LatLonPoint endPoint) {
        super(context);
        this.busPath = busPath;
        this.start = AmapUtilityTools.convertToLatLng(startPoint);
        this.end = AmapUtilityTools.convertToLatLng(endPoint);
    }

    public void addToMap() {
        initLineAndDotOptions();
        try {
            List<BusStep> busSteps = busPath.getSteps();
            polylineOptions.add(start);
            //添加起点和终点
            addStartAndEndMarker();

            for (int i = 0; i < busSteps.size(); i++) {
                //公交路径规划的一个路段（类 BusStep），必存在一段公交导航信息，最多包含一段步行信息。
                BusStep busStep = busSteps.get(i);
                List<RouteBusLineItem> busLineItems = busStep.getBusLines();
                //描绘步行线路
                RouteBusWalkItem walkItem = busStep.getWalk();
                List<WalkStep> walkSteps = walkItem.getSteps();
                for (WalkStep walkStep :
                        walkSteps) {
                    LatLng latLng = AmapUtilityTools.convertToLatLng(walkStep
                            .getPolyline().get(i));
                    //加步行图标和线，addMarker直接在地图上展示marker
                    addWalkStationMarkers(walkStep, latLng);
                    //在线性表中添加线段
                    addWalkPolyLines(walkStep);
                    //在地图中描线
                    drawPolyLine(polylineOptions);
                }

                //描绘每段公交线路
                RouteBusLineItem busLineItem = busLineItems.get(i);
                //描绘起点公交站
                LatLng startLatLng = AmapUtilityTools.convertToLatLng
                        (busLineItem.getDepartureBusStation().getLatLonPoint());
                addDepartureBusLineStationMarkers(busLineItem, startLatLng);
                //添加公交线段
                addBusPolyLines(busLineItem);
                //描绘终点公交站
                LatLng endLatLng = AmapUtilityTools.convertToLatLng
                        (busLineItem.getArrivalBusStation().getLatLonPoint());
                addArrivalBusLineStationMarkers(busLineItem, endLatLng);
                //在地图中描线
                drawPolyLine(polylineOptions);
            }
            polylineOptions.add(end);
            //在地图中描线
        } catch (NullPointerException nullPointerException) {
            Log.e(TAG, "addToMap: 空指针异常");
            nullPointerException.printStackTrace();
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            Log.e(TAG, "addToMap: 数组越界（非法访问）异常");
            indexOutOfBoundsException.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "addToMap: 出现未知异常");
            e.printStackTrace();
        }
    }

    private void initLineAndDotOptions() {
        if (busStationDescriptor == null) {
            busStationDescriptor = getBusBitmapDescriptor();
        }

        if (walkStationDescriptor == null) {
            walkStationDescriptor = getWalkBitmapDescriptor();
        }
        polylineOptions = new PolylineOptions();
        //使用了指示图来设置填充，没有用到单一线来填充
        polylineOptions.setCustomTexture(getLineBitmapDescriptor());
    }

    /**
     * 添加出发公交站的marker
     *
     * @param busLineItem 单个公交线路
     * @param position    marker的位置
     */
    private void addDepartureBusLineStationMarkers(RouteBusLineItem busLineItem, LatLng position) {
        addStationMarker(new MarkerOptions()
                .position(position)
                .snippet(busLineItem.getBusLineName() + "\n从该位置（" +
                        busLineItem.getDepartureBusStation().getBusStationName() + "）站乘车\n在" +
                        busLineItem.getArrivalBusStation().getBusStationName() + "站下车")
                .visible(true)
                .anchor(0.5f, 0.5f).icon(busStationDescriptor));
    }

    /**
     * 添加出发公交站的marker
     *
     * @param busLineItem 单个公交线路
     * @param position    marker的位置
     */
    private void addArrivalBusLineStationMarkers(RouteBusLineItem busLineItem, LatLng position) {
        addStationMarker(new MarkerOptions()
                .position(position)
                .snippet(busLineItem.getBusLineName() + "\n在该位置（" +
                        busLineItem.getArrivalBusStation().getBusStationName() + "）站下车")
                .visible(true)
                .anchor(0.5f, 0.5f).icon(busStationDescriptor));
    }

    private void addBusPolyLines(RouteBusLineItem busLineItem) {
        polylineOptions.addAll(AmapUtilityTools
                .convertToLatLngArrList(busLineItem.getPolyline()));
    }

    /**
     * 加入拐角marker
     *
     * @param walkStep 单个路径的信息
     * @param position 加入marker的位置信息
     */
    private void addWalkStationMarkers(WalkStep walkStep, LatLng position) {
        addStationMarker(new MarkerOptions()
                .position(position)
                .snippet(walkStep.getInstruction()).visible(true)
                .anchor(0.5f, 0.5f).icon(walkStationDescriptor));
    }

    /**
     * 在options中添加线段，将线段添加到线性表中
     *
     * @param walkStep 路线及路线提示，并非在图上显示的路线
     */
    private void addWalkPolyLines(WalkStep walkStep) {
        polylineOptions.addAll(AmapUtilityTools
                .convertToLatLngArrList(walkStep.getPolyline()));
    }
}
