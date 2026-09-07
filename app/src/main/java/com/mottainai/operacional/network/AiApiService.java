package com.mottainai.operacional.network;

import com.mottainai.operacional.models.AiChatHistoryResponse;
import com.mottainai.operacional.models.AiChatRequest;
import com.mottainai.operacional.models.AiChatResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/** Contrato público da Mottainai-IA consumido pelo aplicativo. */
public interface AiApiService {

    @POST("chat")
    Call<AiChatResponse> sendMessage(@Body AiChatRequest request);

    @GET("chat/history/{sessionId}")
    Call<AiChatHistoryResponse> getHistory(@Path("sessionId") String sessionId);
}
