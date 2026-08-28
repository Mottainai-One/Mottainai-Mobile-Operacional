package com.mottainai.operacional.network;

import android.content.Context;

import com.mottainai.operacional.BuildConfig;
import com.mottainai.operacional.utils.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // TODO MOBILE-03: confirmar base URL real com backend. Valor vem de BuildConfig.API_BASE_URL.
    private static final String BASE_URL = BuildConfig.API_BASE_URL;
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            // Nunca logar BODY em release; não expor Authorization/token.
            if (BuildConfig.DEBUG) {
                logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
                logging.redactHeader("Authorization");
                logging.redactHeader("authorization");
            } else {
                logging.setLevel(HttpLoggingInterceptor.Level.NONE);
            }

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(new AuthInterceptor(context.getApplicationContext()))
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /** Força recriação após troca de token/baseUrl (ex.: logout/login). */
    public static void clear() {
        retrofit = null;
    }

    private static class AuthInterceptor implements Interceptor {
        private final SessionManager sessionManager;

        AuthInterceptor(Context context) {
            this.sessionManager = new SessionManager(context);
        }

        @Override
        public okhttp3.Response intercept(Chain chain) throws java.io.IOException {
            String token = sessionManager.getToken();
            Request original = chain.request();
            Request.Builder builder = original.newBuilder();
            if (token != null && !token.isEmpty()) {
                // Token deve ser Firebase ID token, se backend confirmar aceitação.
                builder.header("Authorization", "Bearer " + token);
            }
            return chain.proceed(builder.build());
        }
    }
}