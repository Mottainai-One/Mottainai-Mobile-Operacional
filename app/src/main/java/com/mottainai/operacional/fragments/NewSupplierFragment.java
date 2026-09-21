package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.lifecycle.ViewModelProvider;

import com.mottainai.operacional.R;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.utils.ValidationUtils;
import com.mottainai.operacional.viewmodels.SupplierViewModel;

/**
 * Cadastro local de fornecedor enquanto a API relacional não está ativa no app.
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
        EditText etCnpj = view.findViewById(R.id.et_supplier_cnpj);
        EditText etContact = view.findViewById(R.id.et_supplier_contact);
        SupplierViewModel viewModel = new ViewModelProvider(requireActivity()).get(SupplierViewModel.class);
        SessionManager session = new SessionManager(requireContext());
        viewModel.clearCompletion();
        viewModel.getCompleted().observe(getViewLifecycleOwner(), completed -> {
            if (Boolean.TRUE.equals(completed)) Navigation.findNavController(view).popBackStack();
        });
        view.findViewById(R.id.btn_save_supplier).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String cnpj = etCnpj.getText().toString().replaceAll("\\D", "");
            String contact = etContact.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError("Informe o nome da empresa");
                return;
            }
            if (!ValidationUtils.isValidCnpj(cnpj)) {
                etCnpj.setError("CNPJ inválido");
                return;
            }
            if (!contact.isEmpty() && !ValidationUtils.isValidEmail(contact)) {
                etContact.setError("Informe um e-mail válido");
                return;
            }
            viewModel.create(session.getStoreId(), name, cnpj, contact);
        });
    }
}
