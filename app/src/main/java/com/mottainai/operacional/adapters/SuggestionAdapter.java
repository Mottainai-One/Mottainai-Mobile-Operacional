package com.mottainai.operacional.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    public SuggestionAdapter() {
        this(null);
    }

    public SuggestionAdapter(OnSuggestionClickListener clickListener) {
        this.suggestionList = null;
        this.clickListener = clickListener;
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
        holder.tvSuggestionStatus.setText(suggestion.getStatus());
        if (clickListener != null) {
            holder.itemView.setOnClickListener(v -> clickListener.onSuggestionClick(suggestion));
        }
    }

    @Override
    public int getItemCount() {
        return suggestionList != null ? suggestionList.size() : 0;
    }

    public static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvSuggestionTitle;
        TextView tvSuggestionDescription;
        TextView tvSuggestionStatus;

        public SuggestionViewHolder(View view) {
            super(view);
            tvSuggestionTitle = view.findViewById(R.id.tv_suggestion_title);
            tvSuggestionDescription = view.findViewById(R.id.tv_suggestion_description);
            tvSuggestionStatus = view.findViewById(R.id.tv_suggestion_status);
        }
    }
}