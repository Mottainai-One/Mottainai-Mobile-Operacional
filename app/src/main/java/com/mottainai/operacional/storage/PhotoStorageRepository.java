package com.mottainai.operacional.storage;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Sends user-selected images directly to the configured unsigned Cloudinary preset. */
public class PhotoStorageRepository {
    private static final String CLOUD_NAME = "sffp5gbb";
    private static final String UPLOAD_PRESET = "mottainai";
    private static final long MAX_BYTES = 8L * 1024L * 1024L;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);
    private static final String BOUNDARY = "Mottainai-" + UUID.randomUUID();

    public interface PhotoCallback {
        void onSuccess(String photoUrl);
        void onError(String message);
    }

    public void uploadPhoto(Context context, Uri uri, String storeId, String productId,
                            PhotoCallback callback) {
        if (uri == null) {
            callback.onError("Selecione uma imagem antes de enviar.");
            return;
        }
        Context appContext = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String mimeType = appContext.getContentResolver().getType(uri);
                if (mimeType == null || !(mimeType.equals("image/jpeg")
                        || mimeType.equals("image/png") || mimeType.equals("image/webp"))) {
                    throw new IllegalArgumentException("Use uma imagem JPG, PNG ou WebP.");
                }
                long length;
                try (android.content.res.AssetFileDescriptor descriptor =
                             appContext.getContentResolver().openAssetFileDescriptor(uri, "r")) {
                    length = descriptor == null ? -1 : descriptor.getLength();
                }
                if (length > MAX_BYTES) throw new IllegalArgumentException("A imagem deve ter até 8 MB.");

                URL endpoint = new URL("https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload");
                connection = (HttpURLConnection) endpoint.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(30000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                try (OutputStream out = connection.getOutputStream()) {
                    writeField(out, "upload_preset", UPLOAD_PRESET);
                    out.write(("--" + BOUNDARY + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"photo\"\r\nContent-Type: " + mimeType + "\r\n\r\n").getBytes("UTF-8"));
                    try (InputStream in = appContext.getContentResolver().openInputStream(uri)) {
                        if (in == null) throw new IllegalArgumentException("Não foi possível ler a imagem selecionada.");
                        byte[] buffer = new byte[8192];
                        long total = 0;
                        int count;
                        while ((count = in.read(buffer)) != -1) {
                            total += count;
                            if (total > MAX_BYTES) throw new IllegalArgumentException("A imagem deve ter até 8 MB.");
                            out.write(buffer, 0, count);
                        }
                    }
                    out.write(("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8"));
                }
                int status = connection.getResponseCode();
                InputStream responseStream = status >= 200 && status < 300
                        ? connection.getInputStream() : connection.getErrorStream();
                String response = readAll(responseStream);
                if (status < 200 || status >= 300) {
                    throw new IllegalStateException("Cloudinary recusou o envio (HTTP " + status + "). Confira o preset unsigned.");
                }
                String secureUrl = new JSONObject(response).optString("secure_url", "");
                if (secureUrl.isEmpty() || !secureUrl.startsWith("https://")) {
                    throw new IllegalStateException("Cloudinary não retornou uma URL segura para a imagem.");
                }
                postSuccess(callback, secureUrl);
            } catch (Exception e) {
                postError(callback, e.getMessage() == null ? "Falha ao enviar imagem." : e.getMessage());
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private static void writeField(OutputStream out, String name, String value) throws Exception {
        out.write(("--" + BOUNDARY + "\r\nContent-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" + value + "\r\n").getBytes("UTF-8"));
    }

    private static String readAll(InputStream stream) throws Exception {
        if (stream == null) return "";
        try (InputStream in = stream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
            return out.toString("UTF-8");
        }
    }

    private static void postSuccess(PhotoCallback callback, String url) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(url));
    }

    private static void postError(PhotoCallback callback, String message) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onError(message));
    }
}
