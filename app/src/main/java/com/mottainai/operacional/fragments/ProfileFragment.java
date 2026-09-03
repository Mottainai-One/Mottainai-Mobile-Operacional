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
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;

import com.mottainai.operacional.R;
import com.mottainai.operacional.activities.LoginActivity;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.utils.RoleHelper;

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

        String roleLabel = RoleHelper.roleToLabel(session.getRole());  // <- único lugar
        String store = session.getStoreId() != null ? session.getStoreId() : "—";
        tvUserInfo.setText("Perfil: " + roleLabel + "\nLoja: " + store);

        // Configurações da operação são exclusivas do Dono.
        View btnOpenConfig = view.findViewById(R.id.btn_open_config);
        if (RoleHelper.isOwner(session.getRole())) {
            btnOpenConfig.setVisibility(View.VISIBLE);
            btnOpenConfig.setOnClickListener(v ->
                    Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_configFragment));
        }

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            session.clearSession();

            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}