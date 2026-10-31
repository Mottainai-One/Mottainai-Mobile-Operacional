package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.FragmentChangeEmailBinding;
import com.mottainai.operacional.utils.AuthErrorTranslator;
import com.mottainai.operacional.utils.ValidationUtils;

/** Firebase envia a confirmação ao endereço novo; a troca só vale após abrir o link. */
public class ChangeEmailFragment extends Fragment {
    private FragmentChangeEmailBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChangeEmailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showStatus(getString(R.string.op_session_expired));
            binding.btnSaveEmail.setEnabled(false);
            return;
        }
        binding.tvCurrentEmail.setText(getString(R.string.op_current_email, user.getEmail()));
        binding.btnSaveEmail.setOnClickListener(v -> {
            String next = binding.etNewEmail.getText().toString().trim();
            if (!ValidationUtils.isValidEmail(next)) {
                binding.etNewEmail.setError(getString(R.string.op_valid_email_required));
                return;
            }
            binding.btnSaveEmail.setEnabled(false);
            user.verifyBeforeUpdateEmail(next).addOnCompleteListener(task -> {
                if (binding == null || !isAdded()) return;
                binding.btnSaveEmail.setEnabled(true);
                showStatus(task.isSuccessful() ? getString(R.string.op_email_verification_sent)
                        : AuthErrorTranslator.toUserMessage(task.getException()));
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
