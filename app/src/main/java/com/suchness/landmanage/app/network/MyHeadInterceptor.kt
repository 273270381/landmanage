package com.suchness.deeplearningapp.app.network

import android.util.Log
import com.suchness.landmanage.app.utils.CacheUtil
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * 自定义头部参数拦截器，传入heads
 */
class MyHeadInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        if (CacheUtil.getUser() != null){
            builder.addHeader("token", CacheUtil.getUser()?.accessToken).build()
        }
        return chain.proceed(builder.build())
    }

}