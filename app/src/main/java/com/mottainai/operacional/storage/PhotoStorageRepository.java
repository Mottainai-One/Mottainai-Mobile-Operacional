package com.mottainai.operacional.storage;

import android.content.Context;
import android.net.Uri;

import com.mottainai.operacional.BuildConfig;
import com.mottainai.operacional.network.CloudinaryService;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Real, unsigned Cloudinary image upload. No upload API secret is shipped in the app. */
public class PhotoStorageRepository {
    private static final int MAX_IMAGE_BYTES = 8 * 1024 * 1024;
    private final CloudinaryService cloudinary;

    public interface PhotoCallback {
        void onSuccess(String photoUrl);
        void onError(String message);
    }

    public PhotoStorageRepository() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS).readTimeout(45, TimeUnit.SECONDS)
                .writeTimeout(45, TimeUnit.SECONDS).build();
        cloudinary = new Retrofit.Builder().baseUrl("https://api.cloudinary.com/")
                .client(client).addConverterFactory(GsonConverterFactory.create()).build()
                .create(CloudinaryService.class);
    }

    public void uploadPhoto(Context context, Uri uri, String storeId, String productId, PhotoCallback callback) {
        if (uri == null) { callback.onSuccess(null); return; }
        String mime = context.getContentResolver().getType(uri);
        if (mime == null || !mime.toLowerCase(Locale.ROOT).startsWith("image/")) {
            callback.onError("Selecione um arquivo de imagem."); return;
        }
        try {
            byte[] bytes;
            try (InputStream input = context.getContentResolver().openInputStream(uri);
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                if (input == null) throw new IllegalArgumentException("Não foi possível ler a imagem.");
                byte[] buffer = new byte[8192];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    if (output.size() + count > MAX_IMAGE_BYTES) {
                        callback.onError("A imagem precisa ter até 8 MB."); return;
                    }
                    output.write(buffer, 0, count);
                }
                bytes = output.toByteArray();
            }
            if (bytes.length == 0) { callback.onError("O arquivo de imagem está vazio."); return; }
            RequestBody body = RequestBody.create(MediaType.parse(mime), bytes);
            MultipartBody.Part file = MultipartBody.Part.createFormData("file", "photo", body);
            String safeStore = storeId == null ? "unknown" : storeId.replaceAll("[^A-Za-z0-9_-]", "_");
            String safeProduct = productId == null ? "pending" : productId.replaceAll("[^A-Za-z0-9_-]", "_");
            cloudinary.upload(BuildConfig.CLOUDINARY_CLOUD_NAME, file,
                    RequestBody.create(null, BuildConfig.CLOUDINARY_UPLOAD_PRESET),
                    RequestBody.create(null, "mottainai/operational/" + safeStore + "/" + safeProduct))
                    .enqueue(new Callback<CloudinaryService.CloudinaryUploadResponse>() {
                        @Override public void onResponse(Call<CloudinaryService.CloudinaryUploadResponse> call,
                                Response<CloudinaryService.CloudinaryUploadResponse> response) {
                            if (response.isSuccessful() && response.body() != null
                                    && response.body().getSecureUrl() != null
                                    && response.body().getSecureUrl().startsWith("https://")) {
                                callback.onSuccess(response.body().getSecureUrl());
                            } else callback.onError("Cloudinary recusou o upload (HTTP " + response.code() + ").");
                        }
                        @Override public void onFailure(Call<CloudinaryService.CloudinaryUploadResponse> call, Throwable error) {
                            callback.onError("Falha de rede no upload para Cloudinary: " + error.getMessage());
                        }
                    });
        } catch (Exception error) {
            callback.onError("Não foi possível preparar a imagem: " + error.getMessage());
        }
    }
}
