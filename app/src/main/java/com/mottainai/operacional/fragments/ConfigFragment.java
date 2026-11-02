package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;

import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.FragmentConfigBinding;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;

/** Configuracoes do dono. Notificacoes reais ficam na caixa interna da API Redis. */
public class ConfigFragment extends Fragment {

    private FragmentConfigBinding binding;

    private View contentRules;
    private View contentNotifications;
    private View contentTeam;

    private SwitchMaterial switchAutoPromo;
    private SwitchMaterial switchRequireApproval;
    private SwitchMaterial switchPushAlerts;
    private SwitchMaterial switchPushSuggestions;
    private SwitchMaterial switchPushInventory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentConfigBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);
        SessionManager sessionManager = new SessionManager(requireContext());

        binding.btnBack.setOnClickListener(v -> navController.popBackStack());

        boolean isOwner = RoleHelper.isOwner(sessionManager.getRole());
        binding.scrollConfig.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        binding.tvNotOwner.setVisibility(isOwner ? View.GONE : View.VISIBLE);
        if (!isOwner) {
            return;
        }

        contentRules = binding.tabContentRules;
        contentNotifications = binding.tabContentNotifications;
        contentTeam = binding.tabContentTeam;
        showTab(0);

        TabLayout tabLayout = binding.tabConfig;
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { showTab(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // There is no persisted operational-rules contract yet. Do not report a local save as an operational change.
        contentRules.setEnabled(false);
        binding.switchAutoPromo.setEnabled(false);
        binding.switchAutoPromo.setChecked(false);
        binding.switchRequireApproval.setEnabled(false);
        binding.switchRequireApproval.setChecked(false);
        binding.btnSaveConfig.setEnabled(false);
        binding.btnSaveConfig.setText("Configuração de regras indisponível");

        ViewGroup notifications = (ViewGroup) contentNotifications;
        for (int i = 0; i < notifications.getChildCount(); i++) {
            notifications.getChildAt(i).setVisibility(View.GONE);
        }
        TextView description = new TextView(requireContext());
        description.setText("As notificações da operação ficam na caixa interna. Toque no sino para consultar as mensagens da sua conta.");
        description.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.op_text));
        description.setTextSize(16);
        notifications.addView(description);

        binding.btnInviteMember.setText(R.string.op_team_open);
        binding.btnInviteMember.setOnClickListener(v -> navController.navigate(R.id.teamFragment));
    }

    private void showTab(int position) {
        contentRules.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        contentNotifications.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        contentTeam.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        contentRules = null;
        contentNotifications = null;
        contentTeam = null;
        switchAutoPromo = null;
        switchRequireApproval = null;
        switchPushAlerts = null;
        switchPushSuggestions = null;
        switchPushInventory = null;
    }
}
