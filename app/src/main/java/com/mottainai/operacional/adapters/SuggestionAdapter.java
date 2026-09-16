package com.mottainai.operacional.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.operacional.models.Suggestion;
import com.mottainai.operacional.R;

import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder> {

    /** Clique em um item da lista, para abrir a tela de decisão da sugestão. */
    public interface OnSuggestionClickListener {
        void onSuggestionClick(Suggestion suggestion);
    }

    private List<Suggestion> suggestionList;
    private final OnSuggestionClickListener clickListener;
    private final OnSuggestionClickListener approveListener;
    private final OnSuggestionClickListener rejectListener;

    public SuggestionAdapter() {
        this(null, null, null);
    }

    public SuggestionAdapter(OnSuggestionClickListener clickListener) {
        this(clickListener, clickListener, clickListener);
    }

    public SuggestionAdapter(OnSuggestionClickListener clickListener,
                             OnSuggestionClickListener approveListener,
                             OnSuggestionClickListener rejectListener) {
        this.suggestionList = null;
        this.clickListener = clickListener;
        this.approveListener = approveListener;
        this.rejectListener = rejectListener;
    }

    public void setSuggestions(List<Suggestion> suggestionList) {
        this.suggestionList = suggestionList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        Suggestion suggestion = suggestionList.get(position);
        holder.tvSuggestionTitle.setText(suggestion.getTitle());
        holder.tvSuggestionDescription.setText(suggestion.getDescription());
        holder.tvSuggestionStatus.setText(statusLabel(holder.itemView.getContext(), suggestion.getStatus()));
        holder.itemView.setOnClickListener(clickListener == null ? null
                : v -> clickListener.onSuggestionClick(suggestion));
        holder.btnApprove.setOnClickListener(approveListener == null ? null
                : v -> approveListener.onSuggestionClick(suggestion));
        holder.btnReject.setOnClickListener(rejectListener == null ? null
                : v -> rejectListener.onSuggestionClick(suggestion));
    }

    @Override
    public int getItemCount() {
        return suggestionList != null ? suggestionList.size() : 0;
    }

    private static String statusLabel(Context context, String status) {
        if (status == null || status.trim().isEmpty() || "pending".equalsIgnoreCase(status)) {
            return context.getString(R.string.suggestion_pending);
        }
        return status.trim();
    }

    public static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvSuggestionTitle;
        TextView tvSuggestionDescription;
        TextView tvSuggestionStatus;
        View btnApprove;
        View btnReject;

        public SuggestionViewHolder(View view) {
            super(view);
            tvSuggestionTitle = view.findViewById(R.id.tv_suggestion_title);
            tvSuggestionDescription = view.findViewById(R.id.tv_suggestion_description);
            tvSuggestionStatus = view.findViewById(R.id.tv_suggestion_status);
            btnApprove = view.findViewById(R.id.btn_suggestion_approve);
            btnReject = view.findViewById(R.id.btn_suggestion_reject);
        }
    }
}
