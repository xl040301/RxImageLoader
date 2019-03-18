package com.uniscope.rximageloader.loader;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.uniscope.rximageloader.bean.ImageBean;
import com.uniscope.rximageloader.creater.RequestCreator;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


/**
 * 作者：majun
 * 时间：2019/3/15 17:50
 * 说明：
 */
public class RxImageLoader {
    private final static String TAG = "RxImageLoader";

    static RxImageLoader instance;
    private String url;
    private RequestCreator requestCreator;

    private CompositeDisposable cd;

    private RxImageLoader(Builder builder) {
        requestCreator = new RequestCreator(builder.context);
        cd = new CompositeDisposable();
    }

    public static RxImageLoader getInstance(Context context) {
         if (instance == null) {
             synchronized (RxImageLoader.class) {
                 if (instance == null) {
                     instance = new Builder(context).build();
                 }
             }
         }
         return instance;
    }

    public RxImageLoader loader(String url) {
        this.url = url;
        return instance;
    }

    public RxImageLoader compress(boolean isCompress) {
        requestCreator.setCompress(isCompress);
        return instance;
    }

    public void into(final ImageView imageView) {
        if (TextUtils.isEmpty(url))
            new NullPointerException("url can not null,please set loader(url)");

        Observable.concat(requestCreator.getImageFromMemory(url),
                requestCreator.getImageFromDisk(url),
                requestCreator.getImageFromNetwork(url))
                .firstElement()
                .toObservable()
                .subscribe(new Observer<ImageBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        cd.add(d);
                    }

                    @Override
                    public void onNext(ImageBean imageBean) {
                        if (imageBean.getBitmap() != null) {
                            imageView.setImageBitmap(imageBean.getBitmap());
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG,"error:"+e.getMessage());

                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG,"onComplete");
                    }
                });
    }

    public static class Builder {
        private Context context;

        public Builder(Context context) {
            this.context = context;
        }

        public RxImageLoader build() {
            return new RxImageLoader(this);
        }
    }

    public void close() {
        if (requestCreator != null) {
            requestCreator.close();
        }
        if (cd != null) {
            cd.dispose();
        }
        if (instance != null) {
            instance = null;
        }
    }

    public void flush() {
        if (requestCreator != null) {
            requestCreator.flush();
        }
    }

}
