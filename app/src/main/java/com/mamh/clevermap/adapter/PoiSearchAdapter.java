package com.mamh.clevermap.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.help.Tip;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mamh.clevermap.R;
import com.mamh.clevermap.listener.PoiSearchHelper;

import java.util.List;

public class PoiSearchAdapter extends
        RecyclerView.Adapter<PoiSearchAdapter.PoiSearchViewHolder> {

    private static final String TAG = "PoiSearchAdapter成功";
    private final List<Tip> queryTips;
    //inflater负责从item.xml中读取布局，并将其组织为RecyclerView
    private final LayoutInflater inflater;
    private final View poiViewRootLayout;
    final Context context;
    private View recyclerRootLayout;
    //控制BottomSheet的升降
    private BottomSheetBehavior viewPoiSheetBehaviour, searchPoiSheetBehaviour;

    /**
     * Adapter大类的构造方法，初始化时调用
     *
     * @param context                 当前的上下文
     * @param queryTips               传入的输入预测数据
     * @param poiInfoRootView         显示poi信息的bottomSheet布局
     * @param searchPoiSheetBehaviour 暂时可以忽略
     */
    public PoiSearchAdapter(Context context, List<Tip> queryTips, View poiInfoRootView,
                            BottomSheetBehavior searchPoiSheetBehaviour) {
        inflater = LayoutInflater.from(context);
        this.queryTips = queryTips;
        this.context = context;
        poiViewRootLayout = poiInfoRootView;

        //初始化BottomSheet
        if (poiViewRootLayout != null && searchPoiSheetBehaviour != null) {
            //给Behaviour设置初始化
            viewPoiSheetBehaviour = BottomSheetBehavior.from(poiViewRootLayout);
            this.searchPoiSheetBehaviour = searchPoiSheetBehaviour;
        }
    }

    @NonNull
    @Override
    public PoiSearchAdapter.PoiSearchViewHolder
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
    public void onBindViewHolder(@NonNull PoiSearchAdapter.PoiSearchViewHolder holder,
                                 int position) {
        String titleText = queryTips.get(position).getName();
        String locationText = queryTips.get(position).getDistrict();
        String addressText = queryTips.get(position).getAddress();

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
        return queryTips.size();
    }

    //适配器的内部类，它包含用于从item（单项目）布局中显示或暂存一个项目的必需的信息
    class PoiSearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleTextView, locationTextView, addressTextView;
        PoiSearchAdapter adapter;

        //该内部类的构造方法，这里一次进行初始化，避免每创建一个itemView就初始化一次的麻烦
        public PoiSearchViewHolder(@NonNull View itemView, PoiSearchAdapter adapter) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.recycler_item_title);
            locationTextView = itemView.findViewById(R.id.recycler_item_location);
            addressTextView = itemView.findViewById(R.id.recycler_item_address);
            this.adapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(context, "click", Toast.LENGTH_SHORT).show();
            try {
                int position = getLayoutPosition();
                String tipsId = queryTips.get(position).getPoiID();
                //设置参数并开始搜索
                PoiSearchHelper poiSearchHelper = new PoiSearchHelper(context, null, poiViewRootLayout);
                // 异步搜索
                poiSearchHelper.searchPOIIdAsyn(tipsId);
                //将当前的搜索面板设为不可见，查看面板设为可见并弹出
                searchPoiSheetBehaviour.setHideable(true);
                searchPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);

                viewPoiSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
                //隐藏键盘
                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputManager != null) {
                    inputManager.hideSoftInputFromWindow(recyclerRootLayout.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
            } catch (NullPointerException nullPointerException) {
                nullPointerException.printStackTrace();
                Log.e(TAG, "onClick: 搜索提示poi时点击出现空指针异常");
            } catch (Exception e) {
                Log.e(TAG, "onClick: 搜索提示poi时点击出现异常");
            }
        }
    }
}
