package com.qiniu.droid.video.template.demo.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MediaStoreHelper {

    public static List<MediaStorageImage> queryImage(Context context, boolean isDesc) {
        List<MediaStorageImage> imageItemList = new ArrayList<>();
        String[] columns = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT
        };
        String order = null;
        if (isDesc) {
            order = MediaStore.Images.Media.DATE_MODIFIED + " desc";
        }
        try (Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, order)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                    Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                    int width = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.WIDTH));
                    int height = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT));
                    imageItemList.add(new MediaStorageImage(uri, displayName, width, height));
                }
            }
        }
        return imageItemList;
    }

    public static List<MediaStorageVideo> queryVideo(Context context, boolean isDesc) {
        List<MediaStorageVideo> videoItemList = new ArrayList<>();
        @SuppressLint("InlinedApi")
        String[] columns = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT
        };
        String order = null;
        if (isDesc) {
            order = MediaStore.Video.Media.DATE_MODIFIED + " desc";
        }
        try (Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns, null, null, order)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                    Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                    String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                    int width = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.WIDTH));
                    int height = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT));
                    @SuppressLint("InlinedApi")
                    long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                    videoItemList.add(new MediaStorageVideo(uri, displayName, width, height, duration));
                }
            }
        }
        return videoItemList;
    }

    public static Uri storeVideo(Context context, File srcFile, String mime) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            File dstFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), srcFile.getName());
            boolean succeed = FileUtils.copyFile(srcFile, dstFile);
            if (succeed) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.DATA, dstFile.getAbsolutePath());
                values.put(MediaStore.Video.Media.MIME_TYPE, mime);
                Uri uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(uri);
                context.sendBroadcast(intent);
                return Uri.fromFile(dstFile);
            }
        } else {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DISPLAY_NAME, srcFile.getName());
            values.put(MediaStore.Video.Media.MIME_TYPE, mime);
            values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());

            ContentResolver resolver = context.getContentResolver();
            Uri insertUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            if (insertUri != null) {
                try {
                    OutputStream outputStream = resolver.openOutputStream(insertUri);
                    boolean succeed = FileUtils.copyFile(srcFile, outputStream);
                    if (succeed) {
                        return insertUri;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

    public static Uri storeImage(Context context, File srcFile, String mime) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            File dstFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), srcFile.getName());
            boolean succeed = FileUtils.copyFile(srcFile, dstFile);
            if (succeed) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, dstFile.getAbsolutePath());
                values.put(MediaStore.Images.Media.MIME_TYPE, mime);
                Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(uri);
                context.sendBroadcast(intent);
                return Uri.fromFile(dstFile);
            }
        } else {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, srcFile.getName());
            values.put(MediaStore.Images.Media.MIME_TYPE, mime);
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

            ContentResolver resolver = context.getContentResolver();
            Uri insertUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (insertUri != null) {
                try {
                    OutputStream outputStream = resolver.openOutputStream(insertUri);
                    boolean succeed = FileUtils.copyFile(srcFile, outputStream);
                    if (succeed) {
                        return insertUri;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

    public static class MediaStorageBase implements Serializable {
        public final Uri uri;
        public final String displayName;

        public MediaStorageBase(Uri uri, String displayName) {
            this.uri = uri;
            this.displayName = displayName;
        }
    }

    public static class MediaStorageImage extends MediaStorageBase implements Serializable {
        public final int width;
        public final int height;

        public MediaStorageImage(Uri uri, String displayName, int width, int height) {
            super(uri, displayName);
            this.width = width;
            this.height = height;
        }
    }

    public static class MediaStorageVideo extends MediaStorageBase implements Serializable {
        public final int width;
        public final int height;
        public final long durationMs;

        public MediaStorageVideo(Uri uri, String displayName, int width, int height, long durationMs) {
            super(uri, displayName);
            this.width = width;
            this.height = height;
            this.durationMs = durationMs;
        }
    }
}
