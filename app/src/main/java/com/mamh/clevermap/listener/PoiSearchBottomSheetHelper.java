package com.mamh.clevermap.listener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.poisearch.PoiSearch;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mamh.clevermap.R;
import com.mamh.clevermap.adapter.PoiSearchAdapter;

import static com.mamh.clevermap.activity.MainActivity.searchPoiSheetBehaviour;
import static com.mamh.clevermap.activity.MainActivity.viewPoiSheetBehaviour;

/**
 * 该类用于响应搜索poi的bottomSheet
 */
public class PoiSearchBottomSheetHelper extends
        BottomSheetBehavior.BottomSheetCallback implements androidx.appcompat.widget.SearchView.OnQueryTextListener {
    private static final String TAG = "SearchPoiSheetEventHand成功";
    private final Context context;
    //布局和View
    private final View searchRootLayout, infoRootLayout;
    //RecyclerView
    private final RecyclerView poiSearchRecyclerView;
    private PoiSearchAdapter poiSearchAdapter;

    public PoiSearchBottomSheetHelper(Context context, View searchRootView, View infoRootView) {
        this.context = context;
        this.searchRootLayout = searchRootView;
        this.infoRootLayout = infoRootView;
        //处理搜索框的搜索事件
        SearchView searchView = searchRootView.findViewById(R.id.searchView_poi);
        searchView.setIconifiedByDefault(true);

        searchView.setOnQueryTextListener(this);
        //为RecyclerView初始化
        poiSearchRecyclerView = searchRootLayout.findViewById(R.id.search_recycler_view);
        poiSearchRecyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    /**
     * 当点下搜索键后的响应处理
     *
     * @param query 输入的字符串
     * @return false表明用默认模式处理输入和显示
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        PoiSearch.Query poiQuery = new PoiSearch.Query(query, "", "济南");
        poiQuery.setPageSize(10);// 设置每页最多返回多少条poiItem
        poiQuery.setPageNum(1);//设置查询页码

        PoiSearchHelper poiSearchHelper = new PoiSearchHelper(context, poiQuery);
        poiSearchHelper.searchPOIAsyn();
        return false;
    }

    /**
     * 输入内容自动提示功能
     *
     * @param newText 输入的新字符
     * @return false表明用默认模式处理输入和显示
     */
    @SuppressLint("LongLogTag")
    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d(TAG, "onQueryTextChange: 收缩：" + newText);
        //当输入为空串或者为空时就尽可能使键盘和view收缩回去
        if (newText.equals("")) {
            //设置收缩
            searchPoiSheetBehaviour.setHideable(false);
            searchPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);

            viewPoiSheetBehaviour.setHideable(true);
            viewPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
            //如果有键盘，则隐藏
            InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(searchRootLayout.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }

        } else {
            //正常开始搜索，第二个参数传入null或者“”代表在全国进行检索，否则按照传入的city进行检索
            InputtipsQuery inputQuery = new InputtipsQuery(newText, "济南");
            inputQuery.setCityLimit(false);//限制在当前城市

            Inputtips inputTips = new Inputtips(context, inputQuery);
            inputTips.setInputtipsListener((list, i) -> {
                //解析返回的内容,i为返回状态码，i为1000时成功
                if (i == 1000) {
                    //为适配器初始化，由于list的值在变化，故每次需要重新初始化
                    poiSearchAdapter = new PoiSearchAdapter(context, list, infoRootLayout, searchPoiSheetBehaviour);
                    poiSearchRecyclerView.setAdapter(poiSearchAdapter);
                } else {
                    Log.w(TAG, "获取输入提示时出错，错误代码：" + i);
                }
            });
            inputTips.requestInputtipsAsyn();
        }
        return false;
    }

    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {

    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

    }
}
