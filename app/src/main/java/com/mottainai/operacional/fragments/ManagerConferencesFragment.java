package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.FragmentManagerConferencesBinding;
import com.mottainai.operacional.utils.Constants;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;

/** Estado das filas gerenciais até a API disponibilizar recebimentos e avarias da loja. */
public class ManagerConferencesFragment extends Fragment {
    private FragmentManagerConferencesBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerConferencesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String role = RoleHelper.normalize(new SessionManager(requireContext()).getRole());
        if (!Constants.ROLE_GERENTE.equals(role)) {
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }
        binding.btnReviewReceiving.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.managerReceivingReviewFragment));
        binding.btnReviewDamage.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.managerDamageReviewFragment));
    }

    @Override public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
