package com.mottainai.operacional.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.ItemProductBinding;
import com.mottainai.operacional.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> implements Filterable {

    private final List<Product> fullList = new ArrayList<>();
    private List<Product> filteredList = new ArrayList<>();
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter() {}

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductViewHolder(ItemProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = filteredList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void submitList(List<Product> products) {
        fullList.clear();
        if (products != null) {
            fullList.addAll(products);
        }
        filteredList = new ArrayList<>(fullList);
        notifyDataSetChanged();
    }

    public List<Product> getCurrentList() {
        return new ArrayList<>(filteredList);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString().toLowerCase().trim();
                List<Product> result = new ArrayList<>();

                if (query.isEmpty()) {
                    result.addAll(fullList);
                } else {
                    for (Product product : fullList) {
                        if (product.getName() != null && product.getName().toLowerCase().contains(query)
                                || product.getSku() != null && product.getSku().toLowerCase().contains(query)) {
                            result.add(product);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = result;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList.clear();
                filteredList.addAll((List<Product>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvProductName;
        private final TextView tvProductSku;
        private final TextView tvProductQuantity;
        private final TextView tvProductStatus;
        private final ImageView ivProductImage;
        private final View vStockIndicator;

        public ProductViewHolder(@NonNull ItemProductBinding binding) {
            super(binding.getRoot());
            tvProductName = binding.tvProductName;
            tvProductSku = binding.tvProductSku;
            tvProductQuantity = binding.tvProductQuantity;
            tvProductStatus = binding.tvProductStatus;
            ivProductImage = binding.ivProductImage;
            vStockIndicator = binding.vStockIndicator;

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null && position < filteredList.size()) {
                    listener.onProductClick(filteredList.get(position));
                }
            });
        }

        public void bind(Product product) {
            tvProductName.setText(product.getName());
            tvProductSku.setText("SKU: " + product.getSku());
            tvProductQuantity.setText("Qtd: " + product.getQuantity());

            if (product.isLowStock()) {
                tvProductStatus.setText("Baixo");
                tvProductStatus.setTextColor(itemView.getContext().getColor(R.color.severity_critico));
                vStockIndicator.setBackgroundResource(R.drawable.bg_stock_low);
            } else if (product.isAttentionStock()) {
                tvProductStatus.setText("Atenção");
                tvProductStatus.setTextColor(itemView.getContext().getColor(R.color.severity_atencao));
                vStockIndicator.setBackgroundResource(R.drawable.bg_stock_attention);
            } else {
                tvProductStatus.setText("Normal");
                tvProductStatus.setTextColor(itemView.getContext().getColor(R.color.severity_monitor));
                vStockIndicator.setBackgroundResource(R.drawable.bg_stock_normal);
            }

            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(itemView)
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.ic_product_placeholder)
                        .error(R.drawable.ic_product_placeholder)
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.ic_product_placeholder);
            }
}
}
}
