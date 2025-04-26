package com.rocmine.wip.hydrify.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rocmine.wip.hydrify.R;
import com.rocmine.wip.hydrify.model.HydrationEntry;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HydrationEntryAdapter extends RecyclerView.Adapter<HydrationEntryAdapter.ViewHolder> {
    private List<HydrationEntry> entries;
    private final OnEntryDeleteListener deleteListener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    public interface OnEntryDeleteListener {
        void onEntryDelete(int position, HydrationEntry entry);
    }

    public HydrationEntryAdapter(List<HydrationEntry> entries, OnEntryDeleteListener deleteListener) {
        this.entries = entries;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hydration_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HydrationEntry entry = entries.get(position);
        
        holder.entryTimeTextView.setText(timeFormat.format(entry.getTimestamp()));
        holder.entryTypeTextView.setText(entry.getType());
        holder.entryAmountTextView.setText(entry.getAmountMl() + " ml");
        
        holder.deleteEntryButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onEntryDelete(holder.getAdapterPosition(), entry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entries != null ? entries.size() : 0;
    }

    public void updateEntries(List<HydrationEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView entryTimeTextView;
        TextView entryTypeTextView;
        TextView entryAmountTextView;
        ImageButton deleteEntryButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            entryTimeTextView = itemView.findViewById(R.id.entryTimeTextView);
            entryTypeTextView = itemView.findViewById(R.id.entryTypeTextView);
            entryAmountTextView = itemView.findViewById(R.id.entryAmountTextView);
            deleteEntryButton = itemView.findViewById(R.id.deleteEntryButton);
        }
    }
} 