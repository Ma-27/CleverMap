package com.mamh.clevermap.listener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiSearch;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mamh.clevermap.R;
import com.mamh.clevermap.adapter.PoiSearchAdapter;

import java.util.List;

/**
 * 该类用于响应搜索poi的bottomSheet
 */
public class PoiSearchBottomSheetHandler
        extends BottomSheetBehavior.BottomSheetCallback {
    private static final String TAG = "SearchPoiSheetEventHand成功";
    private final Context context;
    //布局和View
    private final View searchRootLayout, infoRootLayout;
    private final SearchView searchView;
    //RecyclerView
    private final RecyclerView poiSearchRecyclerView;
    private final BottomSheetBehavior viewPoiSheetBehaviour;
    private final BottomSheetBehavior searchPoiSheetBehaviour;
    private final List<Tip> queryTips = null;
    private PoiSearch.Query poiQuery;
    private PoiSearchAdapter poiSearchAdapter;

    public PoiSearchBottomSheetHandler(Context context, View searchRootView, View infoRootView) {
        this.context = context;
        this.searchRootLayout = searchRootView;
        this.infoRootLayout = infoRootView;
        //处理搜索框的搜索事件
        searchView = searchRootView.findViewById(R.id.searchView_poi);
        searchView.setIconifiedByDefault(true);
        viewPoiSheetBehaviour = BottomSheetBehavior.from(infoRootLayout);
        searchPoiSheetBehaviour = BottomSheetBehavior.from(searchRootLayout);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * 当点下搜索键后的响应处理
             * @param query 输入的字符串
             * @return false表明用默认模式处理输入和显示
             */
            @SuppressLint("LongLogTag")
            @Override
            public boolean onQueryTextSubmit(String query) {
                poiQuery = new PoiSearch.Query(query, "", "济南");
                poiQuery.setPageSize(10);// 设置每页最多返回多少条poiitem
                poiQuery.setPageNum(1);//设置查询页码

                PoiSearchHelper poiSearchHelper = new PoiSearchHelper(context, poiQuery);
                poiSearchHelper.searchPOIAsyn();
                return false;
            }

            /**
             * 输入内容自动提示功能
             * @param newText 输入的新字符
             * @return false表明用默认模式处理输入和显示
             */
            @SuppressLint("LongLogTag")
            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: 收缩：" + newText);
                //看看无字时尽可能使键盘和view收缩回去
                if (newText != "" || newText != null) {
                    //第二个参数传入null或者“”代表在全国进行检索，否则按照传入的city进行检索
                    InputtipsQuery inputQuery = new InputtipsQuery(newText, "济南");
                    inputQuery.setCityLimit(true);//限制在当前城市

                    Inputtips inputTips = new Inputtips(context, inputQuery);
                    inputTips.setInputtipsListener(new Inputtips.InputtipsListener() {
                        @SuppressLint("LongLogTag")
                        @Override
                        public void onGetInputtips(List<Tip> list, int i) {
                            //解析返回的内容,i为返回状态码，i为1000时成功
                            if (i == 1000) {
                                //为适配器初始化，由于list的值在变化，故每次需要重新初始化
                                poiSearchAdapter = new PoiSearchAdapter(context, list, infoRootLayout, searchPoiSheetBehaviour);
                                poiSearchRecyclerView.setAdapter(poiSearchAdapter);
                            } else {
                                Log.w(TAG, "获取输入提示时出错，错误代码：" + i);
                            }
                        }
                    });
                    inputTips.requestInputtipsAsyn();
                } else {
                    //设置收缩
                    searchPoiSheetBehaviour.setHideable(false);
                    searchPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    viewPoiSheetBehaviour.setHideable(true);
                    viewPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
                    //如果有键盘，则隐藏
                    //隐藏键盘
                    InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputManager != null) {
                        inputManager.hideSoftInputFromWindow(searchRootView.getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }
                return false;
            }
        });
        //为RecyclerView初始化
        poiSearchRecyclerView = searchRootLayout.findViewById(R.id.search_recycler_view);
        poiSearchRecyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {

    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

    }
}
