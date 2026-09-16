package com.mottainai.operacional.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.mottainai.operacional.R;
import com.mottainai.operacional.activities.LoginActivity;
import com.mottainai.operacional.repository.NotificationRepository;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.viewmodels.ProfileUiState;
import com.mottainai.operacional.viewmodels.ProfileViewModel;

/** Presents the authenticated identity without using the displayed data for authorization. */
public class ProfileFragment extends Fragment {

    private View profileContent;
    private ProgressBar progressBar;
    private TextView tvAvatarInitials;
    private TextView tvName;
    private TextView tvEmail;
    private TextView tvRole;
    private TextView tvStore;
    private TextView tvStatus;
    private Button btnLogout;
    private boolean redirectingToLogin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);

        ProfileViewModel viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.getUiState().observe(getViewLifecycleOwner(), this::render);

        View btnOpenConfig = view.findViewById(R.id.btn_open_config);
        if (RoleHelper.isOwner(new SessionManager(requireContext()).getRole())) {
            btnOpenConfig.setVisibility(View.VISIBLE);
            btnOpenConfig.setOnClickListener(v -> Navigation.findNavController(view)
                    .navigate(R.id.action_profileFragment_to_configFragment));
        }

        btnLogout.setOnClickListener(v -> showLogoutConfirmation(viewModel));
    }

    private void bindViews(View view) {
        profileContent = view.findViewById(R.id.profile_content);
        progressBar = view.findViewById(R.id.progress_profile);
        tvAvatarInitials = view.findViewById(R.id.tv_avatar_initials);
        tvName = view.findViewById(R.id.tv_profile_name);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvRole = view.findViewById(R.id.tv_profile_role);
        tvStore = view.findViewById(R.id.tv_profile_store);
        tvStatus = view.findViewById(R.id.tv_profile_status);
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    private void render(ProfileUiState state) {
        boolean loading = state.getStatus() == ProfileUiState.Status.LOADING;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        profileContent.setVisibility(state.getStatus() == ProfileUiState.Status.CONTENT
                ? View.VISIBLE : View.GONE);

        if (state.getStatus() == ProfileUiState.Status.CONTENT) {
            tvAvatarInitials.setText(state.getInitials());
            tvName.setText(state.getName());
            tvEmail.setText(state.getEmail());
            tvRole.setText(getString(R.string.profile_role_value, state.getRole()));
            tvStore.setText(getString(R.string.profile_store_value, state.getStoreId()));
            tvStatus.setVisibility(View.GONE);
            btnLogout.setEnabled(true);
            return;
        }

        if (state.getStatus() == ProfileUiState.Status.INCOMPLETE_SESSION) {
            tvStatus.setText(state.getMessage());
            tvStatus.setVisibility(View.VISIBLE);
            btnLogout.setEnabled(true);
            return;
        }

        if (state.getStatus() == ProfileUiState.Status.SIGNED_OUT) {
            redirectToLogin();
        }
    }

    private void showLogoutConfirmation(ProfileViewModel viewModel) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.profile_logout_title)
                .setMessage(R.string.profile_logout_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.profile_logout_confirm, (dialog, which) -> {
                    btnLogout.setEnabled(false);
                    new NotificationRepository(requireContext()).clearForLogout();
                    viewModel.logout();
                })
                .show();
    }

    private void redirectToLogin() {
        if (redirectingToLogin || !isAdded()) return;
        redirectingToLogin = true;
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
