package com.mamh.clevermap.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Tip;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.mamh.clevermap.R;
import com.mamh.clevermap.listener.main.PoiSearchHelper;

import java.util.ArrayList;
import java.util.List;

import static com.mamh.clevermap.activity.MainActivity.mapView;
import static com.mamh.clevermap.activity.MainActivity.searchPoiSheetBehaviour;
import static com.mamh.clevermap.activity.MainActivity.viewPoiSheetBehaviour;
import static com.mamh.clevermap.listener.main.PoiViewBottomSheetHelper.pullUpFlag;

public class PoiSearchLayoutAdapter extends
        RecyclerView.Adapter<PoiSearchLayoutAdapter.PoiSearchViewHolder> {

    private static final String TAG = "PoiSearchAdapter成功";
    private static final int POI_ITEM = 1;
    private static final int POI_TIP = 0;
    private static int status = 0;
    private final Context context;
    //输入提示
    private List<Tip> queryTips;
    //inflater负责从item.xml中读取布局，并将其组织为RecyclerView
    private final LayoutInflater inflater;
    private final View poiViewRootLayout;
    //按下搜索后的提示
    private ArrayList<PoiItem> poiSets;
    private View recyclerRootLayout;


    /**
     * Adapter大类的搜索提示的构造方法，在poiSearchBottomSheet中调用
     *
     * @param context         当前的上下文
     * @param queryTips       传入的输入预测数据
     * @param poiInfoRootView 显示poi信息的bottomSheet布局
     */
    public PoiSearchLayoutAdapter(Context context, List<Tip> queryTips, View poiInfoRootView) {
        inflater = LayoutInflater.from(context);
        this.queryTips = queryTips;
        this.context = context;
        poiViewRootLayout = poiInfoRootView;
        status = POI_TIP;
    }

    /**
     * Adapter大类的查看搜索结果的构造方法，当返回搜索结果时调用
     *
     * @param context  当前的上下文
     * @param poiSets  传入的搜索结果数据
     * @param rootView 显示poi信息的bottomSheet布局，就是recyclerview的根布局
     */
    public PoiSearchLayoutAdapter(Context context, ArrayList<PoiItem> poiSets, View rootView) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.poiSets = poiSets;
        poiViewRootLayout = rootView;
        status = POI_ITEM;
    }

    @NonNull
    @Override
    public PoiSearchLayoutAdapter.PoiSearchViewHolder
    onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        recyclerRootLayout = inflater.inflate(R.layout.search_recycler_item,
                parent, false);
        return new PoiSearchViewHolder(recyclerRootLayout, this);
    }

    /**
     * 使用holder中的数据加载item视图
     *
     * @param holder   内部类holder的实例
     * @param position 当前元素的位置，也对应着应当加载的数据在线性表中的位置
     */
    @Override
    public void onBindViewHolder(@NonNull PoiSearchLayoutAdapter.PoiSearchViewHolder holder,
                                 int position) {
        //当recyclerView运作在搜索提示模式时，调用这个，反之调用另一个加载
        String titleText = "";
        String locationText = "";
        String addressText = "";
        if (status == POI_TIP) {
            titleText = queryTips.get(position).getName();
            locationText = queryTips.get(position).getDistrict();
            addressText = queryTips.get(position).getAddress();

        } else {
            try {
                titleText = poiSets.get(position).getTitle();
                //第二个TextView设为省+市，不精确到区
                locationText = poiSets.get(position).getProvinceName()
                        + poiSets.get(position).getCityName();
                addressText = poiSets.get(position).getSnippet();

            } catch (NullPointerException pointerException) {
                Snackbar.make(mapView, "加载搜索结果出现空指针异常", Snackbar.LENGTH_SHORT).show();
                Log.e(TAG, "onBindViewHolder: 加载搜索结果的recyclerVew On Bind View Holder 出现空指针异常");
                pointerException.printStackTrace();
            }
        }
        holder.titleTextView.setText(titleText);
        holder.locationTextView.setText(locationText);
        holder.addressTextView.setText(addressText);
    }

    /**
     * 确定RecyclerView中Item的数量，由系统调用
     *
     * @return 即返回的线性表的长度
     */
    @Override
    public int getItemCount() {
        if (status == POI_TIP) {
            return queryTips.size();
        } else if (status == POI_ITEM) {
            return poiSets.size();
        } else {
            return 0;
        }
    }

    //适配器的内部类，它包含用于从item（单项目）布局中显示或暂存一个项目的必需的信息
    class PoiSearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleTextView, locationTextView, addressTextView;
        PoiSearchLayoutAdapter adapter;

        //该内部类的构造方法，这里一次进行初始化，避免每创建一个itemView就初始化一次的麻烦
        public PoiSearchViewHolder(@NonNull View itemView, PoiSearchLayoutAdapter adapter) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.recycler_item_title);
            locationTextView = itemView.findViewById(R.id.recycler_item_location);
            addressTextView = itemView.findViewById(R.id.recycler_item_address);
            this.adapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            try {
                //隐藏键盘
                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputManager != null) {
                    inputManager.hideSoftInputFromWindow(recyclerRootLayout.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
                //将当前的搜索面板设为不可见，查看面板设为可见并弹出
                searchPoiSheetBehaviour.setHideable(true);
                searchPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);

                viewPoiSheetBehaviour.setHideable(false);
                viewPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
                //获得当前布局item的id
                int position = getLayoutPosition();
                String tipsId;
                if (status == POI_TIP) {
                    tipsId = queryTips.get(position).getPoiID();
                } else {
                    tipsId = poiSets.get(position).getPoiId();
                }
                //设置参数并开始搜索
                PoiSearchHelper poiSearchHelper = new PoiSearchHelper(context, null, poiViewRootLayout);
                // 异步搜索
                poiSearchHelper.searchPOIIdAsyn(tipsId);

                //将flag设为true，代表由搜索起动卡片，但是不用重新搜索
                pullUpFlag = true;
            } catch (NullPointerException nullPointerException) {
                nullPointerException.printStackTrace();
                Log.e(TAG, "onClick: 搜索提示poi时点击出现空指针异常");
            } catch (Exception e) {
                Log.e(TAG, "onClick: 搜索提示poi时点击出现异常");
            }
        }

    }
}
