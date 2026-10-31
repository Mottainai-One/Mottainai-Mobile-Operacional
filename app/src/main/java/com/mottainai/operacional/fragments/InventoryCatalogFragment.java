package com.mottainai.operacional.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mottainai.operacional.R;
import com.mottainai.operacional.activities.ProductDetailActivity;
import com.mottainai.operacional.adapters.StockProductAdapter;
import com.mottainai.operacional.databinding.FragmentInventoryCatalogBinding;
import com.mottainai.operacional.models.Product;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.viewmodels.ProductListViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Consulta de saldos; a contagem local existente continua em sua própria etapa. */
public class InventoryCatalogFragment extends Fragment {
    private FragmentInventoryCatalogBinding binding;
    private ProductListViewModel viewModel;
    private StockProductAdapter adapter;
    private int filter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentInventoryCatalogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(ProductListViewModel.class);
        adapter = new StockProductAdapter(product -> {
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        binding.rvStock.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvStock.setAdapter(adapter);
        TextView[] chips = {binding.filterAll, binding.filterLow,
                binding.filterCritical, binding.filterOut};
        for (int i = 0; i < chips.length; i++) {
            final int selected = i;
            chips[i].setOnClickListener(v -> {
                filter = selected;
                renderFilters(chips);
                applyFilter();
            });
        }
        renderFilters(chips);
        binding.btnCount.setOnClickListener(v -> Navigation.findNavController(view)
                .navigate(R.id.countInventoryFragment));
        binding.btnLevels.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.op_understand_levels)
                .setMessage(R.string.op_levels_explanation)
                .setPositiveButton(android.R.string.ok, null)
                .show());
        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> applyFilter());
        viewModel.getSearchQuery().observe(getViewLifecycleOwner(), query -> applyFilter());
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progressStock.setVisibility(Boolean.TRUE.equals(loading)
                        ? View.VISIBLE : View.GONE));
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                binding.tvStockEmpty.setText(error);
                binding.tvStockEmpty.setVisibility(View.VISIBLE);
            }
        });
        if (viewModel.getProducts().getValue() == null
                && !Boolean.TRUE.equals(viewModel.getLoading().getValue())) {
            String storeId = new SessionManager(requireContext()).getStoreId();
            if (storeId != null) viewModel.loadProducts(storeId);
        }
    }

    private void renderFilters(TextView[] chips) {
        for (int i = 0; i < chips.length; i++) {
            chips[i].setBackgroundResource(i == filter
                    ? R.drawable.op_filter_selected : android.R.color.transparent);
            chips[i].setTextColor(ContextCompat.getColor(requireContext(),
                    i == filter ? R.color.op_link : R.color.op_muted));
        }
    }

    private void applyFilter() {
        if (binding == null || adapter == null || viewModel == null) return;
        List<Product> source = viewModel.getProducts().getValue();
        String query = viewModel.getSearchQuery().getValue();
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<Product> shown = new ArrayList<>();
        if (source != null) {
            for (Product product : source) {
                int quantity = product.getQuantity();
                int min = product.getMinQuantity();
                boolean out = quantity <= 0;
                boolean critical = !out && min > 0 && quantity <= min / 2;
                boolean low = !out && !critical && product.isLowStock();
                if (filter == 1 && !low) continue;
                if (filter == 2 && !critical) continue;
                if (filter == 3 && !out) continue;
                String name = product.getName() == null ? "" : product.getName();
                String sku = product.getSku() == null ? "" : product.getSku();
                if (!normalized.isEmpty() && !name.toLowerCase(Locale.ROOT).contains(normalized)
                        && !sku.toLowerCase(Locale.ROOT).contains(normalized)) continue;
                shown.add(product);
            }
        }
        adapter.submit(shown);
        binding.tvStockEmpty.setText(R.string.op_empty_inventory);
        binding.tvStockEmpty.setVisibility(source != null && shown.isEmpty()
                ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
