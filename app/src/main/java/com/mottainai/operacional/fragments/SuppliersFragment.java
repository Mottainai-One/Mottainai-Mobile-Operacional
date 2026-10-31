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
import com.mottainai.operacional.viewmodels.ProductListViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.mottainai.operacional.models.Supplier;

/**
 * Conteúdo da aba Fornecedores em modo local por loja.
 */
public class SuppliersFragment extends Fragment {

    private FragmentSuppliersBinding binding;
    private List<Supplier> allSuppliers = new ArrayList<>();
    private SupplierAdapter adapter;
    private String query = "";

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
        adapter = new SupplierAdapter(supplier -> {
            Bundle args = new Bundle();
            args.putString("supplier_id", supplier.getId());
            NavHostFragment.findNavController(this).navigate(R.id.supplierDetailFragment, args);
        });
        RecyclerView list = binding.rvSuppliers;
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
        SupplierViewModel viewModel = new ViewModelProvider(requireActivity()).get(SupplierViewModel.class);
        viewModel.getSuppliers().observe(getViewLifecycleOwner(), suppliers -> {
            allSuppliers = suppliers == null ? new ArrayList<>() : suppliers;
            applySearch();
        });
        ProductListViewModel searchModel = new ViewModelProvider(requireParentFragment())
                .get(ProductListViewModel.class);
        searchModel.getSearchQuery().observe(getViewLifecycleOwner(), value -> {
            query = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
            applySearch();
        });
        viewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
        View add = binding.btnNewSupplier;
        add.setVisibility(canManage ? View.VISIBLE : View.GONE);
        add.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.newSupplierFragment));
        viewModel.load(storeId);
    }

    private void applySearch() {
        if (binding == null || adapter == null) return;
        List<Supplier> filtered = new ArrayList<>();
        for (Supplier supplier : allSuppliers) {
            if (supplier.getTradeName().toLowerCase(Locale.ROOT).contains(query)
                    || supplier.getCnpj().contains(query)
                    || supplier.getContact().toLowerCase(Locale.ROOT).contains(query)) {
                filtered.add(supplier);
            }
        }
        adapter.submit(filtered);
        binding.tvSuppliersEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        binding.tvSuppliersEmpty.setText(allSuppliers.isEmpty()
                ? R.string.op_supplier_empty : R.string.op_supplier_search_empty);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
