package com.mamh.clevermap.overlay;

import android.content.Context;
import android.util.Log;

import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkStep;
import com.mamh.clevermap.util.AmapUtilityTools;

import java.util.List;

import static com.mamh.clevermap.activity.RouteActivity.routeAMap;

/**
 * 该类负责在地图中描绘步行路线
 */
public class WalkRouteOverlay extends MyRouteOverlay {
    private static final String TAG = "WalkRouteOverlay成功";
    private WalkPath walkPath;
    //多段线
    private PolylineOptions polylineOptions;
    //在marker上的小图标，点按它可以提示路径信息
    private BitmapDescriptor walkStationDescriptor = null;

    public WalkRouteOverlay(Context context) {
        super(context);
    }

    /**
     * 通过此构造函数创建步行路线图层。
     *
     * @param context  当前activity。
     * @param walkPath 步行路线规划的一个方案。详见搜索服务模块的路径查询包
     *                 （com.amap.api.services.route）中的类
     * @param start    起点。详见搜索服务模块的核心基础包（com.amap.api.services.core）中的类
     * @param end      终点。详见搜索服务模块的核心基础包（com.amap.api.services.core）中的类
     */
    public WalkRouteOverlay(Context context, WalkPath walkPath,
                            LatLonPoint start, LatLonPoint end) {
        super(context);
        this.aMap = routeAMap;
        this.walkPath = walkPath;
        startPoint = AmapUtilityTools.convertToLatLng(start);
        endPoint = AmapUtilityTools.convertToLatLng(end);
    }


    /**
     * 添加步行路线到地图中。
     */
    public void addToMap() {
        initLineAndDotOptions();
        try {
            List<WalkStep> walkSteps = walkPath.getSteps();
            polylineOptions.add(startPoint);
            for (int i = 0; i < walkSteps.size(); i++) {
                WalkStep walkStep = walkSteps.get(i);
                LatLng latLng = AmapUtilityTools.convertToLatLng(walkStep
                        .getPolyline().get(0));
                //加步行图标和线，addMarker直接在地图上展示marker
                addWalkStationMarkers(walkStep, latLng);
                //在线性表中添加线段
                addWalkPolyLines(walkStep);
            }
            polylineOptions.add(endPoint);
            //添加起点和终点
            addStartAndEndMarker();
            //在地图中描线
            addPolyLine(polylineOptions);
        } catch (NullPointerException nullPointerException) {
            Log.e(TAG, "addToMap: 空指针异常");
            nullPointerException.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "addToMap: 出现未知异常");
            e.printStackTrace();
        }
    }

    /**
     * 初始化线段属性
     */
    private void initLineAndDotOptions() {
        if (walkStationDescriptor == null) {
            walkStationDescriptor = getWalkBitmapDescriptor();
        }
        polylineOptions = new PolylineOptions();
        //polylineOptions.color(MyRouteOverlay.getLineColor()).width(MyRouteOverlay.getLineWidth());
        //使用了指示图来设置填充，没有用到单一线来填充
        polylineOptions.setCustomTexture(getLineBitmapDescriptor());
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

    protected void addStartAndEndMarker() {
        //设置起点图标
        startMarker = routeAMap.addMarker((new MarkerOptions())
                .position(startPoint).icon(getStartBitmapDescriptor())
                .title("起点"));
        //设置终点图标
        endMarker = routeAMap.addMarker((new MarkerOptions()).position(endPoint)
                .icon(getEndBitmapDescriptor()).title("终点"));
    }

    /**
     * 在图层中描出线段
     *
     * @param options 前面构建的marker和线属性的的集合
     */
    protected void addPolyLine(PolylineOptions options) {
        try {
            Polyline polyline = routeAMap.addPolyline(options);
            allPolyLines.add(polyline);
        } catch (NullPointerException nullPointerException) {
            Log.e(TAG, "addPolyLine: 出现空指针异常");
            nullPointerException.printStackTrace();
        }
    }

    /**
     * 移动镜头到当前的视角。
     */
    public void zoomToSpan() {
        try {
            LatLngBounds bounds = getLatLngBounds();
            routeAMap.animateCamera
                    (CameraUpdateFactory.newLatLngBounds(bounds, getBoundsZoom()));
        } catch (NullPointerException e) {
            Log.e(TAG, "zoomToSpan: 出现空指针异常");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "zoomToSpan: 出现异常，不明");
            e.printStackTrace();
        }
    }
}
