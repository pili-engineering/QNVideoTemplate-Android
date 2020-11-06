package com.qiniu.droid.video.template.demo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qiniu.droid.video.template.demo.R;
import com.qiniu.droid.video.template.demo.holder.TemplateListHolder;
import com.qiniu.droid.video.template.demo.model.Template;

import java.util.List;

public class TemplateListAdapter extends RecyclerView.Adapter<TemplateListHolder> {

    private final List<Template> mTemplateList;
    private OnItemClickListener mOnItemClickListener;
    public TemplateListAdapter(List<Template> templateList) {
        mTemplateList = templateList;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public TemplateListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.template_holder_template_list, parent, false);
        return new TemplateListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateListHolder holder, int position) {
        holder.bind(mTemplateList.get(position));
        holder.setOnItemClickListener(mOnItemClickListener);
    }

    @Override
    public int getItemCount() {
        return mTemplateList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Template template);
    }

}
