package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Manager review screen backed by the store-scoped Firestore approval queue. */
public class PosApprovalsFragment extends Fragment {

    private ListenerRegistration listener;
    private LinearLayout requestList;
    private TextView emptyState;
    private SessionManager session;

    public PosApprovalsFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        session = new SessionManager(requireContext());
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(16);
        root.setPadding(padding, padding, padding, padding);

        TextView title = new TextView(requireContext());
        title.setText("Pedidos aguardando aprovação");
        title.setTextSize(20);
        title.setGravity(Gravity.START);
        root.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        emptyState = new TextView(requireContext());
        emptyState.setText("Carregando pedidos...");
        emptyState.setPadding(0, dp(18), 0, dp(18));
        root.addView(emptyState);

        ScrollView scroll = new ScrollView(requireContext());
        requestList = new LinearLayout(requireContext());
        requestList.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(requestList);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        if (!RoleHelper.canApprovePosRequests(session.getRole())) {
            emptyState.setText("Seu perfil não pode aprovar pedidos do PDV.");
            return root;
        }
        listenForRequests();
        return root;
    }

    private void listenForRequests() {
        if (session.getStoreKey() == null || session.getStoreKey().trim().isEmpty()) {
            emptyState.setText("Não foi possível identificar a loja desta sessão.");
            return;
        }
        listener = FirebaseFirestore.getInstance().collection("posApprovals")
                .whereEqualTo("storeKey", session.getStoreKey())
                .whereEqualTo("status", "PENDING")
                .addSnapshotListener((snapshot, error) -> {
                    if (!isAdded()) return;
                    if (error != null) {
                        emptyState.setText("Não foi possível carregar os pedidos agora.");
                        return;
                    }
                    render(snapshot);
                });
    }

    private void render(QuerySnapshot snapshot) {
        requestList.removeAllViews();
        if (snapshot == null || snapshot.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            emptyState.setText("Não há pedidos aguardando aprovação.");
            return;
        }
        emptyState.setVisibility(View.GONE);
        for (DocumentSnapshot request : snapshot.getDocuments()) {
            requestList.addView(createRequestCard(request));
        }
    }

    private View createRequestCard(DocumentSnapshot request) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackgroundResource(com.mottainai.operacional.R.drawable.bg_summary_card);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);

        String type = request.getString("requestType");
        Integer saleId = numberAsInteger(request.get("saleId"));
        Integer saleItemId = numberAsInteger(request.get("saleItemId"));
        addText(card, labelFor(type), 17, true);
        addText(card, saleItemId == null
                ? "Venda #" + (saleId == null ? "—" : saleId)
                : "Venda #" + (saleId == null ? "—" : saleId) + " · Produto #" + saleItemId,
                14, false);

        Double amount = numberAsDouble(request.get("requestedAmount"));
        Double discount = numberAsDouble(request.get("discountPercent"));
        if (amount != null) addText(card, String.format(Locale.forLanguageTag("pt-BR"), "Valor: R$ %.2f", amount), 14, false);
        if (discount != null) addText(card, String.format(Locale.forLanguageTag("pt-BR"), "Desconto: %.2f%%", discount), 14, false);
        String reason = request.getString("reason");
        if (reason != null && !reason.trim().isEmpty()) addText(card, "Motivo: " + reason, 14, false);

        LinearLayout actions = new LinearLayout(requireContext());
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button reject = new Button(requireContext());
        reject.setText("Rejeitar");
        Button approve = new Button(requireContext());
        approve.setText("Aprovar");
        actions.addView(reject, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        actions.addView(approve, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        card.addView(actions);

        reject.setOnClickListener(view -> decide(request, "REJECTED", reject, approve));
        approve.setOnClickListener(view -> decide(request, "APPROVED", reject, approve));
        return card;
    }

    private void decide(DocumentSnapshot request, String status, Button reject, Button approve) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() == null
                ? null : FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (uid == null || !RoleHelper.canApprovePosRequests(session.getRole())) {
            Toast.makeText(requireContext(), "Entre novamente com um perfil gerente ou dono.", Toast.LENGTH_LONG).show();
            return;
        }
        reject.setEnabled(false);
        approve.setEnabled(false);
        Map<String, Object> decision = new HashMap<>();
        decision.put("status", status);
        decision.put("decisionUid", uid);
        decision.put("decisionObservation", "");
        decision.put("decisionAt", FieldValue.serverTimestamp());
        request.getReference().update(decision).addOnFailureListener(error -> {
            if (!isAdded()) return;
            reject.setEnabled(true);
            approve.setEnabled(true);
            Toast.makeText(requireContext(), "Não foi possível salvar a decisão.", Toast.LENGTH_LONG).show();
        });
    }

    private void addText(LinearLayout parent, String value, int size, boolean bold) {
        TextView text = new TextView(requireContext());
        text.setText(value);
        text.setTextSize(size);
        if (bold) text.setTypeface(text.getTypeface(), android.graphics.Typeface.BOLD);
        text.setPadding(0, 0, 0, dp(6));
        parent.addView(text, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private String labelFor(String type) {
        if (type == null) return "Pedido do PDV";
        switch (type) {
            case "CANCEL_PURCHASE": return "Cancelar compra";
            case "CANCEL_PRODUCT": return "Cancelar produto";
            case "REFUND": return "Estorno";
            case "RETURN": return "Devolução";
            case "SPECIAL_DISCOUNT": return "Desconto especial";
            default: return "Pedido do PDV";
        }
    }

    private Integer numberAsInteger(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    private Double numberAsDouble(Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : null;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        if (listener != null) listener.remove();
        listener = null;
        requestList = null;
        emptyState = null;
        super.onDestroyView();
    }
}
