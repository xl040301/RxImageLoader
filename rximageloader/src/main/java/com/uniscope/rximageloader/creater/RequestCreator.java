package com.uniscope.rximageloader.creater;

import android.content.Context;

import com.uniscope.rximageloader.bean.ImageBean;
import com.uniscope.rximageloader.cache.DiskCacheUtils;
import com.uniscope.rximageloader.cache.MemoryCacheUtils;
import com.uniscope.rximageloader.cache.NetworkCacheUtils;
import com.uniscope.rximageloader.utils.BitmapUtils;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;


/**
 * 作者：majun
 * 时间：2019/3/15 17:03
 * 说明：
 */
public class RequestCreator {

    private MemoryCacheUtils memoryCacheUtils;
    private DiskCacheUtils diskCacheUtils;
    private NetworkCacheUtils networkCacheUtils;

    //图片是否压缩处理
    private boolean isCompress = false;

    public RequestCreator(Context context) {
        memoryCacheUtils = new MemoryCacheUtils();
        diskCacheUtils = new DiskCacheUtils(context,"");
        networkCacheUtils = new NetworkCacheUtils();
    }

    public Observable<ImageBean> getImageFromMemory(String url) {
        return memoryCacheUtils.getImage(url)
                .filter(new Predicate<ImageBean>() {
                    @Override
                    public boolean test(ImageBean imageBean) throws Exception {
                        return imageBean.getBitmap() != null;
                    }
                });
    }

    public Observable<ImageBean> getImageFromDisk(String url) {
        return diskCacheUtils.getImage(url)
                .filter(new Predicate<ImageBean>() {
                    @Override
                    public boolean test(ImageBean imageBean) throws Exception {
                        return imageBean.getBitmap() != null;
                    }
                });
    }

    public Observable<ImageBean> getImageFromNetwork(final String url) {
        return networkCacheUtils.getImage(url)
                .filter(new Predicate<ImageBean>() {
                    @Override
                    public boolean test(ImageBean imageBean) throws Exception {
                        return imageBean.getBitmap() != null;
                    }
                }).map(new Function<ImageBean, ImageBean>() {
                    @Override
                    public ImageBean apply(ImageBean imageBean) throws Exception {
                        if (isCompress) {
                            return new ImageBean(url,BitmapUtils.compressBitmap(imageBean.getBitmap()));
                        } else {
                            return imageBean;
                        }
                    }
                }).doOnNext(new Consumer<ImageBean>() {
                    @Override
                    public void accept(ImageBean imageBean) throws Exception {
                        //缓存至磁盘和内存中
                        diskCacheUtils.putDataToCache(imageBean);
                        memoryCacheUtils.putDataToCache(imageBean);
                    }
                });
    }

    public void setCompress(boolean compress) {
        this.isCompress = compress;
    }

    public void close() {
        if (memoryCacheUtils != null) {
            memoryCacheUtils.close();
        }

        if (diskCacheUtils != null) {
            diskCacheUtils.removeAll();
        }

        if (networkCacheUtils != null) {
            networkCacheUtils.close();
        }
    }

    public void flush() {
        if (diskCacheUtils != null) {
            diskCacheUtils.flush();
        }
    }


}
