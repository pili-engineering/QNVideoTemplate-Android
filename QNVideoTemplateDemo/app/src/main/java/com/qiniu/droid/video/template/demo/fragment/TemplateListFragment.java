package com.qiniu.droid.video.template.demo.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.qiniu.droid.video.template.demo.R;
import com.qiniu.droid.video.template.demo.acitvity.IndexActivity;
import com.qiniu.droid.video.template.demo.adapter.TemplateListAdapter;
import com.qiniu.droid.video.template.demo.model.Template;
import com.qiniu.droid.video.template.demo.utils.TemplateHelper;
import com.qiniu.droid.video.template.demo.widget.TitleBar;

import static androidx.recyclerview.widget.StaggeredGridLayoutManager.GAP_HANDLING_NONE;

public class TemplateListFragment extends Fragment {

    private TitleBar mTitleBar;
    private RecyclerView mRVTemplate;
    private TemplateListAdapter mTemplateListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.template_fragment_template_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTitleBar = view.findViewById(R.id.title_bar);
        mRVTemplate = view.findViewById(R.id.rv_template);

        mTitleBar.showCenterOnly();
        mTitleBar.setTitle("模板库");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // check permission
        if (!checkPermissionAndPrepareTemplate()) {
            return;
        }

        updateTemplateUI();
    }

    private void updateTemplateUI() {
        mTemplateListAdapter = new TemplateListAdapter(TemplateHelper.getTemplateList(getContext()));
        mTemplateListAdapter.setOnItemClickListener(new TemplateListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Template template) {
                ((IndexActivity) getActivity()).detail(template);
            }
        });
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(GAP_HANDLING_NONE);
        mRVTemplate.setLayoutManager(layoutManager);
        mRVTemplate.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int spanIndex = ((StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams()).getSpanIndex();
                if (spanIndex == 0) {
                    outRect.set(24, 12, 12, 24);
                } else {
                    outRect.set(12, 12, 24, 24);
                }
            }
        });
        mRVTemplate.setAdapter(mTemplateListAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            for (int ret : grantResults) {
                if (ret != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "template files prepared fail, permission denied", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            prepareTemplate();
        }
    }

    private boolean checkPermissionAndPrepareTemplate() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                return false;
            } else {
                if (!TemplateHelper.isTemplateReady(getActivity())) {
                    prepareTemplate();
                    return false;
                }
            }
        } else {
            if (!TemplateHelper.isTemplateReady(getActivity())) {
                prepareTemplate();
                return false;
            }
        }
        return true;
    }

    private void prepareTemplate() {
        new Thread() {
            @Override
            public void run() {
                Context context = getContext();
                if (TemplateHelper.copyTemplateToAppFileDir(context)) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateTemplateUI();
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "template files copy fail", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }.start();
    }
}
