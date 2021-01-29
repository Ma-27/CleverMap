package com.mamh.clevermap.listener;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.mamh.clevermap.R;

import org.jetbrains.annotations.NotNull;

public class PoiSearchHelper extends PoiSearch implements PoiSearch.OnPoiSearchListener {
    private final Context context;
    private final View rootView;
    private final TextView titleView;
    private final TextView item1View;
    private final TextView item2View;
    private final TextView telView;

    public PoiSearchHelper(Context context, Query query, View layoutRootView) {
        super(context, query);
        setOnPoiSearchListener(this);
        this.context = context;
        this.rootView = layoutRootView;
        titleView = rootView.findViewById(R.id.poi_name);
        item1View = rootView.findViewById(R.id.poi_item1);
        item2View = rootView.findViewById(R.id.poi_item2);
        telView = rootView.findViewById(R.id.poi_info_tel);
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {

    }

    @Override
    public void onPoiItemSearched(@NotNull PoiItem poiItem, int i) {
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
            item1View.setText("搜索中发现空指针异常");
        } catch (Exception e) {
            e.printStackTrace();
            item1View.setText("发现未知异常");
        }
    }
}
