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

    /** Redis uses the operational JWT; refresh remains on the SQL API. */
    public static Retrofit getRedisClient(Context context) {
        String url = BuildConfig.REDIS_API_BASE_URL;
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException("Servico de notificacoes nao configurado.");
        }
        okhttp3.HttpUrl parsed = okhttp3.HttpUrl.parse(url);
        if (parsed == null || (!BuildConfig.DEBUG && !parsed.isHttps())) {
            throw new IllegalStateException("Endereco do servico de notificacoes invalido.");
        }
        return new Retrofit.Builder().baseUrl(url.endsWith("/") ? url : url + "/")
                .client(new OkHttpClient.Builder()
                        .addInterceptor(new IntegrationAuthInterceptor(context.getApplicationContext()))
                        .connectTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
                        .build())
                .addConverterFactory(GsonConverterFactory.create()).build();
    }

    /** Força recriação após troca de token/baseUrl (ex.: logout/login). */
    public static void clear() {
        retrofit = null;
    }

    /** IA/Redis still accept the SQL integration JWT. SQL requests use Firebase directly. */
    public static String refreshApiAccessToken(Context context) {
        synchronized (INTEGRATION_LOCK) {
            return integrationToken(context, true);
        }
    }

    private static final Object INTEGRATION_LOCK = new Object();
    private static final OkHttpClient integrationClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).build();

    // Call only on OkHttp/background threads: Firebase task listeners use their own executor.
    private static String firebaseToken(Context context, boolean forceRefresh) throws java.io.IOException {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) throw new java.io.IOException("Entre novamente para acessar a API.");
        try {
            String token = com.google.android.gms.tasks.Tasks.await(user.getIdToken(forceRefresh), 20, TimeUnit.SECONDS).getToken();
            com.google.firebase.auth.FirebaseUser current = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (current == null || !user.getUid().equals(current.getUid()) || token == null || token.isEmpty())
                throw new java.io.IOException("A conta foi alterada. Entre novamente.");
            SessionManager session = new SessionManager(context);
            if (session.getUid() != null && !user.getUid().equals(session.getUid()))
                throw new java.io.IOException("A conta foi alterada. Entre novamente.");
            session.saveFirebaseToken(token);
            return token;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new java.io.IOException("Autenticação interrompida.", exception);
        } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException exception) {
            throw new java.io.IOException("Não foi possível renovar a autenticação Firebase.", exception);
        }
    }

    private static class AuthInterceptor implements Interceptor {
        private final Context context;
        AuthInterceptor(Context context) { this.context = context; }
        @Override public okhttp3.Response intercept(Chain chain) throws java.io.IOException {
            Request original = chain.request();
            String path = original.url().encodedPath();
            if ("/api/v1/auth/firebase".equals(path) || "/api/v1/auth/login".equals(path))
                return chain.proceed(original.newBuilder().removeHeader("Authorization").build());
            com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            String uid = user == null ? null : user.getUid();
            String token = firebaseToken(context, false);
            okhttp3.Response response = chain.proceed(original.newBuilder()
                    .header("Authorization", "Bearer " + token).build());
            if (response.code() != 401) return response;
            user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            // Never replay an old user's request under another account.
            if (user == null || !user.getUid().equals(uid)) return response;
            response.close();
            String refreshed = firebaseToken(context, true);
            user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (user == null || !user.getUid().equals(uid)) throw new java.io.IOException("A conta foi alterada.");
            return chain.proceed(original.newBuilder().header("Authorization", "Bearer " + refreshed).build());
        }
    }

    private static String integrationToken(Context context, boolean force) {
        SessionManager session = new SessionManager(context);
        String uid = session.getUid();
        com.google.firebase.auth.FirebaseUser identity = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (uid == null || identity == null || !uid.equals(identity.getUid())) return null;
        if (!force && session.getApiAccessToken() != null) return session.getApiAccessToken();
        try {
            boolean refreshing = session.getApiRefreshToken() != null;
            String payload = new com.google.gson.Gson().toJson(refreshing
                    ? new com.mottainai.operacional.models.ApiRefreshRequest(session.getApiRefreshToken())
                    : new com.mottainai.operacional.models.ApiLoginRequest(firebaseToken(context, false)));
            Request request = new Request.Builder().url(BASE_URL + (refreshing ? "api/v1/auth/refresh" : "api/v1/auth/firebase"))
                    .post(okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), payload)).build();
            try (okhttp3.Response response = integrationClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    if (refreshing && uid.equals(session.getUid())) {
                        session.saveApiAccessToken(null);
                        session.saveApiRefreshToken(null);
                        return integrationToken(context, true);
                    }
                    return null;
                }
                com.mottainai.operacional.models.ApiTokenResponse tokens = new com.google.gson.Gson().fromJson(
                        response.body().string(), com.mottainai.operacional.models.ApiTokenResponse.class);
                com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                if (tokens == null || tokens.getAccessToken() == null || tokens.getAccessToken().trim().isEmpty()
                        || user == null || !uid.equals(user.getUid()) || !uid.equals(session.getUid())) return null;
                session.saveApiAccessToken(tokens.getAccessToken());
                session.saveApiRefreshToken(tokens.getRefreshToken());
                return tokens.getAccessToken();
            }
        } catch (java.io.IOException | RuntimeException exception) { return null; }
    }

    private static class IntegrationAuthInterceptor implements Interceptor {
        private final Context context;
        IntegrationAuthInterceptor(Context context) { this.context = context; }
        @Override public okhttp3.Response intercept(Chain chain) throws java.io.IOException {
            String uid = new SessionManager(context).getUid();
            String token;
            synchronized (INTEGRATION_LOCK) { token = integrationToken(context, false); }
            if (token == null) throw new java.io.IOException("Não foi possível autenticar no serviço de notificações.");
            okhttp3.Response response = chain.proceed(chain.request().newBuilder().header("Authorization", "Bearer " + token).build());
            if (response.code() != 401 || uid == null || !uid.equals(new SessionManager(context).getUid())) return response;
            response.close();
            synchronized (INTEGRATION_LOCK) { token = integrationToken(context, true); }
            if (token == null || !uid.equals(new SessionManager(context).getUid())) throw new java.io.IOException("Sessão de notificações indisponível.");
            return chain.proceed(chain.request().newBuilder().header("Authorization", "Bearer " + token).build());
        }
    }
}
