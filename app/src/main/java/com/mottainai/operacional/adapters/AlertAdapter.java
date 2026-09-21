package com.mottainai.operacional.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.operacional.models.Alert;
import com.mottainai.operacional.R;

import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    private List<Alert> alertList;

    public AlertAdapter() {
        this.alertList = null;
    }

    public void setAlerts(List<Alert> alertList) {
        this.alertList = alertList;
        notifyDataSetChanged();
    }

    public List<Alert> getAlerts() {
        return alertList;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        Alert alert = alertList.get(position);
        holder.tvAlertTitle.setText(alert.getTitle());
        holder.tvAlertMessage.setText(alert.getMessage());
        String severity = alert.getSeverity() != null ? alert.getSeverity() : "";
        holder.tvAlertSeverity.setText(severity);
        android.content.Context context = holder.tvAlertSeverity.getContext();
        int colorRes = R.color.text_primary;
        switch (severity) {
            case "CRITICO":
                colorRes = R.color.severity_critico;
                break;
            case "ATENCAO":
                colorRes = R.color.severity_atencao;
                break;
            case "MONITOR":
                colorRes = R.color.severity_monitor;
                break;
        }
        holder.tvAlertSeverity.setTextColor(ContextCompat.getColor(context, colorRes));
    }

    @Override
    public int getItemCount() {
        return alertList != null ? alertList.size() : 0;
    }

    public static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView tvAlertTitle;
        TextView tvAlertMessage;
        TextView tvAlertSeverity;

        public AlertViewHolder(View view) {
            super(view);
            tvAlertTitle = view.findViewById(R.id.tv_alert_title);
            tvAlertMessage = view.findViewById(R.id.tv_alert_message);
            tvAlertSeverity = view.findViewById(R.id.tv_alert_severity);
        }
    }
}