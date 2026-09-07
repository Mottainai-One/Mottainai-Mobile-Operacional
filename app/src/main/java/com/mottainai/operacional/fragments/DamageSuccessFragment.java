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

/**
 * Confirmação após registrar uma avaria. O back stack já foi limpo até o
 * scannerFragment pela ação de navegação (ver nav_graph), então "Voltar para
 * câmera" é só um popBackStack.
 */
public class DamageSuccessFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_damage_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String productName = args != null ? args.getString("product_name") : null;
        String quantity = args != null ? args.getString("damage_quantity") : null;

        ((TextView) view.findViewById(R.id.tv_product_name)).setText(productName != null ? productName : "Produto");
        ((TextView) view.findViewById(R.id.tv_damage_summary))
                .setText((quantity != null ? quantity : "1") + " unidade(s) - registrado agora");

        NavController navController = Navigation.findNavController(view);

        view.findViewById(R.id.btn_back_to_scanner).setOnClickListener(v -> navController.popBackStack());

        view.findViewById(R.id.btn_go_to_products).setOnClickListener(v -> {
            try {
                navController.navigate(R.id.productsListFragment);
            } catch (IllegalArgumentException e) {
                navController.popBackStack();
            }
        });
    }
}
