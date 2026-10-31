package com.mottainai.operacional.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.ItemTeamMemberBinding;
import com.mottainai.operacional.models.User;
import com.mottainai.operacional.utils.RoleHelper;

import java.util.ArrayList;
import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.Holder> {
    public interface Listener { void onMember(User member); }
    private final List<User> members = new ArrayList<>();
    private final Listener listener;
    private final boolean editable;

    public TeamAdapter(boolean editable, Listener listener) {
        this.editable = editable;
        this.listener = listener;
    }

    public void submit(List<User> values) {
        members.clear();
        if (values != null) members.addAll(values);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(ItemTeamMemberBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        User member = members.get(position);
        holder.binding.tvMemberName.setText(member.getName() == null || member.getName().trim().isEmpty()
                ? holder.itemView.getContext().getString(R.string.op_team_unnamed) : member.getName());
        holder.binding.tvMemberRole.setText(RoleHelper.roleToLabel(member.getRole()));
        holder.binding.tvMemberChevron.setVisibility(editable ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(editable ? v -> listener.onMember(member) : null);
    }

    @Override public int getItemCount() { return members.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemTeamMemberBinding binding;
        Holder(ItemTeamMemberBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
