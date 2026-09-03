package com.mottainai.operacional.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.operacional.R;
import com.mottainai.operacional.models.InventoryItem;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private List<InventoryItem> items;

    public void setItems(List<InventoryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvCounts.setText("Esperado " + item.getExpected() + " - contado " + item.getCounted());
        holder.tvResult.setText(item.isOk() ? "OK" : "Divergente");
        holder.tvStatus.setText(item.isOk() ? "● Ativo" : "● Revisar");
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvCounts;
        TextView tvResult;
        TextView tvStatus;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvCounts = itemView.findViewById(R.id.tv_item_counts);
            tvResult = itemView.findViewById(R.id.tv_item_result);
            tvStatus = itemView.findViewById(R.id.tv_item_status);
        }
    }
}
