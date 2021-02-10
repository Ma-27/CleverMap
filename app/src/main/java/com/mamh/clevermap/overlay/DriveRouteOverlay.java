package com.mamh.clevermap.overlay;

import android.content.Context;
import android.util.Log;

import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveStep;
import com.mamh.clevermap.util.AmapUtilityTools;

import java.util.List;

/**
 * 该类负责在地图中描绘驾车路线，若有路况情况，则显示驾车时路况
 */
public class DriveRouteOverlay extends MyRouteOverlay {
    private static final String TAG = "DriveRouteOverlay成功";
    private DrivePath drivePath;
    //在marker上的小图标，点按它可以提示路径信息
    private BitmapDescriptor driveStationDescriptor = null;
    private PolylineOptions polylineOptions;
    //private List<LatLng> mLatLngsOfPath = new ArrayList<>();
    //private List<TMC> tmcs = new ArrayList<>();

    /**
     * 根据给定的参数，构造一个导航路线图层类对象。
     *
     * @param context    当前的activity对象。
     * @param drivePath  驾车路线规划的一个方案。
     * @param startPoint 起点。
     * @param endPoint   终点。
     */
    public DriveRouteOverlay(Context context, DrivePath drivePath,
                             LatLonPoint startPoint, LatLonPoint endPoint) {
        super(context);
        this.drivePath = drivePath;
        this.start = AmapUtilityTools.convertToLatLng(startPoint);
        this.end = AmapUtilityTools.convertToLatLng(endPoint);
    }

    public DriveRouteOverlay(Context context) {
        super(context);
    }

    public void addToMap() {
        initLineAndDotOptions();
        try {
            List<DriveStep> driveSteps = drivePath.getSteps();
            polylineOptions.add(start);
            for (int i = 0; i < driveSteps.size(); i++) {
                DriveStep driveStep = driveSteps.get(i);
                LatLng latLng = AmapUtilityTools.convertToLatLng(driveStep
                        .getPolyline().get(0));

                //FIXME:本来想加入路况显示，考虑到已有路况线段在图上，故不显示
                //List<LatLonPoint> latlonPoints = driveStep.getPolyline();
                //List<TMC> tmclist = driveStep.getTMCs();
                //tmcs.addAll(tmclist);

                //加步行图标和线，addMarker直接在地图上展示marker
                addDriveStationMarkers(driveStep, latLng);
                //在线性表中添加线段
                addDrivePolyLines(driveStep);
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
        if (driveStationDescriptor == null) {
            driveStationDescriptor = getDriveBitmapDescriptor();
        }
        polylineOptions = new PolylineOptions();
        //使用了指示图来设置填充，没有用到单一线来填充
        polylineOptions.setCustomTexture(getLineBitmapDescriptor());
    }

    private void addDriveStationMarkers(DriveStep driveStep, LatLng position) {
        addStationMarker(new MarkerOptions()
                .position(position)
                .snippet(driveStep.getInstruction()).visible(true)
                .anchor(0.5f, 0.5f).icon(driveStationDescriptor));
    }

    private void addDrivePolyLines(DriveStep driveStep) {
        polylineOptions.addAll(AmapUtilityTools
                .convertToLatLngArrList(driveStep.getPolyline()));
    }

}
