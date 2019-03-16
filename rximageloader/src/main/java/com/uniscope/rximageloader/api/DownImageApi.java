package com.uniscope.rximageloader.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * 作者：majun
 * 时间：2019/3/14 15:29
 * 说明：下载文件接口设计
 */
public interface DownImageApi {

    @Streaming
    @GET
    Call<ResponseBody> downloadLatestFeature(@Url String fileUrl);
}
