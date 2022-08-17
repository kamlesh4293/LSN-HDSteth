package com.test

import com.app.lsnhdsteth.network.TLSSocketFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    fun getInstance(): Retrofit {
        var mHttpLoggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

        val tlsSocketFactory = TLSSocketFactory()

        var mOkHttpClient = OkHttpClient
            .Builder()
            .sslSocketFactory(tlsSocketFactory, tlsSocketFactory.trustManager!!)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(mHttpLoggingInterceptor)
            .build()


        var retrofit: Retrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://us.lsquared.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(mOkHttpClient)
            .build()
        return retrofit
    }

    fun getOkhttpClient(): OkHttpClient{
        var mHttpLoggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

        val tlsSocketFactory = TLSSocketFactory()

        return OkHttpClient
            .Builder()
            .sslSocketFactory(tlsSocketFactory, tlsSocketFactory.trustManager!!)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(mHttpLoggingInterceptor)
            .build()

    }

}
