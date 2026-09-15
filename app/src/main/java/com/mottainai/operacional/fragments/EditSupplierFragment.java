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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.mottainai.operacional.R;
import com.mottainai.operacional.repository.MockSupplierRepository;
import com.mottainai.operacional.models.Supplier;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.viewmodels.SupplierViewModel;

/**
 * Edição e remoção local de fornecedor enquanto a API relacional não está ativa no app.
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
        String supplierId = args == null ? null : args.getString("supplier_id");
        EditText etName = view.findViewById(R.id.et_supplier_name);
        EditText etCnpj = view.findViewById(R.id.et_supplier_cnpj);
        EditText etContact = view.findViewById(R.id.et_supplier_contact);
        SupplierViewModel viewModel = new ViewModelProvider(requireActivity()).get(SupplierViewModel.class);
        SessionManager session = new SessionManager(requireContext());
        viewModel.clearCompletion();
        if (supplierId == null) { Navigation.findNavController(view).popBackStack(); return; }
        viewModel.find(session.getStoreId(), supplierId, new MockSupplierRepository.Callback<Supplier>() {
            @Override public void onSuccess(Supplier supplier) { etName.setText(supplier.getTradeName()); etCnpj.setText(supplier.getCnpj()); etContact.setText(supplier.getContact()); }
            @Override public void onError(String message) { Navigation.findNavController(view).popBackStack(); }
        });
        viewModel.getCompleted().observe(getViewLifecycleOwner(), completed -> { if (Boolean.TRUE.equals(completed)) Navigation.findNavController(view).popBackStack(); });

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        view.findViewById(R.id.btn_save_supplier).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String cnpj = etCnpj.getText().toString().replaceAll("\\D", "");
            if (name.isEmpty()) { etName.setError("Informe o nome da empresa"); return; }
            if (cnpj.length() != 14) { etCnpj.setError("Informe os 14 dígitos do CNPJ"); return; }
            viewModel.update(session.getStoreId(), supplierId, name, cnpj, etContact.getText().toString().trim());
        });

        view.findViewById(R.id.btn_remove_supplier).setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Remover fornecedor?").setMessage("Esta ação remove o fornecedor salvo neste dispositivo.")
                .setNegativeButton("Cancelar", null).setPositiveButton("Remover", (dialog, which) -> viewModel.delete(session.getStoreId(), supplierId)).show());
    }
}
