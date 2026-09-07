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

/**
 * Edição de um membro da equipe. Sem endpoint de equipe ainda — "Salvar" e
 * "Remover" avisam que estão pendentes, mesmo padrão do resto do app.
 */
public class EditMemberFragment extends Fragment {

    private static final String[] ROLES = {"Estoquista", "Gerente", "Dono"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_member, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String name = args != null ? args.getString("member_name") : null;
        String role = args != null ? args.getString("member_role") : null;

        ((TextView) view.findViewById(R.id.tv_member_name)).setText(name != null ? name : "");

        Spinner spinner = view.findViewById(R.id.spinner_member_role);
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

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        view.findViewById(R.id.btn_save_member).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Salvar membro — pendente (aguarda endpoint)", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_remove_member).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Remover membro — pendente (aguarda endpoint)", Toast.LENGTH_SHORT).show());
    }
}
