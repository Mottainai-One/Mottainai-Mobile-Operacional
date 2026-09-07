package com.mottainai.operacional.models;

/**
 * Um item conferido na contagem de inventário.
 *
 * Sem endpoint de inventário ainda: os dados vêm de uma lista local em
 * InventoryFragment, no mesmo espírito do MockProductRepository (dev sem
 * backend). Quando o endpoint existir, este modelo deve ganhar um
 * fromJson/fromSnapshot igual aos outros modelos do pacote.
 */
public class InventoryItem {
    private final String name;
    private final int expected;
    private final int counted;

    public InventoryItem(String name, int expected, int counted) {
        this.name = name;
        this.expected = expected;
        this.counted = counted;
    }

    public String getName() { return name; }
    public int getExpected() { return expected; }
    public int getCounted() { return counted; }
    public boolean isOk() { return expected == counted; }
}
