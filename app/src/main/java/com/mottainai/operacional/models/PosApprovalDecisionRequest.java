package com.mottainai.operacional.models;
/** Actor/store are resolved by the API, never submitted by the screen. */
public class PosApprovalDecisionRequest {
    public final String decision,observation;
    public PosApprovalDecisionRequest(String decision,String observation){this.decision=decision;this.observation=observation;}
}
