package com.mottainai.operacional.network;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Redis inbox scoped by the verified operational user and company. */
public interface RedisNotificationApi {
    @GET("v1/notifications")
    Call<Inbox> list(@Query("limit") int limit);

    @POST("v1/notifications/{id}/read")
    Call<ReadReceipt> markRead(@Path("id") String id);

    class Inbox {
        public int unreadCount;
        public List<Item> notifications;
    }

    class Item {
        public String id;
        public String title;
        public String body;
        public int priority;
        public String status;
        public long createdAt;
    }

    class ReadReceipt {
        public String id;
        public String status;
    }
}
