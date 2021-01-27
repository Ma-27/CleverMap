package com.mamh.clevermap.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.mamh.clevermap.R;

/**
 * 监听选择地图类型的Fragment
 */
public class ChooseMapTypeDialogFragment extends DialogFragment {
    public static final int CHOOSE_MAP_DEFAULT = 1;
    public static final int CHOOSE_MAP_SATELLITE = 2;
    public static final int CHOOSE_MAP_NAVIGATION = 3;
    public static final int CHOOSE_MAP_NIGHT = 4;

    public ChooseMapTypeDialogFragment() {
        // Required empty public constructor
    }

    /**
     * 使用此工厂方法创建的新实例
     * 该片段使用提供的参数。
     *
     * @return 一个ChooseMapTypeDialogFragment片段的新实例。
     */
    public static ChooseMapTypeDialogFragment newInstance() {
        ChooseMapTypeDialogFragment fragment = new ChooseMapTypeDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        builder.setTitle("请选择地图类型：");
        //设置确定对话框按钮
        builder.setPositiveButton("确定", (dialog, which) -> {

        });
        //设置取消对话框按钮
        builder.setNegativeButton("取消", (dialog, which) -> {

        });
        builder.setView(inflater.inflate(R.layout.dialog_choose_map, null));

        return builder.create();
    }
}