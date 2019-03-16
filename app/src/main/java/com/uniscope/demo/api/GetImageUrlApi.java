package com.uniscope.demo.api;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 作者：majun
 * 时间：2019/3/14 18:28
 * 说明：用来获取百度图片上图片
 * 网址格式：https://image.baidu.com/search/index?tn=baiduimage&word=J20
 */
public interface GetImageUrlApi {

  @GET("search/index")
  Observable<ResponseBody> getImageUrl(@Query("tn") String tn, @Query("word") String word);

}
