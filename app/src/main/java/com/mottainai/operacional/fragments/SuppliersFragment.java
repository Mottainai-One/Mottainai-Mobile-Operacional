package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.FragmentSuppliersBinding;
import com.mottainai.operacional.adapters.SupplierAdapter;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.viewmodels.SupplierViewModel;

/**
 * Conteúdo da aba Fornecedores em modo local por loja.
 */
public class SuppliersFragment extends Fragment {

    private FragmentSuppliersBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentSuppliersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager session = new SessionManager(requireContext());
        String storeId = session.getStoreId();
        boolean canManage = RoleHelper.canRegisterProduct(session.getRole());
        SupplierAdapter adapter = new SupplierAdapter(supplier -> {
            if (!canManage) return;
            Bundle args = new Bundle();
            args.putString("supplier_id", supplier.getId());
            NavHostFragment.findNavController(this).navigate(R.id.editSupplierFragment, args);
        });
        RecyclerView list = binding.rvSuppliers;
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
        SupplierViewModel viewModel = new ViewModelProvider(requireActivity()).get(SupplierViewModel.class);
        viewModel.getSuppliers().observe(getViewLifecycleOwner(), suppliers -> {
            adapter.submit(suppliers);
            binding.tvSuppliersEmpty.setVisibility(suppliers == null || suppliers.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
        View add = binding.btnNewSupplier;
        add.setVisibility(canManage ? View.VISIBLE : View.GONE);
        add.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.newSupplierFragment));
        viewModel.load(storeId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
