package com.mottainai.operacional.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Estado de uma sessão de inventário usada pela interface. */
public class InventorySession {

    public enum Status {
        IN_PROGRESS,
        FINALIZED
    }

    private final String id;
    private final String storeId;
    private final String startedBy;
    private final String startedAt;
    private final Status status;
    private final List<InventoryItem> items;

    public InventorySession(String id, String storeId, String startedBy, String startedAt,
                            Status status, List<InventoryItem> items) {
        this.id = id;
        this.storeId = storeId;
        this.startedBy = startedBy;
        this.startedAt = startedAt;
        this.status = status;
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
    }

    public String getId() { return id; }
    public String getStoreId() { return storeId; }
    public String getStartedBy() { return startedBy; }
    public String getStartedAt() { return startedAt; }
    public Status getStatus() { return status; }
    public List<InventoryItem> getItems() { return items; }

    public int getCountedItems() {
        int count = 0;
        for (InventoryItem item : items) {
            if (item.isCounted()) count++;
        }
        return count;
    }

    public boolean isComplete() {
        return !items.isEmpty() && getCountedItems() == items.size();
    }

    public int getDivergenceItems() {
        int count = 0;
        for (InventoryItem item : items) {
            if (item.hasDivergence()) count++;
        }
        return count;
    }

    public int getMissingItems() {
        int count = 0;
        for (InventoryItem item : items) {
            if (item.isCounted() && item.getVariance() < 0) count++;
        }
        return count;
    }

    public int getExcessItems() {
        int count = 0;
        for (InventoryItem item : items) {
            if (item.isCounted() && item.getVariance() > 0) count++;
        }
        return count;
    }

    public boolean hasUnexplainedDivergences() {
        for (InventoryItem item : items) {
            if (item.hasDivergence()
                    && (item.getObservation() == null || item.getObservation().trim().isEmpty())) {
                return true;
            }
        }
        return false;
    }

    public InventorySession withItems(List<InventoryItem> newItems) {
        return new InventorySession(id, storeId, startedBy, startedAt, status, newItems);
    }

    public InventorySession withStatus(Status newStatus) {
        return new InventorySession(id, storeId, startedBy, startedAt, newStatus, items);
    }
}
