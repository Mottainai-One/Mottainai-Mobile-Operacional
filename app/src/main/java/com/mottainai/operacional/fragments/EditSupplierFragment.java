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
 * Edição de fornecedor. Sem endpoint ainda — "Salvar" e "Remover" avisam que
 * estão pendentes, mesmo padrão do resto do app.
 */
public class EditSupplierFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_supplier, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String name = args != null ? args.getString("supplier_name") : null;
        ((EditText) view.findViewById(R.id.et_supplier_name)).setText(name);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        view.findViewById(R.id.btn_save_supplier).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Fornecedor — pendente (aguarda contrato)", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_remove_supplier).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Remover fornecedor — pendente (aguarda contrato)", Toast.LENGTH_SHORT).show());
    }
}
