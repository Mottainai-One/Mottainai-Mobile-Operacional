package com.mottainai.operacional.models;

/**
 * Item de uma contagem física. A quantidade do sistema é um retrato tirado
 * quando a sessão começa; a quantidade física pode ficar vazia até ser
 * conferida.
 */
public class InventoryItem {

    private final String productId;
    private final String name;
    private final String sku;
    private final int expectedQuantity;
    private final Integer countedQuantity;
    private final String observation;

    public InventoryItem(String productId, String name, String sku, int expectedQuantity,
                         Integer countedQuantity, String observation) {
        this.productId = productId;
        this.name = name;
        this.sku = sku;
        this.expectedQuantity = expectedQuantity;
        this.countedQuantity = countedQuantity;
        this.observation = observation;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public int getExpectedQuantity() {
        return expectedQuantity;
    }

    public Integer getCountedQuantity() {
        return countedQuantity;
    }

    public String getObservation() {
        return observation;
    }

    public boolean isCounted() {
        return countedQuantity != null;
    }

    public int getVariance() {
        return isCounted() ? countedQuantity - expectedQuantity : 0;
    }

    public boolean hasDivergence() {
        return isCounted() && getVariance() != 0;
    }

    public InventoryItem withCountedQuantity(Integer quantity) {
        return new InventoryItem(productId, name, sku, expectedQuantity, quantity, observation);
    }

    public InventoryItem withObservation(String newObservation) {
        return new InventoryItem(productId, name, sku, expectedQuantity, countedQuantity,
                newObservation);
    }
}
