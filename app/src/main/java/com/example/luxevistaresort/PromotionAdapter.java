package com.example.luxevistaresort;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromoViewHolder> {

    private final List<Promotion> promotions;
    private final Context context;

    public PromotionAdapter(Context context, List<Promotion> promotions) {
        this.context = context;
        this.promotions = promotions;
    }

    @NonNull
    @Override
    public PromoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promotion, parent, false);
        return new PromoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PromoViewHolder holder, int position) {
        Promotion p = promotions.get(position);
        holder.title.setText(p.getTitle());
        // short description: first 80 chars (or full if shorter)
        String desc = p.getDescription() == null ? "" : p.getDescription();
        holder.shortDesc.setText(desc.length() > 80 ? desc.substring(0, 80) + "…" : desc);

        String validity = "Valid: " + (p.getStartDate() == null ? "-" : p.getStartDate())
                + " → " + (p.getEndDate() == null ? "-" : p.getEndDate());
        holder.validity.setText(validity);

        // Load first image from CSV stored in DB. We map image name to drawable resource id.
        int resId = getDrawableIdFromCsv(p.getImages());
        if (resId != 0) {
            holder.image.setImageResource(resId);
        } else {
            holder.image.setImageResource(R.drawable.ic_image_placeholder);
        }

        holder.card.setOnClickListener(v -> {
            // show full details in a dialog
            new AlertDialog.Builder(context)
                    .setTitle(p.getTitle())
                    .setMessage(p.getDescription() + "\n\n" + validity)
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return promotions.size();
    }

    static class PromoViewHolder extends RecyclerView.ViewHolder {
        LinearLayout card;
        ImageView image;
        TextView title, shortDesc, validity;

        PromoViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardPromo);
            image = itemView.findViewById(R.id.promoImage);
            title = itemView.findViewById(R.id.promoTitle);
            shortDesc = itemView.findViewById(R.id.promoShortDesc);
            validity = itemView.findViewById(R.id.promoValidity);
        }
    }

    // Helper: take CSV images string, return resource id for first image (strip extension)
    private int getDrawableIdFromCsv(String csv) {
        if (csv == null || csv.trim().isEmpty()) return 0;
        String[] parts = csv.split(",");
        if (parts.length == 0) return 0;
        String first = parts[0].trim();
        // strip extension if present
        first = first.replaceAll("\\s+", "_"); // replace spaces with underscore if any
        int dot = first.lastIndexOf('.');
        if (dot > 0) first = first.substring(0, dot);
        // lower-case to match typical drawable naming
        first = first.toLowerCase();
        int resId = context.getResources().getIdentifier(first, "drawable", context.getPackageName());
        return resId;
    }
}
