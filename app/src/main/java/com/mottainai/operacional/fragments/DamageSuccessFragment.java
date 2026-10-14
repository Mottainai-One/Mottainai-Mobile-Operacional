package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.FragmentDamageSuccessBinding;

/**
 * Confirmação após registrar uma avaria. O back stack já foi limpo até o
 * scannerFragment pela ação de navegação (ver nav_graph), então "Voltar para
 * câmera" é só um popBackStack.
 */
public class DamageSuccessFragment extends Fragment {

    private FragmentDamageSuccessBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentDamageSuccessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String productName = args != null ? args.getString("product_name") : null;
        String quantity = args != null ? args.getString("damage_quantity") : null;

        ((TextView) binding.tvProductName).setText(productName != null ? productName : "Produto");
        ((TextView) binding.tvDamageSummary)
                .setText((quantity != null ? quantity : "1") + " unidade(s) - registrado agora");

        NavController navController = Navigation.findNavController(view);

        binding.btnBackToScanner.setOnClickListener(v -> navController.popBackStack());

        binding.btnGoToProducts.setOnClickListener(v -> {
            try {
                navController.navigate(R.id.productsListFragment);
            } catch (IllegalArgumentException e) {
                navController.popBackStack();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
