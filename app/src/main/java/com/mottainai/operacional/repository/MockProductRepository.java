package com.mottainai.operacional.repository;

import android.app.Application;

import com.mottainai.operacional.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MockProductRepository extends ProductRepository {

    private final List<Product> mockProducts = new ArrayList<>();

    public MockProductRepository(Application application) {
        super(application);
        initMockData();
    }

    private void initMockData() {
        // Produto 1 - Estoque baixo
        Product p1 = new Product();
        p1.setId(UUID.randomUUID().toString());
        p1.setName("Arroz Integral 1kg");
        p1.setSku("ARZ-001");
        p1.setQuantity(3);
        p1.setMinQuantity(10);
        p1.setBatch("LOTE-2024-001");
        p1.setExpiryDate("2025-06-15");
        p1.setSupplier("Grãos Brasil");
        p1.setStoreId("loja02");
        p1.setImageUrl(""); // sem imagem
        mockProducts.add(p1);

        // Produto 2 - Atenção
        Product p2 = new Product();
        p2.setId(UUID.randomUUID().toString());
        p2.setName("Feijão Preto 1kg");
        p2.setSku("FEI-002");
        p2.setQuantity(12);
        p2.setMinQuantity(10);
        p2.setBatch("LOTE-2024-002");
        p2.setExpiryDate("2025-07-20");
        p2.setSupplier("Feijões do Sul");
        p2.setStoreId("loja02");
        p2.setImageUrl("");
        mockProducts.add(p2);

        // Produto 3 - Normal
        Product p3 = new Product();
        p3.setId(UUID.randomUUID().toString());
        p3.setName("Óleo de Soja 900ml");
        p3.setSku("OLE-003");
        p3.setQuantity(50);
        p3.setMinQuantity(10);
        p3.setBatch("LOTE-2024-003");
        p3.setExpiryDate("2025-12-01");
        p3.setSupplier("Óleos Vale");
        p3.setStoreId("loja02");
        p3.setImageUrl("");
        mockProducts.add(p3);

        // Produto 4 - Normal
        Product p4 = new Product();
        p4.setId(UUID.randomUUID().toString());
        p4.setName("Açúcar Cristal 2kg");
        p4.setSku("ACU-004");
        p4.setQuantity(25);
        p4.setMinQuantity(5);
        p4.setBatch("LOTE-2024-004");
        p4.setExpiryDate("2026-01-15");
        p4.setSupplier("Açúcar União");
        p4.setStoreId("loja02");
        p4.setImageUrl("");
        mockProducts.add(p4);

        // Produto 5 - Baixo
        Product p5 = new Product();
        p5.setId(UUID.randomUUID().toString());
        p5.setName("Sal Refinado 1kg");
        p5.setSku("SAL-005");
        p5.setQuantity(2);
        p5.setMinQuantity(8);
        p5.setBatch("LOTE-2024-005");
        p5.setExpiryDate("2026-03-10");
        p5.setSupplier("Salinas do Nordeste");
        p5.setStoreId("loja02");
        p5.setImageUrl("");
        mockProducts.add(p5);

        // Produto 6 - Atenção
        Product p6 = new Product();
        p6.setId(UUID.randomUUID().toString());
        p6.setName("Macarrão Espaguete 500g");
        p6.setSku("MAC-006");
        p6.setQuantity(14);
        p6.setMinQuantity(12);
        p6.setBatch("LOTE-2024-006");
        p6.setExpiryDate("2025-11-30");
        p6.setSupplier("Massas Italiana");
        p6.setStoreId("loja02");
        p6.setImageUrl("");
        mockProducts.add(p6);
    }

    @Override
    public void fetchProducts(String storeId, ProductListCallback callback) {
        // Simula latência de rede
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            callback.onSuccess(new ArrayList<>(mockProducts));
        }, 800);
    }

    @Override
    public void fetchProductById(String productId, ProductCallback callback) {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            for (Product p : mockProducts) {
                if (p.getId().equals(productId)) {
                    callback.onSuccess(p);
                    return;
                }
            }
            callback.onError("Produto não encontrado");
        }, 500);
    }

    @Override
    public void createProduct(Product product, ProductCallback callback) {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            String newId = UUID.randomUUID().toString();
            product.setId(newId);
            mockProducts.add(0, product);
            callback.onSuccess(product);
        }, 800);
    }

    @Override
    public void updateProduct(String productId, Product product, ProductCallback callback) {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            for (int i = 0; i < mockProducts.size(); i++) {
                if (mockProducts.get(i).getId().equals(productId)) {
                    product.setId(productId);
                    mockProducts.set(i, product);
                    callback.onSuccess(product);
                    return;
                }
            }
            callback.onError("Produto não encontrado");
        }, 800);
    }
}