package com.mottainai.operacional.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import com.mottainai.operacional.R;
import com.mottainai.operacional.activities.LoginActivity;
import com.mottainai.operacional.utils.Constants;
import com.mottainai.operacional.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private TextView tvUserInfo;
    private Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUserInfo = view.findViewById(R.id.tv_user_info);
        btnLogout = view.findViewById(R.id.btn_logout);

        SessionManager session = new SessionManager(requireContext());

        //mostra role e storeId salvos na sessão
        String roleLabel = roleToLabel(session.getRole());
        String store = session.getStoreId() != null ? session.getStoreId() : "—";
        tvUserInfo.setText("Perfil: " + roleLabel + "\nLoja: " + store);

        //logout
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            session.clearSession();

            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private String roleToLabel(String role) {
        if (role == null) return "—";
        switch (role) {
            case Constants.ROLE_ESTOQUISTA: return "Estoquista";
            case Constants.ROLE_GERENTE:    return "Gerente";
            case Constants.ROLE_DONO:       return "Dono";
            default: return role;
        }
    }
}
