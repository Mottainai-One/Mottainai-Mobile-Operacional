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
 * Cadastro de fornecedor. Sem endpoint ainda — "Salvar" avisa que está
 * pendente, mesmo padrão do resto do app.
 */
public class NewSupplierFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_supplier, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        EditText etName = view.findViewById(R.id.et_supplier_name);
        view.findViewById(R.id.btn_save_supplier).setOnClickListener(v -> {
            if (etName.getText().toString().trim().isEmpty()) {
                etName.setError("Informe o nome da empresa");
                return;
            }
            Toast.makeText(requireContext(), "Fornecedor — pendente (aguarda contrato)", Toast.LENGTH_SHORT).show();
        });
    }
}
