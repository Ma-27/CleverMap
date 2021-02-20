package com.mamh.clevermap.model;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;

/**
 * Main Activity相关联的MapViewModel,
 */
public class MainActivityViewModel extends AndroidViewModel {
    private static final String TAG = "MapViewModel成功";
    private boolean isFirstOnCreate = true;
    private AMap modelAMap;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
    }

    public boolean isFirstOnCreate() {
        return isFirstOnCreate;
    }

    public void setFirstOnCreate(boolean firstOnCreate) {
        isFirstOnCreate = firstOnCreate;
    }

    public void setAMap(MapView mapView, int mapType) {
        Log.e(TAG, "setAMap: " + mapType);
        if (modelAMap == null) {
            modelAMap = mapView.getMap();
        } else {
            resetAMap(mapView);
        }
        setMapType(mapType);
    }

    /**
     * 在配置更改后重新绑定关联的mapView，其他不变
     * FIXME 按理说这里应该只修改aMap，重新绑定mapView，而不是重开一个对象，这个问题目前找不到解决办法
     *
     * @param mapView 与aMap关联的mapView
     */
    public void resetAMap(MapView mapView) {
        modelAMap = mapView.getMap();
    }

    public void setMapType(int mapType) {
        switch (mapType) {
            case AMap.MAP_TYPE_NAVI:
                modelAMap.setMapType(AMap.MAP_TYPE_NAVI);
                break;
            case AMap.MAP_TYPE_SATELLITE:
                modelAMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                break;
            case AMap.MAP_TYPE_NIGHT:
                modelAMap.setMapType(AMap.MAP_TYPE_NIGHT);
                break;
            case AMap.MAP_TYPE_NORMAL:
            default:
                modelAMap.setMapType(AMap.MAP_TYPE_NORMAL);
        }
        modelAMap.setTrafficEnabled(true);
    }

    public AMap getAMap() {
        return modelAMap;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        modelAMap = null;
        System.gc();
    }
}
