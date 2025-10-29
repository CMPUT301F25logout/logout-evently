package com.example.evently.ui.organizer;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.data.model.Event;
import com.example.evently.databinding.FragmentEventBinding;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Event}.
 */
public class EventRecyclerViewAdapter
        extends RecyclerView.Adapter<EventRecyclerViewAdapter.ViewHolder> {

    private final List<Event> mValues;

    public EventRecyclerViewAdapter(List<Event> items) {
        mValues = items;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentEventBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        // TODO: Set textviews and similar as per event representation.
        holder.mContentView.setText(mValues.get(position).name());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mContentView;
        public Event mItem;

        public ViewHolder(FragmentEventBinding binding) {
            super(binding.getRoot());
            mContentView = binding.content;
        }

        @NonNull @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
