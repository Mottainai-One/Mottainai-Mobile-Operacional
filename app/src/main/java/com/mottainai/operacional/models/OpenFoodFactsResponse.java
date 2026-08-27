package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;

/**
 * DTO mínimo da Open Food Facts (https://world.openfoodfacts.org/api/v0/product/{barcode}.json)
 * Usado apenas para preenchimento auxiliar — sem chave, sem dados pessoais.
 */
public class OpenFoodFactsResponse {

    @SerializedName("status")
    private int status; // 1 = found, 0 = not found

    @SerializedName("status_verbose")
    private String statusVerbose;

    @SerializedName("product")
    private ProductData product;

    public int getStatus() { return status; }
    public String getStatusVerbose() { return statusVerbose; }
    public ProductData getProduct() { return product; }

    public boolean isFound() { return status == 1 && product != null; }

    public static class ProductData {
        @SerializedName("product_name")
        private String productName;

        @SerializedName("brands")
        private String brands;

        @SerializedName("quantity")
        private String quantity; // ex: "500g"

        @SerializedName("categories")
        private String categories;

        @SerializedName("image_url")
        private String imageUrl;

        public String getProductName() { return productName; }
        public String getBrands() { return brands; }
        public String getQuantity() { return quantity; }
        public String getCategories() { return categories; }
        public String getImageUrl() { return imageUrl; }
    }
}