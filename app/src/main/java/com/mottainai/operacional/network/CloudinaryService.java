package com.mottainai.operacional.network;

import com.google.gson.annotations.SerializedName;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface CloudinaryService {
    @Multipart
    @POST("v1_1/{cloud}/image/upload")
    Call<CloudinaryUploadResponse> upload(
            @Path("cloud") String cloud,
            @Part MultipartBody.Part file,
            @Part("upload_preset") RequestBody uploadPreset,
            @Part("folder") RequestBody folder);

    class CloudinaryUploadResponse {
        @SerializedName("secure_url") private String secureUrl;
        public String getSecureUrl() { return secureUrl; }
    }
}
