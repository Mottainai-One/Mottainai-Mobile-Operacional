package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.FragmentChangePasswordBinding;
import com.mottainai.operacional.utils.AuthErrorTranslator;

public class ChangePasswordFragment extends Fragment {
    private FragmentChangePasswordBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnSavePassword.setOnClickListener(v -> savePassword());
    }

    private void savePassword() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            showStatus(getString(R.string.op_session_expired));
            return;
        }
        String current = binding.etCurrentPassword.getText().toString();
        String next = binding.etNewPassword.getText().toString();
        String confirm = binding.etConfirmPassword.getText().toString();
        if (current.isEmpty()) {
            binding.etCurrentPassword.setError(getString(R.string.op_current_password_required));
            return;
        }
        if (next.length() < 6) {
            binding.etNewPassword.setError(getString(R.string.op_password_short));
            return;
        }
        if (!next.equals(confirm)) {
            binding.etConfirmPassword.setError(getString(R.string.op_password_mismatch));
            return;
        }
        binding.btnSavePassword.setEnabled(false);
        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), current))
                .addOnCompleteListener(authTask -> {
                    if (binding == null || !isAdded()) return;
                    if (!authTask.isSuccessful()) {
                        binding.btnSavePassword.setEnabled(true);
                        showStatus(AuthErrorTranslator.toUserMessage(authTask.getException()));
                        return;
                    }
                    user.updatePassword(next).addOnCompleteListener(updateTask -> {
                        if (binding == null || !isAdded()) return;
                        binding.btnSavePassword.setEnabled(true);
                        if (updateTask.isSuccessful()) {
                            binding.etCurrentPassword.getText().clear();
                            binding.etNewPassword.getText().clear();
                            binding.etConfirmPassword.getText().clear();
                            showStatus(getString(R.string.op_password_saved));
                        } else {
                            showStatus(AuthErrorTranslator.toUserMessage(updateTask.getException()));
                        }
                    });
                });
    }

    private void showStatus(String message) {
        binding.tvChangeStatus.setText(message);
        binding.tvChangeStatus.setVisibility(View.VISIBLE);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
