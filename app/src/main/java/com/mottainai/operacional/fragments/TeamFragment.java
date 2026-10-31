package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mottainai.operacional.R;
import com.mottainai.operacional.adapters.TeamAdapter;
import com.mottainai.operacional.databinding.FragmentTeamBinding;
import com.mottainai.operacional.models.User;
import com.mottainai.operacional.repository.UserRepository;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;

import java.util.List;

/** Equipe lida da coleção users, limitada ao storeID da sessão. */
public class TeamFragment extends Fragment {
    private FragmentTeamBinding binding;
    private TeamAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTeamBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SessionManager session = new SessionManager(requireContext());
        boolean owner = RoleHelper.isOwner(session.getRole());
        if (!RoleHelper.canRegisterProduct(session.getRole())) {
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }
        adapter = new TeamAdapter(owner, member -> {
            Bundle args = new Bundle();
            args.putString("member_name", member.getName());
            args.putString("member_role", RoleHelper.roleToLabel(member.getRole()));
            NavHostFragment.findNavController(this).navigate(R.id.editMemberFragment, args);
        });
        binding.rvTeam.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTeam.setAdapter(adapter);
        binding.btnTeamPrimary.setText(owner ? R.string.op_team_invite : R.string.op_team_alerts);
        binding.btnTeamPrimary.setOnClickListener(v -> {
            if (owner) {
                NavHostFragment.findNavController(this).navigate(R.id.inviteMemberFragment);
            } else {
                Bundle args = new Bundle();
                args.putString(IaAlertsFragment.ARG_INITIAL_TAB, IaAlertsFragment.TAB_ALERTS);
                NavHostFragment.findNavController(this).navigate(R.id.iaAlertsFragment, args);
            }
        });
        new UserRepository().listTeam(session.getStoreId(), new UserRepository.TeamCallback() {
            @Override public void onSuccess(List<User> users) {
                if (binding == null) return;
                binding.progressTeam.setVisibility(View.GONE);
                adapter.submit(users);
                binding.tvTeamState.setText(R.string.op_team_empty);
                binding.tvTeamState.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void onError(Exception error) {
                if (binding == null) return;
                binding.progressTeam.setVisibility(View.GONE);
                binding.tvTeamState.setText(R.string.op_team_error);
                binding.tvTeamState.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override public void onDestroyView() {
        binding = null;
        adapter = null;
        super.onDestroyView();
    }
}
