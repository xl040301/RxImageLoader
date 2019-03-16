package com.uniscope.rximageloader.cache;

import com.uniscope.rximageloader.bean.ImageBean;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 作者：majun
 * 时间：2019/3/15 15:54
 * 说明：抽象缓存类
 */
public abstract class CacheObservable {

    /**
     * 获取缓存的图片
     * @param url
     * @return
     */
    public Observable<ImageBean> getImage(final String url) {
        return Observable.create(new ObservableOnSubscribe<ImageBean>() {
            @Override
            public void subscribe(ObservableEmitter<ImageBean> emitter) throws Exception {
                if (!emitter.isDisposed()) {
                    ImageBean imageBean = getDataFromCache(url);
                    if (imageBean != null) {
                        emitter.onNext(imageBean);
                    }
                    emitter.onComplete();
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * 取出缓存数据
     * @param url
     * @return
     */
    public abstract ImageBean getDataFromCache(String url);

    /**
     * 缓存数据
     * @param image
     */
    public abstract void putDataToCache(ImageBean image);

    public abstract void close();

}
