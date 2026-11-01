package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;

public class ApiAlertResponse {
    @SerializedName("id") public Integer id;
    @SerializedName("storeId") public Integer storeId;
    @SerializedName("title") public String title;
    @SerializedName("description") public String description;
    @SerializedName("alertType") public String alertType;
    @SerializedName("priority") public String priority;
    @SerializedName("status") public String status;
    @SerializedName("generatedAt") public String generatedAt;
}
