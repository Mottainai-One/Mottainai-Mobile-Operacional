package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.FragmentStockTasksBinding;
import com.mottainai.operacional.utils.Constants;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;

/** Tarefas do estoquista; a fila externa ainda não é fornecida ao aplicativo. */
public class StockTasksFragment extends Fragment {
    private FragmentStockTasksBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStockTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String role = RoleHelper.normalize(new SessionManager(requireContext()).getRole());
        if (!Constants.ROLE_ESTOQUISTA.equals(role)) {
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }
        binding.btnStartLocalCount.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.countInventoryFragment));
        binding.btnOpenReplenishment.setOnClickListener(v -> openWorkflow(
                R.id.replenishmentFragment, OperationalWorkflowFragment.TYPE_REPLENISHMENT));
        binding.btnOpenReceiving.setOnClickListener(v -> openWorkflow(
                R.id.receivingWorkflowFragment, OperationalWorkflowFragment.TYPE_RECEIVING));
        binding.btnOpenInventory.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt(ProductsListFragment.ARG_INITIAL_TAB, 1);
            NavHostFragment.findNavController(this).navigate(R.id.productsListFragment, args);
        });
    }

    private void openWorkflow(int destination, String type) {
        Bundle args = new Bundle();
        args.putString(OperationalWorkflowFragment.ARG_WORKFLOW_TYPE, type);
        NavHostFragment.findNavController(this).navigate(destination, args);
    }

    @Override public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
