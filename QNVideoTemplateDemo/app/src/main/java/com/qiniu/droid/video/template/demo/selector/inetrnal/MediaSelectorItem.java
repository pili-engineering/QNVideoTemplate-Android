package com.qiniu.droid.video.template.demo.selector.inetrnal;

import com.qiniu.droid.video.template.demo.utils.MediaStoreHelper;

import java.io.Serializable;

public class MediaSelectorItem implements Serializable {

    public int position;
    public boolean enableSelection;
    public boolean selected;
    public MediaStoreHelper.MediaStorageBase item;

    public MediaSelectorItem(MediaStoreHelper.MediaStorageBase item, boolean enableSelection) {
        this.selected = false;
        this.item = item;
        this.enableSelection = enableSelection;
    }
}
