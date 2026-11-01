package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;

public class ApiUserProfile {
    @SerializedName("id") private Integer id;
    @SerializedName("name") private String name;
    @SerializedName("email") private String email;
    @SerializedName("role") private String role;
    @SerializedName("active") private Boolean active;
    @SerializedName("storeId") private Integer storeId;
    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Boolean getActive() { return active; }
    public Integer getStoreId() { return storeId; }
}
