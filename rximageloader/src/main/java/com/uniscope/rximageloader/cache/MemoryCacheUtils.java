package com.uniscope.rximageloader.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

import com.uniscope.rximageloader.bean.ImageBean;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 作者：majun
 * 时间：2019/3/14 9:29
 * 说明：内存缓存
 */
public class MemoryCacheUtils extends CacheObservable  {


    private LruCache<String,Bitmap> mMemoryCache;

    //使用线程安全并发容器，用来存储LruCache执行完LRU策略后被移除的数据，
    //由于SoftReference特性，在内存告急情况下，GC直接 clear SoftReference保存的数据，
    //这样做以最大化保留数据存活时间。
    private ConcurrentHashMap<String,SoftReference<Bitmap>> lruHashBitmap =
            new ConcurrentHashMap<String,SoftReference<Bitmap>>();

    public MemoryCacheUtils() {
        //获取手机最大允许内存的1/8,超过指定内存,则开始回收
        long maxMmenory = Runtime.getRuntime().maxMemory()/8;
        mMemoryCache = new LruCache<String,Bitmap>((int) maxMmenory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //获取图片字节数
                return value.getRowBytes() * value.getHeight();
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                if (oldValue != null) {
                    lruHashBitmap.put(key,new SoftReference<Bitmap>(oldValue));
                }
            }
        };
    }

    /**
     * 通过url获取缓存在内存的bitmap对象
     * @param url
     * @return
     */
    @Override
    public ImageBean getDataFromCache(String url) {
        if(TextUtils.isEmpty(url)) return null;
        Bitmap bitmap = mMemoryCache.get(url);
        if (bitmap != null) {
            return new ImageBean(url,bitmap);
        } else {
            SoftReference<Bitmap> bitmapSoftReference = lruHashBitmap.get(url);
            if (bitmapSoftReference != null) {
                bitmap = bitmapSoftReference.get();
                if (bitmap != null) {
                    return new ImageBean(url,bitmap);
                }
            }
        }
        return null;
    }

    /**
     * 添加bitmap对象至内存缓存中
     * @param image
     */
    @Override
    public void putDataToCache(ImageBean image) {
        if (image != null
                && getDataFromCache(image.getUrl()) == null) {
            mMemoryCache.put(image.getUrl(),image.getBitmap());
        }
    }

    @Override
    public void close() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
        }
    }
}
