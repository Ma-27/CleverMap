package com.mamh.clevermap.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.mamh.clevermap.R;

import java.util.ArrayList;
import java.util.List;

import static com.mamh.clevermap.activity.RouteActivity.routeAMap;

/**
 * 该类是getter和setter类，用来准备资源和数据
 */
public class MyRouteOverlay {
    private static final String TAG = "MyRouteOverlay成功";
    //设置路线颜色（这里定死了，改不了）
    private static final int LINE_COLOR = Color.argb(100, 0, 0, 255);
    //设置路线宽度（这里定死了，改不了）
    private static final int LINE_WIDTH = 18;
    //设置缩放范围，边上适当留了一点空间（这里定死了，改不了）
    private static final int BOUNDS_ZOOM = 60;

    protected final Context context;
    protected final List<Marker> stationMarkers = new ArrayList<>();
    //线的线性表（线的聚合）
    protected final List<Polyline> allPolyLines = new ArrayList<>();
    //开始和结束的Marker
    protected Marker startMarker, endMarker;
    protected LatLng start, end;
    protected AMap aMap;
    //标识的小图标
    private Bitmap startBit, endBit, busBit, walkBit, driveBit;
    //设置各个转弯节点可见性，默认可见
    protected boolean isNodeIconVisible = true;

    public MyRouteOverlay(Context context) {
        this.context = context;
    }

    /**
     * 图层通过该函数获得预设的线段color
     *
     * @return 返回颜色值，为整数
     */
    protected static int getLineColor() {
        return LINE_COLOR;
    }

    /**
     * 图层通过该函数获得预设的线段宽度
     *
     * @return 返回宽度，为整数
     */
    protected static int getLineWidth() {
        return LINE_WIDTH;
    }

    /**
     * 图层通过该函数获得预设的线段宽度
     *
     * @return 返回宽度，为整数
     */
    public static int getBoundsZoom() {
        return BOUNDS_ZOOM;
    }

    /**
     * 移除各种marker和line
     */
    public void remove() {
        if (startMarker != null) {
            startMarker.remove();
        }
        if (endMarker != null) {
            endMarker.remove();
        }
        for (Marker marker : stationMarkers) {
            marker.remove();
        }
        for (Polyline line : allPolyLines) {
            line.remove();
        }
        destroyBit();
    }

    /**
     * 销毁小图标
     */
    private void destroyBit() {
        if (startBit != null) {
            startBit.recycle();
            startBit = null;
        }
        if (endBit != null) {
            endBit.recycle();
            endBit = null;
        }
        if (busBit != null) {
            busBit.recycle();
            busBit = null;
        }
        if (walkBit != null) {
            walkBit.recycle();
            walkBit = null;
        }
        if (driveBit != null) {
            driveBit.recycle();
            driveBit = null;
        }
    }

    /**
     * 添加marker
     *
     * @param options 用来添加marker
     */
    protected void addStationMarker(MarkerOptions options) {
        try {
            Marker marker = routeAMap.addMarker(options);
            stationMarkers.add(marker);
        } catch (NullPointerException nullPointerException) {
            Log.e(TAG, "addStationMarker: 传入参数空指针异常");
            nullPointerException.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "addStationMarker: 出现异常");
            e.printStackTrace();
        }
    }

    /**
     * 给路径中拐点Marker设置图标，并返回更换图标的图片。如不用默认图片，需要重写此方法。
     *
     * @return 要更换的Marker图片。
     */
    protected BitmapDescriptor getStartBitmapDescriptor() {
        return BitmapDescriptorFactory.fromResource(R.drawable.amap_start);
    }


    /**
     * 给终点Marker设置图标，并返回更换图标的图片。如不用默认图片，需要重写此方法。
     *
     * @return 更换的Marker图片。
     */
    protected BitmapDescriptor getEndBitmapDescriptor() {
        return BitmapDescriptorFactory.fromResource(R.drawable.amap_end);
    }

    /**
     * 在图上添加起点和终点
     */
    protected void addStartAndEndMarker() {
        //设置起点图标
        startMarker = routeAMap.addMarker((new MarkerOptions())
                .position(start).icon(getStartBitmapDescriptor())
                .title("起点"));
        //设置终点图标
        endMarker = routeAMap.addMarker((new MarkerOptions()).position(end)
                .icon(getEndBitmapDescriptor()).title("终点"));
    }

    /**
     * 在图层中描出线段
     *
     * @param options 前面构建的marker和线属性的的集合
     */
    protected void drawPolyLine(PolylineOptions options) {
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

    /**
     * 给步行Marker设置图标，并返回更换图标的图片。如不用默认图片，需要重写此方法。
     *
     * @return 更换的Marker图片。
     */
    protected BitmapDescriptor getWalkBitmapDescriptor() {
        return BitmapDescriptorFactory.fromResource(R.drawable.amap_man);
    }

    /**
     * 给骑行Marker设置图标，并返回更换图标的图片。如不用默认图片，需要重写此方法。
     *
     * @return 更换的Marker图片。
     */
    protected BitmapDescriptor getRideBitmapDescriptor() {
        return BitmapDescriptorFactory.fromResource(R.drawable.amap_ride);
    }

    /**
     * 给驾车Marker设置图标，并返回更换图标的图片。如不用默认图片，需要重写此方法。
     *
     * @return 更换的Marker图片。
     */
    protected BitmapDescriptor getDriveBitmapDescriptor() {
        return BitmapDescriptorFactory.fromResource(R.drawable.amap_car);
    }

    /**
     * 给公交Marker设置图标，并返回更换图标的图片。如不用默认图片，需要重写此方法。
     *
     * @return 更换的Marker图片。
     */
    protected BitmapDescriptor getBusBitmapDescriptor() {
        return BitmapDescriptorFactory.fromResource(R.drawable.amap_bus);
    }


    /**
     * 给路线填充，并返回填充的图片。如不用默认图片，需要重写此方法。
     *
     * @return 更换的路径填充图片。
     */
    protected BitmapDescriptor getLineBitmapDescriptor() {
        return BitmapDescriptorFactory.fromResource(R.drawable.up);
    }


    /**
     * 获取边界，用于camera移动和确定范围
     *
     * @return 一个边界对象
     */
    protected LatLngBounds getLatLngBounds() {
        LatLngBounds.Builder b = LatLngBounds.builder();
        b.include(new LatLng(start.latitude, start.longitude));
        b.include(new LatLng(end.latitude, end.longitude));
        for (Polyline polyline : allPolyLines) {
            for (LatLng point : polyline.getPoints()) {
                b.include(point);
            }
        }
        return b.build();
    }

    /**
     * 控制路段节点图标是否显示。
     *
     * @param visible true为显示节点图标，false为不显示。
     */
    public void setNodeIconVisibility(boolean visible) {
        try {
            isNodeIconVisible = visible;
            if (this.stationMarkers.size() > 0) {
                for (int i = 0; i < this.stationMarkers.size(); i++) {
                    this.stationMarkers.get(i).setVisible(visible);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "setNodeIconVisibility: 出现未知异常");
            e.printStackTrace();
        }
    }
}
