package com.mottainai.operacional.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mottainai.operacional.R;
import com.mottainai.operacional.repository.AuthRepository;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;

import java.util.Locale;

/** Keeps identity rendering and local sign-out behavior outside the Fragment. */
public class ProfileViewModel extends AndroidViewModel {

    private final MutableLiveData<ProfileUiState> uiState = new MutableLiveData<>();
    private final SessionManager sessionManager;
    private final AuthRepository authRepository;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        sessionManager = new SessionManager(application);
        authRepository = new AuthRepository();
        loadProfile();
    }

    public LiveData<ProfileUiState> getUiState() {
        return uiState;
    }

    public void loadProfile() {
        uiState.setValue(ProfileUiState.loading());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            uiState.setValue(ProfileUiState.signedOut());
            return;
        }

        if (!sessionManager.hasCompleteProfile()) {
            uiState.setValue(ProfileUiState.incompleteSession(getApplication()
                    .getString(R.string.profile_incomplete_session)));
            return;
        }

        String name = firstNonBlank(user.getDisplayName(), sessionManager.getName(),
                getApplication().getString(R.string.profile_name_fallback));
        String email = firstNonBlank(user.getEmail(),
                getApplication().getString(R.string.profile_email_unavailable));
        String role = RoleHelper.roleToLabel(sessionManager.getRole());
        String storeId = sessionManager.getStoreId();
        uiState.setValue(ProfileUiState.content(name, email, role, storeId, initialsFrom(name)));
    }

    public void logout() {
        authRepository.logout(sessionManager);
        uiState.setValue(ProfileUiState.signedOut());
    }

    private static String initialsFrom(String name) {
        String[] words = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                initials.append(word.substring(0, 1));
                if (initials.length() == 2) break;
            }
        }
        return initials.length() == 0 ? "U" : initials.toString().toUpperCase(Locale.ROOT);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) return value.trim();
        }
        return "";
    }
}
