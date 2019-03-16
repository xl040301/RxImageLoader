package com.uniscope.rximageloader.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.uniscope.rximageloader.api.DownImageApi;
import com.uniscope.rximageloader.bean.ImageBean;
import com.uniscope.rximageloader.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 作者：majun
 * 时间：2019/3/14 15:21
 * 说明：从网络上下载图片
 */
public class NetworkCacheUtils extends CacheObservable {

    public static final String BASE_URL = "https://image.baidu.com/";

    public final static String TN = "baiduimage";

    public NetworkCacheUtils() {

    }

    private Bitmap downloadImage(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        Bitmap bitmap = null;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(3*1000, TimeUnit.SECONDS)
                .readTimeout(3*1000,TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DownImageApi apiService = retrofit.create(DownImageApi.class);
        InputStream inputStream = null;
        try {
            //这里不能使用enqueue()执行
            inputStream = apiService.downloadLatestFeature(url)
                    .execute().body().byteStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.closeQuietly(inputStream);
        }
        return bitmap;
    }

    /**public Bitmap downloadImage(String url) {
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            URL imageUrl = new URL(url);
            URLConnection urlConnection = (HttpURLConnection) imageUrl.openConnection();
            inputStream = urlConnection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Utils.closeQuietly(inputStream);
        }
        return bitmap;
    }**/

    @Override
    public ImageBean getDataFromCache(String url) {
        Bitmap bitmap = downloadImage(url);
        if (bitmap != null) {
            return new ImageBean(url,bitmap);
        } else {
            return null;
        }
    }

    @Override
    public void putDataToCache(ImageBean image) {

    }

    @Override
    public void close() {

    }


}
