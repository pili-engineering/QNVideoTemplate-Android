package com.qiniu.droid.video.template.demo.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.qiniu.droid.video.template.QNVTAssetProperty;
import com.qiniu.droid.video.template.QNVTTemplateEditor;
import com.qiniu.droid.video.template.demo.Config;
import com.qiniu.droid.video.template.demo.model.MusicSelectorItem;
import com.qiniu.droid.video.template.demo.model.RelatedAssetGroup;
import com.qiniu.droid.video.template.demo.model.Template;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TemplateHelper {
    public static boolean isTemplateReady(Context context) {
        SharedPreferences sp = context.getSharedPreferences("", Context.MODE_PRIVATE);
        return sp.getBoolean(Config.TEMPLATE_READY_STATE, false);
    }

    public static boolean copyTemplateToAppFileDir(Context context) {
        String appDirPath = context.getCacheDir().getAbsolutePath();
        try {
            // copy template
            boolean result = copyFilesFromAssets(context, Config.TEMPLATE_PREFIX_IN_ASSET, appDirPath);
            if (!result) {
                return false;
            }
            // copy music
            result = copyFilesFromAssets(context, Config.MUSIC_PREFIX_IN_ASSET, appDirPath);
            SharedPreferences sp = context.getSharedPreferences("", Context.MODE_PRIVATE);
            sp.edit().putBoolean(Config.TEMPLATE_READY_STATE, result).apply();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<RelatedAssetGroup> getAssetGroup(QNVTTemplateEditor player) {
        QNVTAssetProperty[] replaceableAssets = player.getReplaceableAssetList();
        List<RelatedAssetGroup> groupList = new ArrayList<>();
        for (QNVTAssetProperty asset : replaceableAssets) {
            // we recommend only replace asset which it's name is started with "rpl_"
            if (!asset.name.startsWith("rpl_")) {
                continue;
            }
            RelatedAssetGroup exists = null;
            for (RelatedAssetGroup g : groupList) {
                if (g.assetType == asset.type) {
                    if (asset.type == QNVTAssetProperty.TYPE_TEXT) {
                        if (asset.text.equals(g.assetList.get(0).text)) {
                            exists = g;
                            break;
                        }
                    } else if (asset.type == QNVTAssetProperty.TYPE_IMAGE || asset.type == QNVTAssetProperty.TYPE_VIDEO) {
                        if (asset.path.equals(g.assetList.get(0).path)) {
                            exists = g;
                            break;
                        }
                    }
                }
            }
            if (exists == null) {
                exists = new RelatedAssetGroup();
                exists.assetType = asset.type;
                groupList.add(exists);
            }
            exists.assetList.add(asset);
        }
        // sort for every group
        for (RelatedAssetGroup group : groupList) {
            Collections.sort(group.assetList, new Comparator<QNVTAssetProperty>() {
                @Override
                public int compare(QNVTAssetProperty o1, QNVTAssetProperty o2) {
                    if (o1.in > o2.in) {
                        return 1;
                    } else if (o1.in < o2.in) {
                        return -1;
                    }
                    return 0;
                }
            });
            group.firstInPoint = (group.assetList.get(0).in) * 1f / player.fps();
        }
        Collections.sort(groupList, new Comparator<RelatedAssetGroup>() {
            @Override
            public int compare(RelatedAssetGroup o1, RelatedAssetGroup o2) {
                if (o1.firstInPoint > o2.firstInPoint) {
                    return 1;
                } else if (o1.firstInPoint < o2.firstInPoint) {
                    return -1;
                }
                return 0;
            }
        });
        return groupList;
    }

    public static List<MusicSelectorItem> getMusicList(Context context) {
        String musicRootDirPath = context.getCacheDir().getAbsolutePath() + "/" + Config.MUSIC_PREFIX_IN_ASSET;
        File musicRootDir = new File(musicRootDirPath);
        File[] musicFiles = musicRootDir.listFiles();
        List<MusicSelectorItem> musicList = new ArrayList<>();
        if (musicFiles == null) {
            return musicList;
        }
        for (File musicFile : musicFiles) {
            File configFile = new File(musicFile, "config.json");
            if (configFile.exists()) {
                try {
                    FileReader fileReader = new FileReader(configFile);
                    char[] content = new char[(int) configFile.length()];
                    fileReader.read(content);
                    String jsonStr = new String(content);
                    JSONObject json = new JSONObject(jsonStr);
                    String parent = musicFile.getAbsolutePath();
                    String name = json.getString("name");
                    String path = parent + File.separator + json.getString("path");
                    String icon = parent + File.separator + json.getString("icon");
                    musicList.add(new MusicSelectorItem(name, path, icon, 0));
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return musicList;
    }

    public static List<Template> getTemplateList(Context context) {
        String templateRootDirPath = context.getCacheDir().getAbsolutePath() + "/" + Config.TEMPLATE_PREFIX_IN_ASSET;
        File[] templateFiles = new File(templateRootDirPath).listFiles();
        List<Template> templateList = new ArrayList<>();
        if (templateFiles == null) {
            return templateList;
        }
        for (File templateFile : templateFiles) {
            File configFile = new File(templateFile, "config.json");
            if (configFile.exists()) {
                try {
                    FileReader fileReader = new FileReader(configFile);
                    char[] content = new char[(int) configFile.length()];
                    fileReader.read(content);
                    String jsonStr = new String(content);
                    JSONObject json = new JSONObject(jsonStr);
                    String name = json.getString("name");
                    int width = json.getInt("width");
                    int height = json.getInt("height");
                    String cover = "";
                    File coverFile = new File(templateFile, "cover.png");
                    if (coverFile.exists()) {
                        cover = coverFile.getAbsolutePath();
                    } else {
                        coverFile = new File(templateFile, "cover.jpg");
                        if (coverFile.exists()) {
                            cover = coverFile.getAbsolutePath();
                        }
                    }
                    String demoPath = "";
                    File previewFile = new File(templateFile, "demo.mp4");
                    if (previewFile.exists()) {
                        demoPath = previewFile.getAbsolutePath();
                    }
                    templateList.add(new Template(name, "", templateFile.getAbsolutePath(), width, height, cover, demoPath));
                    continue;
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
            templateList.add(new Template(templateFile.getName(), "", templateFile.getAbsolutePath(), 500, 500, "", ""));
        }
        return templateList;
    }

    private static boolean copyFilesFromAssets(Context context, String srcAssetsPath, String dstRootDir) throws IOException {
        String[] srcAssetChildren = context.getAssets().list(srcAssetsPath);
        if (srcAssetChildren != null && srcAssetChildren.length > 0) {
            File dst = new File(dstRootDir + "/" + srcAssetsPath);
            if (dst.isFile()) {
                throw new IOException("src is dir but dst is file");
            }
            if (!dst.exists()) {
                dst.mkdirs();
            }
            for (String srcChild : srcAssetChildren) {
                boolean result = copyFilesFromAssets(context, srcAssetsPath + "/" + srcChild, dstRootDir);
                if (!result) {
                    return false;
                }
            }
        } else {
            InputStream is = context.getAssets().open(srcAssetsPath);
            OutputStream os = new BufferedOutputStream(new FileOutputStream(new File(dstRootDir + "/" + srcAssetsPath)));
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                os.write(buffer, 0, byteCount);
            }
            os.flush();
            is.close();
            os.close();
        }
        return true;
    }
}
