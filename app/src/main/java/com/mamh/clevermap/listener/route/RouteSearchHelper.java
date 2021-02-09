package com.mamh.clevermap.listener.route;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.mamh.clevermap.overlay.WalkRouteOverlay;
import com.mamh.clevermap.util.ErrorHandler;

public class RouteSearchHelper extends RouteSearch
        implements RouteSearch.OnRouteSearchListener {
    private static final String TAG = "RouteSearchHelper成功";
    private final Context context;
    //步行图层和结果处理
    private WalkRouteOverlay walkRouteOverlay;
    private WalkRouteResult walkRouteResult;

    public RouteSearchHelper(Context context) {
        super(context);
        this.context = context;
        setRouteSearchListener(this);
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
        if (i == 1000) {

        } else {
            Log.e(TAG, "onBusRouteSearched: 计算路线时发生异常，详情见下方");
            String err = ErrorHandler.handleErrorCode(i);
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
        if (i == 1000) {

        } else {
            Log.e(TAG, "onDriveRouteSearched: 计算路线时发生异常，详情见下方");
            String err = ErrorHandler.handleErrorCode(i);
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 解析并展示步行路线
     *
     * @param walkRouteResult 后端发回的步行路线
     * @param i               状态码，1000为正常
     */
    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
        //如若一切正常，开始绘制图层，否则异常处理
        if (i == 1000) {
            this.walkRouteResult = walkRouteResult;
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
            Log.e(TAG, "onWalkRouteSearched: 计算路线时发生异常，详情见下方");
            String err = ErrorHandler.handleErrorCode(i);
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
        if (i == 1000) {

        } else {
            Log.e(TAG, "onRideRouteSearched: 计算路线时发生异常，详情见下方");
            String err = ErrorHandler.handleErrorCode(i);
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
        }
    }
}
