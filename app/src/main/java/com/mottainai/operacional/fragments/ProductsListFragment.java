package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;
import com.mottainai.operacional.databinding.FragmentProductsListBinding;
import com.mottainai.operacional.viewmodels.ProductListViewModel;
import com.mottainai.operacional.R;

/**
 * Seção Produtos. As três páginas compartilham o mesmo cabeçalho e as mesmas
 * abas, para que alternar Lista, Inventário e Fornecedores não crie telas
 * independentes no back stack.
 */
public class ProductsListFragment extends Fragment {

    public static final String ARG_INITIAL_TAB = "initial_tab";
    private static final String STATE_SELECTED_TAB = "selected_tab";
    private static final String[] SECTION_TITLES = {
            "Lista", "Inventário", "Fornecedores"
    };

    private FragmentProductsListBinding binding;
    private TabLayoutMediator tabMediator;
    private int selectedTab;
    private androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback pageChangeCallback;
    private ProductListViewModel productListViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProductsListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectedTab = savedInstanceState == null
                ? (getArguments() == null ? 0 : getArguments().getInt(ARG_INITIAL_TAB, 0))
                : savedInstanceState.getInt(STATE_SELECTED_TAB, 0);
        selectedTab = Math.max(0, Math.min(selectedTab, SECTION_TITLES.length - 1));

        productListViewModel = new ViewModelProvider(this).get(ProductListViewModel.class);
        binding.svProductSearch.setIconified(false);
        binding.svProductSearch.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String query) {
                productListViewModel.setSearchQuery(query);
                return true;
            }
        });
        android.widget.EditText searchInput = binding.svProductSearch
                .findViewById(androidx.appcompat.R.id.search_src_text);
        searchInput.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_search, 0, 0, 0);
        searchInput.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.search_icon_padding));
        searchInput.setHintTextColor(requireContext().getColor(R.color.op_muted));

        binding.productsPager.setAdapter(new ProductsSectionAdapter(this));
        binding.productsPager.setOffscreenPageLimit(2);
        tabMediator = new TabLayoutMediator(binding.tabProducts, binding.productsPager,
                (tab, position) -> tab.setText(SECTION_TITLES[position]));
        tabMediator.attach();
        binding.productsPager.setCurrentItem(selectedTab, false);
        pageChangeCallback = new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (selectedTab != position) {
                    binding.svProductSearch.setQuery("", false);
                    productListViewModel.setSearchQuery("");
                }
                selectedTab = position;
                binding.svProductSearch.setQueryHint(getString(position == 1
                        ? R.string.op_search_inventory : position == 2
                        ? R.string.op_search_supplier : R.string.op_search_products));
                binding.productsSectionTitle.setText(position == 0 ? R.string.op_catalog_title
                        : position == 1 ? R.string.op_inventory_title : R.string.op_suppliers_title);
            }
        };
        binding.productsPager.registerOnPageChangeCallback(pageChangeCallback);
        binding.svProductSearch.setQueryHint(getString(selectedTab == 1
                ? R.string.op_search_inventory : selectedTab == 2
                ? R.string.op_search_supplier : R.string.op_search_products));
        binding.productsSectionTitle.setText(selectedTab == 0 ? R.string.op_catalog_title
                : selectedTab == 1 ? R.string.op_inventory_title : R.string.op_suppliers_title);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(STATE_SELECTED_TAB, selectedTab);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        if (pageChangeCallback != null) {
            binding.productsPager.unregisterOnPageChangeCallback(pageChangeCallback);
            pageChangeCallback = null;
        }
        if (tabMediator != null) {
            tabMediator.detach();
            tabMediator = null;
        }
        binding.productsPager.setAdapter(null);
        binding = null;
        super.onDestroyView();
    }

    private static class ProductsSectionAdapter extends FragmentStateAdapter {

        ProductsSectionAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ProductCatalogFragment();
                case 1:
                    return new InventoryCatalogFragment();
                case 2:
                    return new SuppliersFragment();
                default:
                    throw new IllegalArgumentException("Unknown products section: " + position);
            }
        }

        @Override
        public int getItemCount() {
            return SECTION_TITLES.length;
        }
    }
}
