package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.FragmentSupplierDetailBinding;
import com.mottainai.operacional.models.Supplier;
import com.mottainai.operacional.repository.MockSupplierRepository;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.viewmodels.SupplierViewModel;

/** Consulta de fornecedor com acesso à edição local para perfis autorizados. */
public class SupplierDetailFragment extends Fragment {
    private FragmentSupplierDetailBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSupplierDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String supplierId = requireArguments().getString("supplier_id");
        if (supplierId == null) {
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }
        SessionManager session = new SessionManager(requireContext());
        boolean canManage = RoleHelper.canRegisterProduct(session.getRole());
        binding.btnEditSupplier.setVisibility(canManage ? View.VISIBLE : View.GONE);
        binding.btnBackToSuppliers.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.btnEditSupplier.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("supplier_id", supplierId);
            NavHostFragment.findNavController(this).navigate(R.id.editSupplierFragment, args);
        });
        SupplierViewModel viewModel = new ViewModelProvider(requireActivity()).get(SupplierViewModel.class);
        viewModel.find(session.getStoreId(), supplierId, new MockSupplierRepository.Callback<Supplier>() {
            @Override public void onSuccess(Supplier supplier) {
                if (binding == null) return;
                binding.tvSupplierName.setText(supplier.getTradeName());
                binding.tvSupplierContact.setText(supplier.getContact().isEmpty()
                        ? getString(R.string.op_supplier_address_unavailable) : supplier.getContact());
                binding.tvSupplierCnpj.setText(getString(R.string.op_supplier_cnpj, supplier.getCnpj()));
            }
            @Override public void onError(String message) {
                if (isAdded()) NavHostFragment.findNavController(SupplierDetailFragment.this).navigateUp();
            }
        });
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
