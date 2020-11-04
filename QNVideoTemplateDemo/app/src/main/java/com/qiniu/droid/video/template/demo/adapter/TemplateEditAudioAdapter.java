package com.qiniu.droid.video.template.demo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qiniu.droid.video.template.demo.R;
import com.qiniu.droid.video.template.demo.holder.TemplateEditAudioHolder;
import com.qiniu.droid.video.template.demo.model.MusicSelectorItem;

import java.util.ArrayList;
import java.util.List;

public class TemplateEditAudioAdapter extends RecyclerView.Adapter<TemplateEditAudioHolder> {

    private final List<MusicSelectorItem> mAudioList;
    private OnItemClickListener mOnItemClickListener;
    public TemplateEditAudioAdapter() {
        mAudioList = new ArrayList<>();
    }

    public void setAudioList(List<MusicSelectorItem> audioList) {
        mAudioList.clear();
        if (!audioList.isEmpty()) {
            mAudioList.addAll(audioList);
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public TemplateEditAudioHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.template_holder_template_edit_audio, parent, false);
        return new TemplateEditAudioHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateEditAudioHolder holder, int position) {
        holder.bind(mAudioList.get(position));
        holder.setOnItemClickListener(mOnItemClickListener);
    }

    @Override
    public int getItemCount() {
        return mAudioList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(MusicSelectorItem asset);
    }

}
