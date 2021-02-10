package com.mamh.clevermap.util;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;

import java.util.ArrayList;
import java.util.List;

public class AmapUtilityTools {
    /**
     * 用来将LatLonPoint单点转化为latLng类
     *
     * @param latLonPoint latLon点
     * @return latLon经纬度，直接访问经度和纬度
     */
    public static LatLng convertToLatLng(LatLonPoint latLonPoint) {
        return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
    }

    /**
     * 用来将LatLonPoint队形转化为latLonPoint，和上面的方法互逆
     *
     * @param latLng 经纬度，这里只有经度和纬度
     * @return latLon经纬度点
     */
    public static LatLonPoint convertToLatLonPoint(LatLng latLng) {
        return new LatLonPoint(latLng.latitude, latLng.longitude);
    }

    /**
     * 用来将LatLonPoint单点转化为latLng类线性表（集合）
     *
     * @param pointList 待转化的LatLonPoint点集合（List）
     * @return 转化完成的LatLng线性表
     */
    public static ArrayList<LatLng> convertToLatLngArrList(List<LatLonPoint> pointList) {
        ArrayList<LatLng> lineShapes = new ArrayList<>();
        for (LatLonPoint point : pointList) {
            LatLng latLngTemp = AmapUtilityTools.convertToLatLng(point);
            lineShapes.add(latLngTemp);
        }
        return lineShapes;
    }

    /**
     * 用来直接将经纬度转化为latLng类
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return latLng类对象
     */
    public static LatLng convertToLatLng(double latitude, double longitude) {
        return new LatLng(latitude, longitude);
    }
}
