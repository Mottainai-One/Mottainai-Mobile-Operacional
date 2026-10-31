package com.mottainai.operacional.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.ItemStockProductBinding;
import com.mottainai.operacional.models.Product;

import java.util.ArrayList;
import java.util.List;

/** Cartões de saldo reutilizados pelos filtros do inventário. */
public class StockProductAdapter extends RecyclerView.Adapter<StockProductAdapter.Holder> {
    public interface OnProductClick { void onClick(Product product); }

    private final List<Product> items = new ArrayList<>();
    private final OnProductClick click;

    public StockProductAdapter(OnProductClick click) { this.click = click; }

    public void submit(List<Product> products) {
        items.clear();
        if (products != null) items.addAll(products);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemStockProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Product product = items.get(position);
        holder.binding.tvName.setText(product.getName());
        holder.binding.tvQuantity.setText(holder.binding.getRoot().getContext().getString(
                R.string.op_stock_quantity, product.getQuantity()));
        int minimum = product.getMinQuantity();
        holder.binding.tvDetail.setText(minimum > 0
                ? holder.binding.getRoot().getContext().getString(R.string.op_min_quantity, minimum)
                : holder.binding.getRoot().getContext().getString(R.string.op_code_value,
                    product.getSku() == null ? "—" : product.getSku()));
        int status;
        int color;
        if (product.getQuantity() <= 0) {
            status = R.string.op_stock_out;
            color = R.color.op_critical;
        } else if (minimum > 0 && product.getQuantity() <= minimum / 2) {
            status = R.string.op_stock_critical;
            color = R.color.op_critical;
        } else if (product.isLowStock()) {
            status = R.string.op_stock_low;
            color = R.color.op_warning_text;
        } else {
            status = R.string.op_stock_available;
            color = R.color.op_success;
        }
        holder.binding.tvStatus.setText(status);
        holder.binding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), color));
        boolean needsAttention = status != R.string.op_stock_available;
        holder.binding.getRoot().setCardBackgroundColor(ContextCompat.getColor(
                holder.itemView.getContext(), needsAttention ? R.color.op_warning_soft : R.color.op_surface));
        holder.binding.getRoot().setStrokeColor(ContextCompat.getColor(
                holder.itemView.getContext(), needsAttention ? R.color.op_warning_text : R.color.op_outline));
        holder.itemView.setOnClickListener(v -> click.onClick(product));
    }

    @Override public int getItemCount() { return items.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemStockProductBinding binding;
        Holder(ItemStockProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
