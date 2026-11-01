package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;

public class ApiSuggestionResponse {
    @SerializedName("id") public Integer id;
    @SerializedName("alertId") public Integer alertId;
    @SerializedName("actionType") public String actionType;
    @SerializedName("description") public String description;
    @SerializedName("priority") public String priority;
    @SerializedName("status") public String status;
    @SerializedName("generatedAt") public String generatedAt;
}
