package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mottainai.operacional.network.RedisNotificationApi;
import com.mottainai.operacional.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Inbox read directly from Redis API; no synthetic success or offline demo data. */
public class NotificationInboxFragment extends Fragment {
    private LinearLayout content;
    private Call<?> pending;
    private androidx.appcompat.app.AlertDialog detail;
    private RedisNotificationApi api;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle state) {
        ScrollView scroll = new ScrollView(requireContext());
        content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        content.setPadding(padding, padding, padding, padding);
        scroll.addView(content);
        return scroll;
    }

    @Override public void onViewCreated(View view, Bundle state) { load(); }

    private void text(String value) {
        TextView label = new TextView(requireContext());
        label.setText(value);
        label.setTextSize(16);
        content.addView(label);
    }

    private void error(String message) {
        if (content == null) return;
        content.removeAllViews();
        text(message);
        Button retry = new Button(requireContext());
        retry.setText("Tentar novamente");
        retry.setOnClickListener(v -> load());
        content.addView(retry);
    }

    private void load() {
        if (pending != null) pending.cancel();
        content.removeAllViews();
        text("Carregando notificações…");
        try {
            api = RetrofitClient.getRedisClient(requireContext()).create(RedisNotificationApi.class);
        } catch (IllegalStateException exception) {
            error(exception.getMessage());
            return;
        }
        Call<RedisNotificationApi.Inbox> call = api.list(100);
        pending = call;
        call.enqueue(new Callback<RedisNotificationApi.Inbox>() {
            @Override public void onResponse(Call<RedisNotificationApi.Inbox> request, Response<RedisNotificationApi.Inbox> response) {
                if (content == null || request.isCanceled()) return;
                RedisNotificationApi.Inbox inbox = response.body();
                if (!response.isSuccessful() || inbox == null || inbox.notifications == null) {
                    error("Não foi possível carregar as notificações (HTTP " + response.code() + ").");
                    return;
                }
                content.removeAllViews();
                text(inbox.unreadCount + " notificações não lidas");
                if (inbox.notifications.isEmpty()) text("Nenhuma notificação recebida.");
                if (inbox.notifications.size() == 100) text("Exibindo as 100 notificações mais recentes.");
                for (RedisNotificationApi.Item item : inbox.notifications) {
                    if (item == null || item.id == null) continue;
                    Button row = new Button(requireContext());
                    row.setAllCaps(false);
                    row.setText(("unread".equals(item.status) ? "● " : "") + item.title);
                    row.setOnClickListener(v -> open(item));
                    content.addView(row);
                }
            }
            @Override public void onFailure(Call<RedisNotificationApi.Inbox> request, Throwable failure) {
                if (!request.isCanceled()) error("Falha de conexão ao carregar as notificações.");
            }
        });
    }

    private void open(RedisNotificationApi.Item item) {
        detail = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(item.title).setMessage(item.body).setNegativeButton("Fechar", null)
                .setPositiveButton("Marcar como lida", (dialog, which) -> markRead(item.id)).show();
    }

    private void markRead(String id) {
        Call<RedisNotificationApi.ReadReceipt> call = api.markRead(id);
        pending = call;
        call.enqueue(new Callback<RedisNotificationApi.ReadReceipt>() {
            @Override public void onResponse(Call<RedisNotificationApi.ReadReceipt> request, Response<RedisNotificationApi.ReadReceipt> response) {
                if (content == null || request.isCanceled()) return;
                RedisNotificationApi.ReadReceipt receipt = response.body();
                if (response.isSuccessful() && receipt != null && id.equals(receipt.id) && "read".equals(receipt.status)) load();
                else error("A leitura não foi confirmada pelo serviço. Atualize e tente novamente.");
            }
            @Override public void onFailure(Call<RedisNotificationApi.ReadReceipt> request, Throwable failure) {
                if (!request.isCanceled()) error("Não foi possível confirmar a leitura. Atualize e tente novamente.");
            }
        });
    }

    @Override public void onDestroyView() {
        if (pending != null) pending.cancel();
        if (detail != null) detail.dismiss();
        detail = null;
        content = null;
        super.onDestroyView();
    }
}
