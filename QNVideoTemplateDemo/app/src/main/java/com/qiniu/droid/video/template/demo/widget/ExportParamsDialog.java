package com.qiniu.droid.video.template.demo.widget;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.qiniu.droid.video.template.demo.R;

public class ExportParamsDialog extends DialogFragment {

    private OnClickListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.template_dialog_output_params, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true);
        builder.setView(view);
        final AlertDialog dialog = builder.create();

        final EditText etWidth = view.findViewById(R.id.et_width);
        final EditText etHeight = view.findViewById(R.id.et_height);
        final EditText etBitrate = view.findViewById(R.id.et_bitrate);
        final Button btnExport = view.findViewById(R.id.btn_export);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (mListener != null) {
                    int width = Integer.parseInt(etWidth.getText().toString());
                    int height = Integer.parseInt(etHeight.getText().toString());
                    int bitrate = Integer.parseInt(etBitrate.getText().toString());
                    mListener.onClick(width, height, bitrate);
                }
            }
        });
        return dialog;
    }

    public void setOnClickListener(OnClickListener listener) {
        mListener = listener;
    }

    public interface OnClickListener {
        void onClick(int width, int height, int bitrate);
    }
}
