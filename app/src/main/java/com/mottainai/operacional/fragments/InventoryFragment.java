package com.mottainai.operacional.fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.mottainai.operacional.R;
import com.mottainai.operacional.adapters.InventoryAdapter;
import com.mottainai.operacional.models.InventorySession;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.viewmodels.InventoryViewModel;

/** Conteúdo da aba Inventário no modo de demonstração local. */
public class InventoryFragment extends Fragment {

    private InventoryViewModel viewModel;
    private InventoryAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressInventory;
    private View progressLoading;
    private View inventoryHeader;
    private TextView tvState;
    private TextView tvMeta;
    private TextView tvProgress;
    private TextView tvInfo;
    private View cardDivergences;
    private TextView tvDivergenceTitle;
    private TextView tvDivergenceMessage;
    private View btnStart;
    private View btnFinish;
    private String storeId;
    private String userName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager sessionManager = new SessionManager(requireContext());
        storeId = sessionManager.getStoreId();
        userName = sessionManager.getName();

        progressInventory = view.findViewById(R.id.progress_inventory);
        progressLoading = view.findViewById(R.id.progress_inventory_loading);
        inventoryHeader = view.findViewById(R.id.inventory_header);
        tvState = view.findViewById(R.id.tv_inventory_state);
        tvMeta = view.findViewById(R.id.tv_inventory_meta);
        tvProgress = view.findViewById(R.id.tv_progress_label);
        tvInfo = view.findViewById(R.id.tv_inventory_info);
        cardDivergences = view.findViewById(R.id.card_inventory_divergences);
        tvDivergenceTitle = view.findViewById(R.id.tv_inventory_divergence_title);
        tvDivergenceMessage = view.findViewById(R.id.tv_inventory_divergence_message);
        btnStart = view.findViewById(R.id.btn_inventory_start);
        btnFinish = view.findViewById(R.id.btn_inventory_finish);

        recyclerView = view.findViewById(R.id.rv_inventory);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(false);
        adapter = new InventoryAdapter(new InventoryAdapter.Listener() {
            @Override
            public void onCountChanged(String productId, Integer quantity) {
                viewModel.updateCount(productId, quantity);
            }

            @Override
            public void onObservationChanged(String productId, String observation) {
                viewModel.updateObservation(productId, observation);
            }
        });
        recyclerView.setAdapter(adapter);
        configureKeyboardHandling(view);

        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
        renderNotStarted();

        btnStart.setOnClickListener(v -> viewModel.start(storeId, userName));
        btnFinish.setOnClickListener(v -> requestFinish());

        viewModel.getSession().observe(getViewLifecycleOwner(), this::renderSession);
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading ->
                progressLoading.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE));
        viewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
            }
        });
        viewModel.load(storeId);
    }

    private void configureKeyboardHandling(View root) {
        final int defaultBottomPadding = recyclerView.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            boolean keyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            int desiredHeaderVisibility = keyboardVisible ? View.GONE : View.VISIBLE;
            if (inventoryHeader.getVisibility() != desiredHeaderVisibility) {
                inventoryHeader.setVisibility(desiredHeaderVisibility);
            }

            int imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            int navigationBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            int keyboardPadding = keyboardVisible ? Math.max(0, imeBottom - navigationBottom) : 0;
            recyclerView.setPadding(
                    recyclerView.getPaddingLeft(),
                    recyclerView.getPaddingTop(),
                    recyclerView.getPaddingRight(),
                    defaultBottomPadding + keyboardPadding
            );

            if (keyboardVisible) {
                recyclerView.post(() -> revealFocusedField(root.findFocus()));
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void revealFocusedField(View focusedView) {
        if (focusedView == null || !isInsideRecyclerView(focusedView)) return;
        Rect fieldBounds = new Rect();
        focusedView.getDrawingRect(fieldBounds);
        focusedView.requestRectangleOnScreen(fieldBounds, true);
    }

    private boolean isInsideRecyclerView(View view) {
        ViewParent parent = view.getParent();
        while (parent != null) {
            if (parent == recyclerView) return true;
            parent = parent.getParent();
        }
        return false;
    }

    private void requestFinish() {
        InventorySession session = viewModel.getSession().getValue();
        if (session == null || !session.isComplete()) {
            viewModel.finish();
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Finalizar contagem de demonstração?")
                .setMessage("A edição ficará bloqueada nesta sessão. Nenhuma quantidade será "
                        + "enviada para a API até a integração de inventário estar disponível.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Finalizar", (dialog, which) -> viewModel.finish())
                .show();
    }

    private void renderSession(InventorySession session) {
        if (session == null) {
            renderNotStarted();
            return;
        }

        int counted = session.getCountedItems();
        int total = session.getItems().size();
        int progress = total == 0 ? 0 : Math.round((counted * 100f) / total);
        progressInventory.setVisibility(View.VISIBLE);
        progressInventory.setProgress(progress);
        tvProgress.setText(counted + " de " + total + " itens conferidos");
        tvMeta.setText("Iniciado por " + session.getStartedBy() + " em " + session.getStartedAt());
        renderDivergenceSummary(session);

        boolean finalized = session.getStatus() == InventorySession.Status.FINALIZED;
        if (finalized) {
            tvState.setText("Contagem finalizada");
            tvInfo.setText("Finalizado no modo demonstração. O estoque da API não foi alterado.");
            btnStart.setVisibility(View.GONE);
            btnFinish.setVisibility(View.GONE);
        } else {
            tvState.setText("Contagem em andamento");
            tvInfo.setText("Rascunho salvo somente enquanto o app estiver aberto.");
            btnStart.setVisibility(View.GONE);
            btnFinish.setVisibility(View.VISIBLE);
            ((TextView) btnFinish).setText("Finalizar inventário (" + counted + "/" + total + ")");
        }
        adapter.submitItems(session.getItems(), !finalized);
    }

    private void renderDivergenceSummary(InventorySession session) {
        int divergences = session.getDivergenceItems();
        if (divergences == 0) {
            cardDivergences.setVisibility(View.GONE);
            return;
        }

        int missing = session.getMissingItems();
        int excess = session.getExcessItems();
        String summary = "";
        if (missing > 0) summary = missing + (missing == 1 ? " item em falta" : " itens em falta");
        if (excess > 0) {
            if (!summary.isEmpty()) summary += " e ";
            summary += excess + (excess == 1 ? " item em excesso" : " itens em excesso");
        }

        tvDivergenceTitle.setText(divergences == 1
                ? "1 divergência exige revisão"
                : divergences + " divergências exigem revisão");
        tvDivergenceMessage.setText(summary
                + ". Registre uma observação em cada item antes de finalizar. "
                + "O aviso automático ao gerente depende da integração da API.");
        cardDivergences.setVisibility(View.VISIBLE);
    }

    private void renderNotStarted() {
        tvState.setText("Inventário não iniciado");
        tvMeta.setText("Inicie uma contagem para conferir o estoque físico.");
        tvProgress.setText("A contagem ainda não começou");
        tvInfo.setText("Os itens serão carregados dos dados mockados atuais do aplicativo.");
        cardDivergences.setVisibility(View.GONE);
        progressInventory.setVisibility(View.INVISIBLE);
        progressInventory.setProgress(0);
        btnStart.setVisibility(View.VISIBLE);
        btnFinish.setVisibility(View.GONE);
        if (adapter != null) adapter.submitItems(null, false);
    }
}
