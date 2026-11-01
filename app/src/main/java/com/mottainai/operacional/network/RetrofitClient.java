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

    /** Obtém um JWT operacional novo para chamadas ao serviço de IA. */
    public static String refreshApiAccessToken(Context context) {
        AuthInterceptor interceptor = new AuthInterceptor(context.getApplicationContext());
        synchronized (AuthInterceptor.REFRESH_LOCK) {
            return interceptor.refreshAccessToken();
        }
    }

    private static class AuthInterceptor implements Interceptor {
        private static final Object REFRESH_LOCK = new Object();
        private final SessionManager sessionManager;

        AuthInterceptor(Context context) {
            this.sessionManager = new SessionManager(context);
        }

        @Override
        public okhttp3.Response intercept(Chain chain) throws java.io.IOException {
            String token = sessionManager.getApiAccessToken();
            Request original = chain.request();
            Request.Builder builder = original.newBuilder();
            if (token != null && !token.isEmpty()) {
                // Token Firebase usado pela integração atual.
                builder.header("Authorization", "Bearer " + token);
            }
            okhttp3.Response response = chain.proceed(builder.build());
            if (response.code() != 401 || original.header("X-Auth-Retry") != null
                    || sessionManager.getApiRefreshToken() == null) return response;

            response.close();
            String refreshed;
            synchronized (REFRESH_LOCK) {
                // Parallel requests can all receive 401 for the same access token. Only
                // the first one should rotate the refresh token; the others reuse its result.
                String latest = sessionManager.getApiAccessToken();
                if (latest != null && !latest.isEmpty() && !latest.equals(token)) {
                    refreshed = latest;
                } else {
                    refreshed = refreshAccessToken();
                }
            }
            if (refreshed == null) return chain.proceed(original.newBuilder()
                    .header("X-Auth-Retry", "1").build());
            return chain.proceed(original.newBuilder()
                    .header("Authorization", "Bearer " + refreshed)
                    .header("X-Auth-Retry", "1").build());
        }

        private String refreshAccessToken() {
            String refreshToken = sessionManager.getApiRefreshToken();
            if (refreshToken == null || refreshToken.trim().isEmpty()) return null;
            String payload = new com.google.gson.Gson().toJson(
                    new com.mottainai.operacional.models.ApiRefreshRequest(refreshToken));
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(BASE_URL + "api/v1/auth/refresh")
                    .post(okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), payload))
                    .build();
            try (okhttp3.Response response = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build()
                    .newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) return null;
                com.mottainai.operacional.models.ApiTokenResponse tokens = new com.google.gson.Gson().fromJson(
                        response.body().string(), com.mottainai.operacional.models.ApiTokenResponse.class);
                if (tokens == null || tokens.getAccessToken() == null || tokens.getRefreshToken() == null) return null;
                sessionManager.saveApiAccessToken(tokens.getAccessToken());
                sessionManager.saveApiRefreshToken(tokens.getRefreshToken());
                return tokens.getAccessToken();
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
