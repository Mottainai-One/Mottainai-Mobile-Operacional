package com.mottainai.operacional.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mottainai.operacional.R;
import com.mottainai.operacional.models.Supplier;
import java.util.ArrayList;
import java.util.List;

public class SupplierAdapter extends RecyclerView.Adapter<SupplierAdapter.ViewHolder> {
    public interface Listener { void onSupplierClick(Supplier supplier); }
    private final List<Supplier> suppliers = new ArrayList<>();
    private final Listener listener;
    public SupplierAdapter(Listener listener) { this.listener = listener; setHasStableIds(true); }
    public void submit(List<Supplier> values) { suppliers.clear(); if (values != null) suppliers.addAll(values); notifyDataSetChanged(); }
    @Override public long getItemId(int position) {
        String id = suppliers.get(position).getId();
        return id == null ? RecyclerView.NO_ID : id.hashCode();
    }
    @Override public int getItemCount() { return suppliers.size(); }
    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) { return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_supplier, parent, false)); }
    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) { holder.bind(suppliers.get(position), listener); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(@NonNull View view) { super(view); }
        void bind(Supplier supplier, Listener listener) {
            ((TextView) itemView.findViewById(R.id.tv_supplier_name)).setText(supplier.getTradeName());
            ((TextView) itemView.findViewById(R.id.tv_supplier_details)).setText("CNPJ " + supplier.getCnpj() + " • " + supplier.getContact());
            itemView.setOnClickListener(v -> listener.onSupplierClick(supplier));
        }
    }
}
