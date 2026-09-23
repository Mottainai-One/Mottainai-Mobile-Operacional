package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.mottainai.operacional.R;

/**
 * Registro de avaria de um produto, aberto a partir do scanner
 * (ScannerFragment.openDamageRegistration). Sem endpoint /api/v1/damage ainda
 * (mesmo TODO MOBILE-06 que já estava lá e em ProductDetailActivity) — ao
 * enviar, segue direto para a tela de sucesso, no mesmo espírito que o resto
 * do fluxo do scanner usa para simular o que a API real faria.
 */
public class RegisterDamageFragment extends Fragment {

    private static final String[] REASONS = {"Vencido", "Embalagem danificada", "Quebra", "Contaminação", "Outro"};

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

        MaterialAutoCompleteTextView actvReason = view.findViewById(R.id.actv_damage_reason);
        ArrayAdapter<String> reasonAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, REASONS);
        actvReason.setAdapter(reasonAdapter);
        TextView tvErrorReason = view.findViewById(R.id.tv_error_reason);

        EditText etQuantity = view.findViewById(R.id.et_damage_quantity);
        TextView tvErrorQuantity = view.findViewById(R.id.tv_error_quantity);

        String finalProductName = productName;
        view.findViewById(R.id.btn_submit_damage).setOnClickListener(v -> {
            String reason = actvReason.getText() != null ? actvReason.getText().toString().trim() : "";
            if (reason.isEmpty()) {
                tvErrorReason.setText("Selecione o motivo da avaria");
                tvErrorReason.setVisibility(View.VISIBLE);
                return;
            }
            tvErrorReason.setVisibility(View.GONE);

            int quantity;
            try {
                quantity = Integer.parseInt(etQuantity.getText().toString().trim());
            } catch (NumberFormatException e) {
                tvErrorQuantity.setText("Quantidade inválida");
                tvErrorQuantity.setVisibility(View.VISIBLE);
                return;
            }
            if (quantity <= 0) {
                tvErrorQuantity.setText("Deve ser maior que zero");
                tvErrorQuantity.setVisibility(View.VISIBLE);
                return;
            }
            tvErrorQuantity.setVisibility(View.GONE);

            Bundle successArgs = new Bundle();
            successArgs.putString("product_name", finalProductName);
            successArgs.putString("damage_quantity", String.valueOf(quantity));
            navController.navigate(R.id.action_registerDamageFragment_to_damageSuccessFragment, successArgs);
        });
    }
}
