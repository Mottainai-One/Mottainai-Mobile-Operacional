package com.mottainai.operacional.viewmodels;

/** Immutable state rendered by the operational profile screen. */
public final class ProfileUiState {

    public enum Status {
        LOADING,
        CONTENT,
        INCOMPLETE_SESSION,
        SIGNED_OUT
    }

    private final Status status;
    private final String name;
    private final String email;
    private final String role;
    private final String storeId;
    private final String initials;
    private final String message;

    private ProfileUiState(Status status, String name, String email, String role,
                           String storeId, String initials, String message) {
        this.status = status;
        this.name = name;
        this.email = email;
        this.role = role;
        this.storeId = storeId;
        this.initials = initials;
        this.message = message;
    }

    public static ProfileUiState loading() {
        return new ProfileUiState(Status.LOADING, null, null, null, null, null, null);
    }

    public static ProfileUiState content(String name, String email, String role,
                                         String storeId, String initials) {
        return new ProfileUiState(Status.CONTENT, name, email, role, storeId, initials, null);
    }

    public static ProfileUiState incompleteSession(String message) {
        return new ProfileUiState(Status.INCOMPLETE_SESSION, null, null, null, null, null, message);
    }

    public static ProfileUiState signedOut() {
        return new ProfileUiState(Status.SIGNED_OUT, null, null, null, null, null, null);
    }

    public Status getStatus() { return status; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStoreId() { return storeId; }
    public String getInitials() { return initials; }
    public String getMessage() { return message; }
}
