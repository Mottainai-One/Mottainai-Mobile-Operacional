package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.FragmentEditMemberBinding;

/**
 * Edição de um membro da equipe. Sem endpoint de equipe ainda — "Salvar" e
 * "Remover" avisam que estão pendentes, mesmo padrão do resto do app.
 */
public class EditMemberFragment extends Fragment {

    private FragmentEditMemberBinding binding;

    private static final String[] ROLES = {"Estoquista", "Gerente", "Dono"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentEditMemberBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String name = args != null ? args.getString("member_name") : null;
        String role = args != null ? args.getString("member_role") : null;

        ((TextView) binding.tvMemberName).setText(name != null ? name : "");

        Spinner spinner = binding.spinnerMemberRole;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, ROLES);
        spinner.setAdapter(adapter);
        if (role != null) {
            for (int i = 0; i < ROLES.length; i++) {
                if (ROLES[i].equalsIgnoreCase(role)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }

        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        binding.btnSaveMember.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Salvar membro — pendente (aguarda endpoint)", Toast.LENGTH_SHORT).show());

        binding.btnRemoveMember.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Remover membro — pendente (aguarda endpoint)", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
