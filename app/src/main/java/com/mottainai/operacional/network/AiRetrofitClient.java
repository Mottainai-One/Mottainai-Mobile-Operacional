package com.mottainai.operacional.network;

import com.mottainai.operacional.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cliente separado para a Mottainai-IA. Ele nunca envia o token Firebase:
 * recebe apenas o token curto emitido pela API relacional para esta chamada.
 */
public final class AiRetrofitClient {

    private AiRetrofitClient() {
    }

    public static Retrofit getClient(String accessToken) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            logging.redactHeader("Authorization");
            logging.redactHeader("authorization");
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        Interceptor authInterceptor = chain -> {
            Request request = chain.request().newBuilder()
                    .header("Authorization", "Bearer " + accessToken)
                    .build();
            return chain.proceed(request);
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.IA_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
