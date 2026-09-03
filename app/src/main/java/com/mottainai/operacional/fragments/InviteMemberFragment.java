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

/**
 * Convite de novo membro para a equipe. Sem endpoint de convite/equipe ainda
 * (UserRepository só busca o próprio perfil por uid) — "Enviar convite" avisa
 * que está pendente.
 */
public class InviteMemberFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invite_member, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        EditText etEmail = view.findViewById(R.id.et_invite_email);
        view.findViewById(R.id.btn_send_invite).setOnClickListener(v -> {
            if (etEmail.getText().toString().trim().isEmpty()) {
                etEmail.setError("Informe um e-mail");
                return;
            }
            Toast.makeText(requireContext(), "Convite — pendente (aguarda endpoint)", Toast.LENGTH_SHORT).show();
        });
    }
}
