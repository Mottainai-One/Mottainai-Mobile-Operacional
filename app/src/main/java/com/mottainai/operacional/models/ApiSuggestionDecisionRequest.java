package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;

public class ApiSuggestionDecisionRequest {
    @SerializedName("decision") private final String decision;
    public ApiSuggestionDecisionRequest(String decision) { this.decision = decision; }
}
