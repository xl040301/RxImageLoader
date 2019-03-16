package com.uniscope.rximageloader.bean;

import android.graphics.Bitmap;

/**
 * 作者：majun
 * 时间：2019/3/15 15:46
 * 说明：
 */
public class ImageBean {
    private String url;
    private Bitmap bitmap;

    public ImageBean(String url, Bitmap bitmap) {
        this.url = url;
        this.bitmap = bitmap;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
