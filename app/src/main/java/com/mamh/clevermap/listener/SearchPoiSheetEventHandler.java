package com.mamh.clevermap.listener;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mamh.clevermap.interfaces.OnPoiChange;

public class SearchPoiSheetEventHandler extends BottomSheetBehavior.BottomSheetCallback
        implements OnPoiChange {
    private final Context context;
    //布局和View
    private final View rootLayout;

    public SearchPoiSheetEventHandler(Context context, View rootView) {
        this.context = context;
        this.rootLayout = rootView;
    }

    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {

    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

    }

    @Override
    public void updatePOIText() {

    }
}
