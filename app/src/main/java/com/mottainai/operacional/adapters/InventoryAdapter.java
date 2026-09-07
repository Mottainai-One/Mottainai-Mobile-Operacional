package com.mottainai.operacional.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.mottainai.operacional.R;
import com.mottainai.operacional.models.InventoryItem;

import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    public interface Listener {
        void onCountChanged(String productId, Integer quantity);
        void onObservationChanged(String productId, String observation);
    }

    private final List<InventoryItem> items = new ArrayList<>();
    private final Listener listener;
    private boolean editable;

    public InventoryAdapter(Listener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void submitItems(List<InventoryItem> newItems, boolean canEdit) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        editable = canEdit;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        String id = items.get(position).getProductId();
        return id == null ? position : id.hashCode();
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        holder.bind(items.get(position), editable);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class InventoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvSku;
        private final TextView tvExpected;
        private final TextView tvVariance;
        private final TextView tvStatus;
        private final TextInputLayout tilCount;
        private final TextInputLayout tilObservation;
        private final EditText etCount;
        private final EditText etObservation;
        private final Listener listener;
        private InventoryItem boundItem;
        private final TextWatcher countWatcher;
        private final TextWatcher observationWatcher;

        InventoryViewHolder(@NonNull View itemView, Listener listener) {
            super(itemView);
            this.listener = listener;
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvSku = itemView.findViewById(R.id.tv_item_sku);
            tvExpected = itemView.findViewById(R.id.tv_expected_quantity);
            tvVariance = itemView.findViewById(R.id.tv_item_variance);
            tvStatus = itemView.findViewById(R.id.tv_item_status);
            tilCount = itemView.findViewById(R.id.til_physical_quantity);
            tilObservation = itemView.findViewById(R.id.til_observation);
            etCount = itemView.findViewById(R.id.et_physical_quantity);
            etObservation = itemView.findViewById(R.id.et_observation);

            countWatcher = new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                    if (boundItem == null || !etCount.hasFocus()) return;
                    Integer quantity = parseQuantity(editable.toString());
                    listener.onCountChanged(boundItem.getProductId(), quantity);
                    renderResult(boundItem.withCountedQuantity(quantity));
                }
            };
            observationWatcher = new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                    if (boundItem == null || !etObservation.hasFocus()) return;
                    listener.onObservationChanged(boundItem.getProductId(), editable.toString().trim());
                }
            };
            etCount.addTextChangedListener(countWatcher);
            etObservation.addTextChangedListener(observationWatcher);
        }

        void bind(InventoryItem item, boolean editable) {
            boundItem = item;
            tvName.setText(item.getName());
            tvSku.setText(item.getSku() == null || item.getSku().trim().isEmpty()
                    ? "Sem SKU" : "SKU: " + item.getSku());
            tvExpected.setText("Sistema\n" + item.getExpectedQuantity() + " un.");

            etCount.removeTextChangedListener(countWatcher);
            String countText = item.isCounted() ? String.valueOf(item.getCountedQuantity()) : "";
            if (!countText.contentEquals(etCount.getText())) etCount.setText(countText);
            etCount.addTextChangedListener(countWatcher);

            etObservation.removeTextChangedListener(observationWatcher);
            String observation = item.getObservation() == null ? "" : item.getObservation();
            if (!observation.contentEquals(etObservation.getText())) etObservation.setText(observation);
            etObservation.addTextChangedListener(observationWatcher);

            etCount.setEnabled(editable);
            etObservation.setEnabled(editable);
            tilCount.setEnabled(editable);
            tilObservation.setEnabled(editable);
            renderResult(item);
        }

        private void renderResult(InventoryItem item) {
            tilObservation.setError(null);
            if (!item.isCounted()) {
                setStatus("Pendente", "Aguardando contagem", R.drawable.bg_inventory_status_pending,
                        R.color.text_grey);
                return;
            }

            int variance = item.getVariance();
            if (variance == 0) {
                setStatus("Conferido", "Sem divergência", R.drawable.bg_inventory_status_ok,
                        R.color.primary_green);
            } else if (variance < 0) {
                setStatus("Divergência", "Falta " + Math.abs(variance) + " un.",
                        R.drawable.bg_inventory_status_problem, R.color.accent_red);
                requireDivergenceObservation(item);
            } else {
                setStatus("Divergência", "Excesso de " + variance + " un.",
                        R.drawable.bg_inventory_status_warning, R.color.accent_orange);
                requireDivergenceObservation(item);
            }
        }

        private void requireDivergenceObservation(InventoryItem item) {
            if (item.getObservation() == null || item.getObservation().trim().isEmpty()) {
                tilObservation.setError("Explique a divergência antes de finalizar");
            }
        }

        private void setStatus(String status, String variance, int backgroundRes, int colorRes) {
            tvStatus.setText(status);
            tvStatus.setBackgroundResource(backgroundRes);
            tvVariance.setText(variance);
            int color = ContextCompat.getColor(itemView.getContext(), colorRes);
            tvStatus.setTextColor(color);
            tvVariance.setTextColor(color);
        }

        private Integer parseQuantity(String rawValue) {
            if (rawValue == null || rawValue.trim().isEmpty()) return null;
            try {
                return Integer.parseInt(rawValue.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
    }
}
