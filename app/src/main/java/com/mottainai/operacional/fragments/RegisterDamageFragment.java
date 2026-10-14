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
import com.mottainai.operacional.databinding.FragmentRegisterDamageBinding;

/**
 * Registro de avaria de um produto, aberto a partir do scanner
 * (ScannerFragment.openDamageRegistration). Sem endpoint /api/v1/damage ainda
 * (mesmo TODO MOBILE-06 que já estava lá e em ProductDetailActivity) — ao
 * enviar, segue direto para a tela de sucesso, no mesmo espírito que o resto
 * do fluxo do scanner usa para simular o que a API real faria.
 */
public class RegisterDamageFragment extends Fragment {

    private FragmentRegisterDamageBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterDamageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String productName = args != null ? args.getString("product_name") : null;
        String productSku = args != null ? args.getString("product_sku") : null;
        if (productName == null) productName = getString(R.string.damage_product_fallback);

        binding.tvProductName.setText(productName);
        TextView tvSku = binding.tvProductSku;
        if (productSku != null) {
            tvSku.setText(getString(R.string.damage_product_sku, productSku));
        } else {
            tvSku.setVisibility(View.GONE);
        }

        NavController navController = Navigation.findNavController(view);
        binding.btnBack.setOnClickListener(v -> navController.popBackStack());

        MaterialAutoCompleteTextView actvReason = binding.actvDamageReason;
        ArrayAdapter<String> reasonAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.damage_reasons));
        actvReason.setAdapter(reasonAdapter);
        TextView tvErrorReason = binding.tvErrorReason;

        EditText etQuantity = binding.etDamageQuantity;
        TextView tvErrorQuantity = binding.tvErrorQuantity;

        String finalProductName = productName;
        binding.btnSubmitDamage.setOnClickListener(v -> {
            String reason = actvReason.getText() != null ? actvReason.getText().toString().trim() : "";
            if (reason.isEmpty()) {
                tvErrorReason.setText(R.string.damage_reason_required);
                tvErrorReason.setVisibility(View.VISIBLE);
                return;
            }
            tvErrorReason.setVisibility(View.GONE);

            int quantity;
            try {
                quantity = Integer.parseInt(etQuantity.getText().toString().trim());
            } catch (NumberFormatException e) {
                tvErrorQuantity.setText(R.string.damage_quantity_invalid);
                tvErrorQuantity.setVisibility(View.VISIBLE);
                return;
            }
            if (quantity <= 0) {
                tvErrorQuantity.setText(R.string.damage_quantity_positive);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
