package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.mottainai.operacional.R;

/**
 * Registro de avaria de um produto, aberto a partir do scanner
 * (ScannerFragment.openDamageRegistration). Sem endpoint /api/v1/damage ainda
 * (mesmo TODO MOBILE-06 que já estava lá e em ProductDetailActivity) — ao
 * enviar, segue direto para a tela de sucesso, no mesmo espírito que o resto
 * do fluxo do scanner usa para simular o que a API real faria.
 */
public class RegisterDamageFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_damage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String productName = args != null ? args.getString("product_name") : null;
        String productSku = args != null ? args.getString("product_sku") : null;
        if (productName == null) productName = "Produto";

        ((TextView) view.findViewById(R.id.tv_product_name)).setText(productName);
        TextView tvSku = view.findViewById(R.id.tv_product_sku);
        if (productSku != null) {
            tvSku.setText("SKU " + productSku);
        } else {
            tvSku.setVisibility(View.GONE);
        }

        NavController navController = Navigation.findNavController(view);
        view.findViewById(R.id.btn_back).setOnClickListener(v -> navController.popBackStack());

        String finalProductName = productName;
        EditText etQuantity = view.findViewById(R.id.et_damage_quantity);
        view.findViewById(R.id.btn_submit_damage).setOnClickListener(v -> {
            String quantity = etQuantity.getText().toString().trim();
            if (quantity.isEmpty()) quantity = "1";

            Bundle successArgs = new Bundle();
            successArgs.putString("product_name", finalProductName);
            successArgs.putString("damage_quantity", quantity);
            navController.navigate(R.id.action_registerDamageFragment_to_damageSuccessFragment, successArgs);
        });
    }
}
