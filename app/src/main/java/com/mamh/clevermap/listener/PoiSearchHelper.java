package com.mamh.clevermap.listener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mamh.clevermap.R;

import org.jetbrains.annotations.NotNull;

public class PoiSearchHelper extends PoiSearch implements PoiSearch.OnPoiSearchListener {
    private static final String TAG = "PoiSearchHelper成功";
    private BottomSheetBehavior viewPoiSheetBehaviour, searchPoiSheetBehaviour;
    private final Context context;
    private View rootView = null;
    private TextView titleView = null, item1View = null, item2View = null, telView = null;

    /**
     * 响应来自poi的搜索请求
     *
     * @param context
     * @param query
     * @param layoutRootView
     */
    @SuppressLint("NonConstantResourceId")
    public PoiSearchHelper(Context context, Query query, View layoutRootView) {
        super(context, query);
        setOnPoiSearchListener(this);
        this.context = context;
        this.rootView = layoutRootView;
        titleView = rootView.findViewById(R.id.textView_poi_title);
        item1View = rootView.findViewById(R.id.poi_item1);
        item2View = rootView.findViewById(R.id.poi_item2);
        telView = rootView.findViewById(R.id.poi_info_tel);

        switch (layoutRootView.getId()) {
            case R.id.search_sheet_linear_layout:
                searchPoiSheetBehaviour = BottomSheetBehavior.from(layoutRootView);
                break;
            case R.id.poi_sheet_linear_layout:
                viewPoiSheetBehaviour = BottomSheetBehavior.from(layoutRootView);
                break;
            default:
                Log.w(TAG, "找到的layout id与预期不符，id为：" + layoutRootView.getId());
        }

    }

    public PoiSearchHelper(Context context, Query query) {
        super(context, query);
        this.context = context;
        setOnPoiSearchListener(this);
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        //解析result获取POI信息
        if (i == 1000) {
            //正确的处理信息
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onPoiItemSearched(@NotNull PoiItem poiItem, int i) {
        //设置个BottomSheet的可见性
        /*
        searchPoiSheetBehaviour.setHideable(true);
        searchPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);

        viewPoiSheetBehaviour.setHideable(false);
        viewPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
         */

        if (i != 1000) {
            titleView.setText("网络异常\n错误代码：" + i);
            item2View.setText("");
            item1View.setText("");
            telView.setText("");
        } else {
            try {
                titleView.setText(poiItem.getTitle());
                item1View.setText(poiItem.getSnippet());
                item2View.setText(poiItem.getTypeDes());
                if (!poiItem.getTel().equals("") && poiItem.getTel() != null) {
                    telView.setText("联系电话：" + poiItem.getTel());
                } else {
                    telView.setText("暂缺该地点的联系电话");
                }
            } catch (NullPointerException nullPointerException) {
                nullPointerException.printStackTrace();
                Log.e(TAG, "onPoiItemSearched: ，搜索发现空指针异常");
                titleView.setText("程序发现空指针异常");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "onPoiItemSearched: ，搜索发现异常");
                titleView.setText("发现未知异常");
            }
        }
    }
}
