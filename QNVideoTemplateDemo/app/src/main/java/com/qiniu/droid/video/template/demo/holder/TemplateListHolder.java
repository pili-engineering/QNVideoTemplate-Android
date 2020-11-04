package com.qiniu.droid.video.template.demo.holder;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;
import com.bumptech.glide.request.RequestOptions;
import com.qiniu.droid.video.template.demo.R;
import com.qiniu.droid.video.template.demo.adapter.TemplateListAdapter;
import com.qiniu.droid.video.template.demo.model.Template;

import java.security.MessageDigest;

public class TemplateListHolder extends RecyclerView.ViewHolder {

    private final ImageView mTemplateCover;
    private TemplateListAdapter.OnItemClickListener mListener;

    private Template mTemplate;

    public TemplateListHolder(@NonNull View itemView) {
        super(itemView);
        mTemplateCover = itemView.findViewById(R.id.iv_template_cover);
        mTemplateCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onItemClick(mTemplate);
                }
            }
        });
    }

    public void bind(Template template) {
        mTemplate = template;
        updateCoverSize();
        Glide.with(itemView).load(mTemplate.getCover())
                .apply(RequestOptions.bitmapTransform(new BitmapTransformation() {
                    @Override
                    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
                        Bitmap bitmap = TransformationUtils.centerCrop(pool, toTransform, outWidth, outHeight);
                        return TransformationUtils.roundedCorners(pool, bitmap, 16);
                    }

                    @Override
                    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
                    }
                }))
                .into(mTemplateCover);
    }

    public void setOnItemClickListener(TemplateListAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    private void updateCoverSize() {
        ViewGroup.LayoutParams layoutParams = mTemplateCover.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        }
        int width = mTemplateCover.getWidth();
        if (width == 0) {
            width = mTemplateCover.getResources().getDisplayMetrics().widthPixels / 2 - itemView.getPaddingLeft() - itemView.getPaddingRight() - 24 * 2;
        }
        int height = (int) (mTemplate.getHeight() * (width * 1.0 / mTemplate.getWidth()));
        layoutParams.width = width;
        layoutParams.height = height;
        mTemplateCover.setLayoutParams(layoutParams);
    }
}
