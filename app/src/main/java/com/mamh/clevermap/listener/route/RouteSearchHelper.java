package com.mamh.clevermap.listener.route;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.mamh.clevermap.overlay.BusRouteOverlay;
import com.mamh.clevermap.overlay.DriveRouteOverlay;
import com.mamh.clevermap.overlay.RideRouteOverlay;
import com.mamh.clevermap.overlay.WalkRouteOverlay;
import com.mamh.clevermap.util.ErrorHandler;

public class RouteSearchHelper extends RouteSearch
        implements RouteSearch.OnRouteSearchListener {
    private static final String TAG = "RouteSearchHelper成功";
    private final Context context;
    //步行图层和结果处理
    private WalkRouteOverlay walkRouteOverlay;
    //骑行图层和结果处理
    private RideRouteOverlay rideRouteOverlay;
    //驾车图层和结果处理
    private DriveRouteOverlay driveRouteOverlay;


    public RouteSearchHelper(Context context) {
        super(context);
        this.context = context;
        setRouteSearchListener(this);
    }

    /**
     * 解析并展示公交与步行结合路线
     *
     * @param busRouteResult 后端发回的步行路线
     * @param i              状态码，1000为正常，其他状态码进行异常处理
     */
    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
        if (i == 1000) {
            //公交路径，这里仅仅绘制一个图层，采用第一个
            BusPath busPath = busRouteResult.getPaths().get(0);
            //公交图层和结果处理
            BusRouteOverlay busRouteOverlay = new BusRouteOverlay(context, busPath,
                    busRouteResult.getStartPos(), busRouteResult.getTargetPos());
            //将busPath添加到地图
            busRouteOverlay.addToMap();
            //移动和缩放到导航位置
            busRouteOverlay.zoomToSpan();
        } else {
            Log.e(TAG, "onBusRouteSearched: 计算公交路线时发生异常，详情见下方");
            String err = ErrorHandler.handleErrorCode(i);
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 解析并展示驾车路线
     *
     * @param driveRouteResult 后端发回的驾车路线
     * @param i                状态码，1000为正常，其他状态码进行异常处理
     */
    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
        if (i == 1000) {
            //驾车路径，这里仅仅绘制一个图层，采用第一个
            DrivePath drivePath = driveRouteResult.getPaths().get(0);
            //如果该图层具有内容，则移除添加新的内容
            if (driveRouteOverlay != null) {
                driveRouteOverlay.remove();
            }
            driveRouteOverlay = new DriveRouteOverlay(context, drivePath,
                    driveRouteResult.getStartPos(), driveRouteResult.getTargetPos());
            //将drivePath添加到地图
            driveRouteOverlay.addToMap();
            //移动和缩放到导航位置
            driveRouteOverlay.zoomToSpan();
        } else {
            Log.e(TAG, "onDriveRouteSearched: 计算驾车路线时发生异常，详情见下方");
            String err = ErrorHandler.handleErrorCode(i);
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 解析并展示步行路线
     *
     * @param walkRouteResult 后端发回的步行路线
     * @param i               状态码，1000为正常，其他状态码进行异常处理
     */
    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
        //如若一切正常，开始绘制图层，否则异常处理
        if (i == 1000) {
            //步行路径，这里仅仅绘制一个图层，采用第一个
            WalkPath walkPath = walkRouteResult.getPaths().get(0);
            //如果该图层具有内容，则移除添加新的内容
            if (walkRouteOverlay != null) {
                walkRouteOverlay.remove();
            }
            //最后两个参数为LatLonPoint对象
            walkRouteOverlay = new WalkRouteOverlay(context, walkPath,
                    walkRouteResult.getStartPos(),
                    walkRouteResult.getTargetPos());
            //将walkPath添加到地图
            walkRouteOverlay.addToMap();
            //移动和缩放到导航位置
            walkRouteOverlay.zoomToSpan();
        } else {
            Log.e(TAG, "onWalkRouteSearched: 计算步行路线时发生异常，详情见下方");
            String err = ErrorHandler.handleErrorCode(i);
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 解析并展示骑行路线
     *
     * @param rideRouteResult 后端发回的骑行路线
     * @param i               状态码，1000为正常，其他状态码进行异常处理
     */
    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
        if (i == 1000) {
            //骑行路径，这里仅仅绘制一个图层，采用第一个搜索结果
            RidePath ridePath = rideRouteResult.getPaths().get(0);
            //如果该图层具有内容，则移除添加新的内容
            if (rideRouteOverlay != null) {
                rideRouteOverlay.remove();
            }
            //最后两个参数为LatLonPoint对象
            rideRouteOverlay = new RideRouteOverlay(context, ridePath,
                    rideRouteResult.getStartPos(),
                    rideRouteResult.getTargetPos());
            //将ridePath添加到地图
            rideRouteOverlay.addToMap();
            //移动和缩放到导航位置
            rideRouteOverlay.zoomToSpan();
        } else {
            Log.e(TAG, "onRideRouteSearched: 计算骑行路线时发生异常，详情见下方");
            String err = ErrorHandler.handleErrorCode(i);
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
        }
    }
}
