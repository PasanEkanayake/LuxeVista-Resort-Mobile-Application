package com.example.luxevistaresort;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttractionAdapter extends RecyclerView.Adapter<AttractionAdapter.AttractionViewHolder> {

    private final List<Attraction> items;
    private final Context context;

    public AttractionAdapter(Context context, List<Attraction> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public AttractionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attraction, parent, false);
        return new AttractionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AttractionViewHolder holder, int position) {
        Attraction a = items.get(position);
        holder.title.setText(a.getTitle());
        String shortDesc = a.getDescription() == null ? "" :
                (a.getDescription().length() > 100 ? a.getDescription().substring(0, 100) + "…" : a.getDescription());
        holder.desc.setText(shortDesc);
        holder.distance.setText(a.getDistance() == null ? "-" : a.getDistance());

        int resId = getDrawableIdFromCsv(a.getImages());
        if (resId != 0) holder.image.setImageResource(resId);
        else holder.image.setImageResource(R.drawable.ic_image_placeholder);

        holder.card.setOnClickListener(v -> {
            // show details dialog
            String message = a.getDescription() + "\n\nDistance: " + (a.getDistance() == null ? "-" : a.getDistance())
                    + "\nContact: " + (a.getContact() == null ? "-" : a.getContact());
            new AlertDialog.Builder(context)
                    .setTitle(a.getTitle())
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AttractionViewHolder extends RecyclerView.ViewHolder {
        LinearLayout card;
        ImageView image;
        TextView title, desc, distance;

        AttractionViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardAttraction);
            image = itemView.findViewById(R.id.attractionImage);
            title = itemView.findViewById(R.id.attractionTitle);
            desc = itemView.findViewById(R.id.attractionDesc);
            distance = itemView.findViewById(R.id.attractionDistance);
        }
    }

    // Helper: parse CSV and return drawable resource id for first image name (strip extension)
    private int getDrawableIdFromCsv(String csv) {
        if (csv == null || csv.trim().isEmpty()) return 0;
        String[] parts = csv.split(",");
        if (parts.length == 0) return 0;
        String first = parts[0].trim();
        int dot = first.lastIndexOf('.');
        if (dot > 0) first = first.substring(0, dot);
        first = first.replaceAll("\\s+", "_").toLowerCase();
        return context.getResources().getIdentifier(first, "drawable", context.getPackageName());
    }
}
