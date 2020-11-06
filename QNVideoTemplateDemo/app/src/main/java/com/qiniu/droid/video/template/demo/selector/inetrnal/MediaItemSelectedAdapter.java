package com.qiniu.droid.video.template.demo.selector.inetrnal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qiniu.droid.video.template.demo.R;

import java.util.ArrayList;
import java.util.List;

public class MediaItemSelectedAdapter extends RecyclerView.Adapter<MediaItemSelectedHolder> {

    private final List<MediaSelectorItem> mSelectorItemList = new ArrayList<>();
    private OnItemClickListener mListener;

    public void setListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void setItemList(List<MediaSelectorItem> mediaItemList) {
        mSelectorItemList.clear();
        mSelectorItemList.addAll(mediaItemList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MediaItemSelectedHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.template_holder_media_item_selected, null);
        return new MediaItemSelectedHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaItemSelectedHolder holder, int position) {
        holder.bind(mSelectorItemList.get(position));
        holder.setOnClickListener(mListener);
    }

    @Override
    public int getItemCount() {
        return mSelectorItemList.size();
    }
}
