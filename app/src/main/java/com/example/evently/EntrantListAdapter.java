package com.example.evently;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.data.model.Account;

public class EntrantListAdapter extends RecyclerView.Adapter<EntrantListAdapter.ViewHolder> {
    private ArrayList<Account> entrants;
    private LayoutInflater inflater;

    EntrantListAdapter(Context context, ArrayList<Account> entrants) {
        this.entrants = entrants;
        this.inflater = LayoutInflater.from(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView image;
        TextView name;

        public ViewHolder(View view) {
            super(view);
            // Can define click listeners here

            this.image = view.findViewById(R.id.image_placeholder);
            this.name = view.findViewById(R.id.entrant_name);
        }
    }

    @NonNull @Override
    public EntrantListAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.event_entrants_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantListAdapter.ViewHolder holder, int position) {
        String img = "Image placeholder";
        String name = entrants.get(position).name();

        holder.image.setText(img);
        holder.name.setText(name);
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }
}
