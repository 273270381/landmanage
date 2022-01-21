package com.suchness.landmanage.app.network;

import com.suchness.landmanage.app.network.download.DownLoadMoreCallBack;
import com.suchness.landmanage.app.network.download.DownLoadSubscriber;
import com.suchness.landmanage.app.network.download.ProgressCallBack;
import com.suchness.landmanage.app.network.download.ProgressInterceptor;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import me.hgj.jetpackmvvm.network.NetworkUtil;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by goldze on 2017/5/11.
 * 文件下载管理，封装一行代码实现下载
 */

public class DownLoadManager {
    private static DownLoadManager instance;

    private static Retrofit retrofit;

    private DownLoadManager() {
        buildNetWork();
    }

    /**
     * 单例模式
     *
     * @return DownLoadManager
     */
    public static DownLoadManager getInstance() {
        if (instance == null) {
            instance = new DownLoadManager();
        }
        return instance;
    }

    //下载
    public void load(String fileName,String time, final ProgressCallBack callBack) {
        retrofit.create(ApiService.class)
                .downLoadImag(fileName,time)
                .subscribeOn(Schedulers.io())//请求网络 在调度者的io线程
                .observeOn(Schedulers.io()) //指定线程保存文件
                .doOnNext(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) throws Exception {
                        callBack.saveFile(responseBody);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()) //在主线程中更新ui
                .subscribe(new DownLoadSubscriber<ResponseBody>(callBack));
    }

    //Worker中下载
    public void loadmore(String fileName,String nameLike, final DownLoadMoreCallBack callBack){
        retrofit.create(ApiService.class)
                .downLoadMoreImag(fileName,nameLike)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) throws Exception {
                        callBack.saveFile(responseBody);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DownLoadSubscriber<ResponseBody>(callBack));
    }

    private void buildNetWork() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new ProgressInterceptor())
                .connectTimeout(20, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(NetworkUtil.url)
                .build();
    }

    private interface ApiService {
        @Streaming
        @GET
        Observable<ResponseBody> download(@Url String url);

        /**
         * 文件下载
         */
        @Streaming
        @GET("http://183.208.120.226:6666/api/minio/download/{fileName}/{time}")
        Observable<ResponseBody> downLoadImag(@Path("fileName") String  fileName, @Path("time") String  time);


        /**
         * 批量文件下载
         */
        @FormUrlEncoded
        @Streaming
        @POST("http://183.208.120.226:6666/api/minio/download")
        Observable<ResponseBody> downLoadMoreImag(@Field("fileName") String fileName,@Field("nameLike") String nameLike);
    }
}
