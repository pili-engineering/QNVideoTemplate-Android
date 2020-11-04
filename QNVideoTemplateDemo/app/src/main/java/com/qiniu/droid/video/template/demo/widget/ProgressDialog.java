package com.qiniu.droid.video.template.demo.widget;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.qiniu.droid.video.template.demo.R;

public class ProgressDialog extends DialogFragment {

    private TextView mTextView;
    private int mProgress;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.template_dialog_progress, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true);
        builder.setView(view);
        final AlertDialog dialog = builder.create();

        mTextView = view.findViewById(R.id.tv_progress);

        return dialog;
    }

    public void setProgress(int progress) {
        mProgress = progress;
        if (mTextView != null) {
            mTextView.post(new Runnable() {
                @Override
                public void run() {
                    mTextView.setText(mProgress + "%");
                }
            });
        }
    }

}
