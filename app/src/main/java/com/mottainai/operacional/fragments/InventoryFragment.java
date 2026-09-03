package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.operacional.R;
import com.mottainai.operacional.adapters.InventoryAdapter;
import com.mottainai.operacional.models.InventoryItem;

import java.util.Arrays;
import java.util.List;

/**
 * Contagem de inventário. Sem endpoint ainda (o mesmo "aguarda endpoint" que
 * já estava no placeholder da aba, em ProductsListFragment.setupTabs) — os
 * itens abaixo são ilustrativos, no espírito do MockProductRepository.
 */
public class InventoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        List<InventoryItem> items = Arrays.asList(
                new InventoryItem("Arroz integral 1kg", 48, 48),
                new InventoryItem("Feijão carioca 1kg", 30, 28),
                new InventoryItem("Óleo de soja 900ml", 20, 20),
                new InventoryItem("Macarrão espaguete 500g", 40, 36));

        long counted = items.stream().filter(i -> i.getCounted() > 0).count();
        ((TextView) view.findViewById(R.id.tv_progress_label))
                .setText(counted + " de " + items.size() + " itens conferidos");
        ProgressBar progressBar = view.findViewById(R.id.progress_inventory);
        progressBar.setProgress((int) (100.0 * counted / items.size()));

        RecyclerView recyclerView = view.findViewById(R.id.rv_inventory);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        InventoryAdapter adapter = new InventoryAdapter();
        adapter.setItems(items);
        recyclerView.setAdapter(adapter);
    }
}
