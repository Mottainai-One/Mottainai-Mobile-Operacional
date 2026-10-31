package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.FragmentAccountSettingsBinding;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;

import java.util.Locale;

public class AccountSettingsFragment extends Fragment {
    private FragmentAccountSettingsBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SessionManager session = new SessionManager(requireContext());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = session.getName() == null ? "" : session.getName().trim();
        String email = user == null || user.getEmail() == null ? "—" : user.getEmail();
        binding.tvAccountName.setText(name);
        binding.tvAccountEmail.setText(email);
        binding.tvCurrentEmail.setText(email);
        StringBuilder initials = new StringBuilder();
        for (String word : name.split("\\s+")) {
            if (!word.isEmpty()) initials.append(word.charAt(0));
            if (initials.length() == 2) break;
        }
        binding.tvAccountInitials.setText(initials.length() == 0
                ? "U" : initials.toString().toUpperCase(Locale.ROOT));
        binding.rowEmail.setOnClickListener(v -> Navigation.findNavController(view)
                .navigate(R.id.changeEmailFragment));
        binding.rowPassword.setOnClickListener(v -> Navigation.findNavController(view)
                .navigate(R.id.changePasswordFragment));
        if (RoleHelper.isOwner(session.getRole())) {
            binding.btnStoreSettings.setVisibility(View.VISIBLE);
            binding.btnStoreSettings.setOnClickListener(v -> Navigation.findNavController(view)
                    .navigate(R.id.configFragment));
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
