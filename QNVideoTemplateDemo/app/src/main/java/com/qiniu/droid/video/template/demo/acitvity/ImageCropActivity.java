package com.qiniu.droid.video.template.demo.acitvity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qiniu.droid.video.template.demo.R;
import com.qiniu.droid.video.template.demo.widget.ImageCropViewFixArea;
import com.qiniu.droid.video.template.demo.widget.TitleBar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class ImageCropActivity extends AppCompatActivity {

    public static final String K_IMAGE_CROP_URI = "K_IMAGE_CROP_URI";
    public static final String K_IMAGE_CROP_WIDTH = "K_IMAGE_CROP_WIDTH";
    public static final String K_IMAGE_CROP_HEIGHT = "K_IMAGE_CROP_HEIGHT";

    public static final String K_IMAGE_CROP_RESULT_PATH = "K_IMAGE_CROP_RESULT_PATH";

    private TitleBar mTitleBar;
    private ImageCropViewFixArea mImageCropView;

    private String mSrcUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.template_activity_image_crop);

        mTitleBar = findViewById(R.id.title_bar);
        mImageCropView = findViewById(R.id.image_crop_view);

        mTitleBar.setTitle("图片剪裁");
        mTitleBar.setLeftIcon(R.drawable.template_ic_clear_light);
        mTitleBar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTitleBar.setRightListener("完成", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageCropViewFixArea.CropResult cropResult = mImageCropView.getCropResult();

                String dstPath = getCacheDir().getAbsolutePath() + "/crop_image/" + System.currentTimeMillis() + ".png";
                try {
                    if (cropImage(mSrcUri, dstPath, cropResult.full, cropResult.crop)) {
                        Intent intent = new Intent();
                        intent.putExtra(K_IMAGE_CROP_RESULT_PATH, dstPath);
                        setResult(RESULT_OK, intent);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                finish();
            }
        });

        mSrcUri = getIntent().getStringExtra(K_IMAGE_CROP_URI);
        int width = getIntent().getIntExtra(K_IMAGE_CROP_WIDTH, 1);
        int height = getIntent().getIntExtra(K_IMAGE_CROP_HEIGHT, 1);
        mImageCropView.setImageUri(mSrcUri, width / 1f / height);
    }

    private boolean cropImage(String srcUri, String dstPath, Rect src, Rect dst) throws FileNotFoundException {
        long start = System.currentTimeMillis();
        Bitmap bitmap = Bitmap.createBitmap(dst.width(), dst.height(), Bitmap.Config.ARGB_8888);
        Bitmap srcBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(srcUri)));
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(srcBitmap, dst, new Rect(0, 0, dst.width(), dst.height()), new Paint());
        File dstFile = new File(dstPath);
        if (!dstFile.getParentFile().exists()) {
            dstFile.getParentFile().mkdirs();
        }
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(dstFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            bitmap.recycle();
        }
        return false;
    }
}
