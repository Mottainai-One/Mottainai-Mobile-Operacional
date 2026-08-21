package com.mottainai.operacional.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mottainai.operacional.R;
import com.mottainai.operacional.models.Product;

public class ProductAdapter extends ListAdapter<Product, ProductAdapter.ProductViewHolder> {

    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Product> DIFF_CALLBACK = new DiffUtil.ItemCallback<Product>() {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getName().equals(newItem.getName())
                    && oldItem.getQuantity() == newItem.getQuantity()
                    && oldItem.getSku().equals(newItem.getSku());
        }
    };

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = getItem(position);
        holder.bind(product);
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvProductName;
        private final TextView tvProductSku;
        private final TextView tvProductQuantity;
        private final TextView tvProductStatus;
        private final ImageView ivProductImage;
        private final View vStockIndicator;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductSku = itemView.findViewById(R.id.tv_product_sku);
            tvProductQuantity = itemView.findViewById(R.id.tv_product_quantity);
            tvProductStatus = itemView.findViewById(R.id.tv_product_status);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            vStockIndicator = itemView.findViewById(R.id.v_stock_indicator);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProductClick(getItem(position));
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

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }
}