package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.tabs.TabLayout;

import com.mottainai.operacional.R;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;

/**
 * Configurações da operação: exclusiva do Dono (RoleHelper.isOwner).
 *
 * As regras/notificações ainda não têm onde persistir (não existe endpoint de
 * configuração da loja), então "Salvar" só avisa que está pendente — mesma
 * honestidade que o resto do app usa para funcionalidades que aguardam
 * backend. A aba Equipe mostra um convite e um membro de exemplo, já que
 * também não existe endpoint de listagem de equipe (UserRepository só busca o
 * próprio perfil por uid).
 */
public class ConfigFragment extends Fragment {

    private View contentRules;
    private View contentNotifications;
    private View contentTeam;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);
        SessionManager sessionManager = new SessionManager(requireContext());

        boolean isOwner = RoleHelper.isOwner(sessionManager.getRole());
        view.findViewById(R.id.scroll_config).setVisibility(isOwner ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.tv_not_owner).setVisibility(isOwner ? View.GONE : View.VISIBLE);
        if (!isOwner) {
            return;
        }

        contentRules = view.findViewById(R.id.tab_content_rules);
        contentNotifications = view.findViewById(R.id.tab_content_notifications);
        contentTeam = view.findViewById(R.id.tab_content_team);

        TabLayout tabLayout = view.findViewById(R.id.tab_config);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { showTab(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        view.findViewById(R.id.btn_save_config).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Configurações salvas — pendente (aguarda endpoint)", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_invite_member).setOnClickListener(v ->
                navController.navigate(R.id.action_configFragment_to_inviteMemberFragment));

        view.findViewById(R.id.card_member_example).setOnClickListener(v -> {
            String name = ((TextView) view.findViewById(R.id.tv_member_name)).getText().toString();
            String role = ((TextView) view.findViewById(R.id.tv_member_role)).getText().toString();
            Bundle args = new Bundle();
            args.putString("member_name", name);
            args.putString("member_role", role);
            navController.navigate(R.id.action_configFragment_to_editMemberFragment, args);
        });
    }

    private void showTab(int position) {
        contentRules.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        contentNotifications.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        contentTeam.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
    }
}
