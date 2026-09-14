package com.mottainai.operacional.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.mottainai.operacional.models.InventoryItem;
import com.mottainai.operacional.models.InventorySession;
import com.mottainai.operacional.models.Product;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Fonte temporária da etapa MOBILE-07. Mantém a sessão enquanto o processo do
 * aplicativo estiver ativo e não envia nenhuma alteração para a API.
 */
public class MockInventoryRepository {

    public interface Callback {
        void onSuccess(InventorySession session);
        void onError(String message);
    }

    private static final ConcurrentMap<String, InventorySession> sessionsByStore =
            new ConcurrentHashMap<>();

    private final MockProductRepository productRepository;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public MockInventoryRepository(Application application) {
        productRepository = new MockProductRepository(application);
    }

    public void load(String storeId, Callback callback) {
        mainHandler.post(() -> callback.onSuccess(copyOf(sessionsByStore.get(storeKey(storeId)))));
    }

    public void start(String storeId, String startedBy, Callback callback) {
        String key = storeKey(storeId);
        InventorySession existing = sessionsByStore.get(key);
        if (existing != null) {
            mainHandler.post(() -> callback.onSuccess(copyOf(existing)));
            return;
        }

        productRepository.fetchProducts(storeId, new ProductRepository.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (products == null || products.isEmpty()) {
                    callback.onError("Não há produtos disponíveis para iniciar a contagem.");
                    return;
                }

                List<InventoryItem> items = new ArrayList<>();
                for (Product product : products) {
                    items.add(new InventoryItem(
                            product.getId(),
                            product.getName(),
                            product.getSku(),
                            product.getQuantity(),
                            null,
                            ""
                    ));
                }

                InventorySession session = new InventorySession(
                        UUID.randomUUID().toString(),
                        key,
                        isBlank(startedBy) ? "Usuário autenticado" : startedBy,
                        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT,
                                new Locale("pt", "BR")).format(new Date()),
                        InventorySession.Status.IN_PROGRESS,
                        items
                );
                InventorySession storedSession = sessionsByStore.putIfAbsent(key, session);
                callback.onSuccess(copyOf(storedSession != null ? storedSession : session));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void updateCount(String sessionId, String productId, Integer quantity, Callback callback) {
        updateItem(sessionId, productId, quantity, null, false, callback);
    }

    public void updateObservation(String sessionId, String productId, String observation,
                                  Callback callback) {
        updateItem(sessionId, productId, null, observation, true, callback);
    }

    private void updateItem(String sessionId, String productId, Integer quantity, String observation,
                            boolean updateObservation, Callback callback) {
        InventorySession session = findById(sessionId);
        if (session == null) {
            callback.onError("A sessão de inventário não foi encontrada.");
            return;
        }
        if (session.getStatus() == InventorySession.Status.FINALIZED) {
            callback.onError("Esta contagem já foi finalizada.");
            return;
        }

        List<InventoryItem> updatedItems = new ArrayList<>();
        boolean found = false;
        for (InventoryItem item : session.getItems()) {
            if (item.getProductId().equals(productId)) {
                found = true;
                updatedItems.add(updateObservation
                        ? item.withObservation(observation == null ? "" : observation)
                        : item.withCountedQuantity(quantity));
            } else {
                updatedItems.add(item);
            }
        }
        if (!found) {
            callback.onError("Produto não encontrado nesta contagem.");
            return;
        }

        InventorySession updated = session.withItems(updatedItems);
        sessionsByStore.put(updated.getStoreId(), updated);
        callback.onSuccess(copyOf(updated));
    }

    public void finish(String sessionId, Callback callback) {
        InventorySession session = findById(sessionId);
        if (session == null) {
            callback.onError("A sessão de inventário não foi encontrada.");
            return;
        }
        if (!session.isComplete()) {
            callback.onError("Confira todos os produtos antes de finalizar.");
            return;
        }
        if (session.hasUnexplainedDivergences()) {
            callback.onError("Registre uma observação em cada divergência antes de finalizar.");
            return;
        }

        InventorySession finished = session.withStatus(InventorySession.Status.FINALIZED);
        sessionsByStore.put(finished.getStoreId(), finished);
        callback.onSuccess(copyOf(finished));
    }

    private InventorySession findById(String sessionId) {
        for (InventorySession session : sessionsByStore.values()) {
            if (session.getId().equals(sessionId)) return session;
        }
        return null;
    }

    private InventorySession copyOf(InventorySession session) {
        if (session == null) return null;
        return new InventorySession(
                session.getId(),
                session.getStoreId(),
                session.getStartedBy(),
                session.getStartedAt(),
                session.getStatus(),
                session.getItems()
        );
    }

    private String storeKey(String storeId) {
        return isBlank(storeId) ? "demo-store" : storeId;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
