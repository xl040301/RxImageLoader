package com.uniscope.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.GridView;

import com.uniscope.demo.adapter.PhotosWallAdapter;
import com.uniscope.demo.api.GetImageUrlApi;
import com.uniscope.demo.utils.ImageSource;
import com.uniscope.rximageloader.cache.NetworkCacheUtils;
import com.uniscope.rximageloader.loader.RxImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AbstractBaseActivity implements AbsListView.OnScrollListener {

    private String[] mImageWords = { "J20", "天空之城", "千与千寻", "清新美女", "美女壁纸" };

    private List<String> mImageUrlList = new ArrayList<>();
    private ImageSource imageSource;

    private int mImageThumbSize;
    private int mImageThumbSpacing;



    /** GridView的适配器 */
    private PhotosWallAdapter mWallAdapter;

   // @BindView(R.id.photo_wall)
   private GridView mPhotoWallView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPhotoWallView = (GridView) findViewById(R.id.photo_wall);
        if (isPermissionGranted()) {
            initEvent();
            initData();
        }
    }

    private void initData() {
        if (mWallAdapter == null) {
            mWallAdapter = new PhotosWallAdapter(this,mImageUrlList);
        }
        mPhotoWallView.setAdapter(mWallAdapter);
        imageSource = new ImageSource();
        imageSource.setDuRegex(true);
        getImageListFromNet(4);
    }

    private void initEvent() {
        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
        // 监听获取图片的宽高
        mPhotoWallView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // 计算列数
                final int numColumns = (int) Math.floor(mPhotoWallView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
                if (numColumns > 0) {
                    int columnWidth = (mPhotoWallView.getWidth() / numColumns) - mImageThumbSpacing;
                    mWallAdapter.setItemSize(columnWidth);
                    mPhotoWallView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    /**
     * 从百度搜索页面提取具体图片URL地址
     * @param index
     */
    private void getImageListFromNet(final int index) {
        final String regex = ImageSource.regex[2];
        String word = mImageWords[index];

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(2*1000,TimeUnit.SECONDS)
                .build();
        new Retrofit.Builder()
                .baseUrl(NetworkCacheUtils.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build().create(GetImageUrlApi.class)
                .getImageUrl(NetworkCacheUtils.TN,word)
                .map(new Function<ResponseBody, ArrayList<String>>() {
                    @Override
                    public ArrayList<String> apply(ResponseBody responseBody) throws Exception {
                        String html = responseBody.string();
                        if (!TextUtils.isEmpty(html)) {
                            return imageSource.ParseHtmlToImage(html,regex);
                        }
                        return null;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<String>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ArrayList<String> strings) {
                        if (strings != null) {
                            Log.i("majun","get image url count = "+strings.size());
                            mImageUrlList.addAll(strings);
                            mWallAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        RxImageLoader.getInstance(this).flush();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxImageLoader.getInstance(this).close();
    }

    @Override
    protected void onGetPermissionsSuccess() {
        initEvent();
        initData();
    }

    @Override
    protected void onGetPermissionsFailure() {
         finish();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}
