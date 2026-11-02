package com.mottainai.operacional.models;

/** Minimal staff directory projection. Contact and document fields are deliberately ignored. */
public class TeamMemberResponse {
    public Integer employeeId;
    public String name;
    public String role;
    public Integer storeId;
    public Boolean active;
}
