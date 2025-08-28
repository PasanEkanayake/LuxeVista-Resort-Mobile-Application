package com.example.luxevistaresort;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private ArrayList<Room> rooms;
    private OnRoomClickListener listener;

    public interface OnRoomClickListener {
        void onRoomClick(Room room);
    }

    public RoomAdapter(ArrayList<Room> rooms, OnRoomClickListener listener) {
        this.rooms = rooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = rooms.get(position);
        holder.txtName.setText(room.getName());
        holder.txtType.setText(room.getType());
        holder.txtPrice.setText("Rs. " + room.getPrice() + " / night");

        String images = room.getImages();
        if (images != null && !images.trim().isEmpty()) {
            String first = images.split(",")[0].trim();

            String name = first.contains(".") ? first.substring(0, first.lastIndexOf('.')) : first;

            int resId = holder.itemView.getContext()
                    .getResources()
                    .getIdentifier(name, "drawable", holder.itemView.getContext().getPackageName());

            if (resId != 0) {
                Glide.with(holder.itemView.getContext())
                        .load(resId)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .into(holder.imgRoom);
            } else {
                holder.imgRoom.setImageResource(R.drawable.ic_image_placeholder);
                Log.d("RoomAdapter", "Drawable not found for name: " + name);
            }
        } else {
            holder.imgRoom.setImageResource(R.drawable.ic_image_placeholder);
        }

        holder.itemView.setOnClickListener(v -> listener.onRoomClick(room));
    }


    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtType, txtPrice;
        ImageView imgRoom;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtRoomItemName);
            txtType = itemView.findViewById(R.id.txtRoomItemType);
            txtPrice = itemView.findViewById(R.id.txtRoomItemPrice);
            imgRoom = itemView.findViewById(R.id.imgRoomItem);
        }
    }
}
