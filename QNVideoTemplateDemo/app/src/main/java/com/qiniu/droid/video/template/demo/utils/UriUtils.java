package com.qiniu.droid.video.template.demo.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class UriUtils {

    private static final String TAG = "UriUtils";

    public static File copyToCacheDir(Context context, Uri uri) {
        InputStream is;
        try {
            is = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "copyToCacheDir, openInputStream fail", e);
            return null;
        }
        File file = new File(context.getCacheDir(), "" + System.currentTimeMillis());
        FileUtils.copyFile(is, file);
        return file;
    }

}
