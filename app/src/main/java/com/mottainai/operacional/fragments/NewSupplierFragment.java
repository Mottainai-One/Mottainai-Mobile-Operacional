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
import androidx.lifecycle.ViewModelProvider;

import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.FragmentSupplierFormBinding;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.utils.ValidationUtils;
import com.mottainai.operacional.viewmodels.SupplierViewModel;

/**
 * Cadastro local de fornecedor enquanto a API relacional não está ativa no app.
 */
public class NewSupplierFragment extends Fragment {

    private FragmentSupplierFormBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentSupplierFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnCancel.setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        EditText etName = binding.etSupplierName;
        EditText etCnpj = binding.etSupplierCnpj;
        EditText etContact = binding.etSupplierContact;
        SupplierViewModel viewModel = new ViewModelProvider(requireActivity()).get(SupplierViewModel.class);
        SessionManager session = new SessionManager(requireContext());
        viewModel.clearCompletion();
        viewModel.getCompleted().observe(getViewLifecycleOwner(), completed -> {
            if (Boolean.TRUE.equals(completed)) {
                viewModel.clearCompletion();
                Navigation.findNavController(view).navigate(R.id.supplierSavedFragment);
            }
        });
        viewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
        binding.btnSaveSupplier.setOnClickListener(v -> {
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
            if (!ValidationUtils.isValidSupplierContact(contact)) {
                etContact.setError(getString(R.string.op_supplier_invalid_contact));
                return;
            }
            viewModel.create(session.getStoreId(), name, cnpj, contact);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
