package com.qiniu.droid.video.template.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qiniu.droid.video.template.demo.R;

public class TitleBar extends FrameLayout {

    private ImageView mIvLeft;
    private TextView mTvCenter;
    private TextView mTvRight;

    public TitleBar(@NonNull Context context) {
        this(context, null);
    }

    public TitleBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.template_v_title_bar, this);
        mIvLeft = findViewById(R.id.iv_left);
        mTvCenter = findViewById(R.id.tv_center);
        mTvRight = findViewById(R.id.tv_right);
    }

    public void showCenterOnly() {
        mIvLeft.setVisibility(GONE);
        mTvRight.setVisibility(GONE);
    }

    public void setTitle(String text) {
        mTvCenter.setText(text);
    }

    public void setLeftIcon(@DrawableRes int iconRes) {
        mIvLeft.setImageResource(iconRes);
    }

    public void setLeftListener(OnClickListener clickListener) {
        mIvLeft.setOnClickListener(clickListener);
    }

    public void setRightListener(String text, OnClickListener onClickListener) {
        mTvRight.setText(text);
        mTvRight.setOnClickListener(onClickListener);
    }
}
