package com.mottainai.operacional.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;

import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.FragmentProfileBinding;
import com.mottainai.operacional.activities.LoginActivity;
import com.mottainai.operacional.repository.NotificationRepository;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.utils.ThemeSettings;
import com.mottainai.operacional.utils.Constants;
import com.mottainai.operacional.viewmodels.ProfileUiState;
import com.mottainai.operacional.viewmodels.ProfileViewModel;

/** Presents the authenticated identity without using the displayed data for authorization. */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    private View profileContent;
    private View profileInfoGroup;
    private ProgressBar progressBar;
    private TextView tvAvatarInitials;
    private TextView tvName;
    private TextView tvEmail;
    private TextView tvRole;
    private TextView tvStore;
    private TextView tvStatus;
    private MaterialButton btnLogout;
    private boolean redirectingToLogin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);

        ProfileViewModel viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.getUiState().observe(getViewLifecycleOwner(), this::render);

        View btnOpenConfig = binding.btnOpenConfig;
        btnOpenConfig.setVisibility(View.VISIBLE);
        btnOpenConfig.setOnClickListener(v -> Navigation.findNavController(view)
                .navigate(R.id.accountSettingsFragment));

        binding.btnLightTheme.setOnClickListener(v -> ThemeSettings.setDark(requireContext(), false));
        binding.btnDarkTheme.setOnClickListener(v -> ThemeSettings.setDark(requireContext(), true));
        renderThemeSelection();

        String role = RoleHelper.normalize(new SessionManager(requireContext()).getRole());
        if (Constants.ROLE_ESTOQUISTA.equals(role) || Constants.ROLE_DONO.equals(role)
                || Constants.ROLE_GERENTE.equals(role)) {
            binding.btnRoleShortcut.setVisibility(View.VISIBLE);
            binding.btnRoleShortcut.setText(Constants.ROLE_DONO.equals(role)
                    ? R.string.op_reports : Constants.ROLE_GERENTE.equals(role)
                    ? R.string.op_team_title : R.string.op_view_tasks);
            binding.btnRoleShortcut.setOnClickListener(v -> {
                if (Constants.ROLE_DONO.equals(role)) {
                    Navigation.findNavController(view).navigate(R.id.reportsFragment);
                } else if (Constants.ROLE_GERENTE.equals(role)) {
                    Navigation.findNavController(view).navigate(R.id.teamFragment);
                } else {
                    Toast.makeText(requireContext(), R.string.op_more_information_unavailable,
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        btnLogout.setOnClickListener(v -> showLogoutConfirmation(viewModel));
    }

    private void bindViews(View view) {
        profileContent = binding.profileContent;
        profileInfoGroup = binding.profileInfoGroup;
        progressBar = binding.progressProfile;
        tvAvatarInitials = binding.tvAvatarInitials;
        tvName = binding.tvProfileName;
        tvEmail = binding.tvProfileEmail;
        tvRole = binding.tvProfileRole;
        tvStore = binding.tvProfileStore;
        tvStatus = binding.tvProfileStatus;
        btnLogout = binding.btnLogout;
    }

    private void renderThemeSelection() {
        boolean dark = ThemeSettings.isDark(requireContext());
        binding.btnLightTheme.setBackgroundResource(dark
                ? R.drawable.op_soft_card : R.drawable.op_theme_selected);
        binding.btnDarkTheme.setBackgroundResource(dark
                ? R.drawable.op_theme_selected : R.drawable.op_soft_card);
        binding.btnLightTheme.setTextColor(ContextCompat.getColor(requireContext(), dark
                ? R.color.op_text : R.color.op_theme_selection_text));
        binding.btnDarkTheme.setTextColor(ContextCompat.getColor(requireContext(), dark
                ? R.color.op_theme_selection_text : R.color.op_text));
    }

    private void render(ProfileUiState state) {
        boolean loading = state.getStatus() == ProfileUiState.Status.LOADING;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        // profile_content (o ScrollView) fica visível sempre que não está carregando;
        // profile_info_group e tv_profile_status alternam DENTRO dele conforme o
        // estado, com btn_logout como último item do mesmo fluxo — assim ele fica
        // logo abaixo do conteúdo visível, nunca colado no rodapé físico da tela.
        profileContent.setVisibility(loading ? View.GONE : View.VISIBLE);

        boolean isContent = state.getStatus() == ProfileUiState.Status.CONTENT;
        boolean isIncompleteSession = state.getStatus() == ProfileUiState.Status.INCOMPLETE_SESSION;
        profileInfoGroup.setVisibility(isContent ? View.VISIBLE : View.GONE);
        tvStatus.setVisibility(isIncompleteSession ? View.VISIBLE : View.GONE);
        btnLogout.setVisibility(isContent || isIncompleteSession ? View.VISIBLE : View.GONE);

        if (isContent) {
            tvAvatarInitials.setText(state.getInitials());
            tvName.setText(state.getName());
            tvEmail.setText(state.getEmail());
            tvRole.setText(state.getRole() + " — " + getString(R.string.op_store_id, state.getStoreId()));
            tvStore.setVisibility(View.GONE);
            btnLogout.setEnabled(true);
            return;
        }

        if (isIncompleteSession) {
            tvStatus.setText(state.getMessage());
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
        intent.putExtra(LoginActivity.EXTRA_SIGNED_OUT, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        profileContent = null;
        profileInfoGroup = null;
        progressBar = null;
        tvAvatarInitials = null;
        tvName = null;
        tvEmail = null;
        tvRole = null;
        tvStore = null;
        tvStatus = null;
        btnLogout = null;
    }
}
