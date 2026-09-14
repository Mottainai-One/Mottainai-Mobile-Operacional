package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.mottainai.operacional.R;

/**
 * Conteúdo da aba Fornecedores. Sem endpoint ainda, portanto os fornecedores
 * abaixo são ilustrativos.
 */
public class SuppliersFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suppliers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = NavHostFragment.findNavController(this);

        view.findViewById(R.id.btn_new_supplier).setOnClickListener(v ->
                navController.navigate(R.id.newSupplierFragment));

        view.findViewById(R.id.cv_supplier_1).setOnClickListener(v ->
                navController.navigate(R.id.editSupplierFragment,
                        supplierArgs("Cooperativa Verde")));

        view.findViewById(R.id.cv_supplier_2).setOnClickListener(v ->
                navController.navigate(R.id.editSupplierFragment,
                        supplierArgs("Distribuidora Circular")));
    }

    private static Bundle supplierArgs(String name) {
        Bundle args = new Bundle();
        args.putString("supplier_name", name);
        return args;
    }
}
