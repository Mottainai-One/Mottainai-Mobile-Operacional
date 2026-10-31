package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.FragmentInviteMemberBinding;
import com.mottainai.operacional.utils.ValidationUtils;

/**
 * Convite de novo membro para a equipe. Sem endpoint de convite/equipe ainda
 * (UserRepository só busca o próprio perfil por uid) — "Enviar convite" avisa
 * que está pendente.
 */
public class InviteMemberFragment extends Fragment {

    private FragmentInviteMemberBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentInviteMemberBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        EditText etEmail = binding.etInviteEmail;
        binding.btnSendInvite.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                etEmail.setError("Informe um e-mail");
                return;
            }
            if (!ValidationUtils.isValidEmail(email)) {
                etEmail.setError("Informe um e-mail válido");
                return;
            }
            Toast.makeText(requireContext(), R.string.op_invite_unavailable, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
