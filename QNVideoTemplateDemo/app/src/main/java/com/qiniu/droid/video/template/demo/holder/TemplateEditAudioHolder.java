package com.qiniu.droid.video.template.demo.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.qiniu.droid.video.template.demo.R;
import com.qiniu.droid.video.template.demo.adapter.TemplateEditAudioAdapter;
import com.qiniu.droid.video.template.demo.model.MusicSelectorItem;

public class TemplateEditAudioHolder extends RecyclerView.ViewHolder {

    private final ImageView mIvAudioCover;
    private final ImageView mIvAudioCheckMask;
    private final TextView mTvAudioName;

    private TemplateEditAudioAdapter.OnItemClickListener mListener;

    public TemplateEditAudioHolder(@NonNull View itemView) {
        super(itemView);
        mIvAudioCover = itemView.findViewById(R.id.iv_audio_cover);
        mIvAudioCheckMask = itemView.findViewById(R.id.iv_audio_check_mask);
        mTvAudioName = itemView.findViewById(R.id.tv_audio_name);
    }

    public void bind(final MusicSelectorItem item) {

        if (item.selected) {
            mIvAudioCheckMask.setVisibility(View.VISIBLE);
        } else {
            mIvAudioCheckMask.setVisibility(View.INVISIBLE);
        }

        mTvAudioName.setText(item.musicName);
        if (item.musicCover == null) {
            Glide.with(itemView).load(item.musicCoverResId).circleCrop().into(mIvAudioCover);
        } else {
            Glide.with(itemView).load(item.musicCover).circleCrop().into(mIvAudioCover);
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onItemClick(item);
                }
            }
        });
    }

    public void setOnItemClickListener(TemplateEditAudioAdapter.OnItemClickListener onItemClickListener) {
        mListener = onItemClickListener;
    }
}
