package com.example.luxevistaresort;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;

public class BookingAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<BookingItem> bookingList;

    public BookingAdapter(Context context, ArrayList<BookingItem> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @Override public int getCount() { return bookingList.size(); }
    @Override public Object getItem(int position) { return bookingList.get(position); }
    @Override public long getItemId(int position) { return position; }

    private static class ViewHolder {
        TextView title, subtitle, subtitle2, subtitle3;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
            vh = new ViewHolder();
            vh.title = convertView.findViewById(R.id.bookingTitle);
            vh.subtitle = convertView.findViewById(R.id.bookingSubtitle);
            vh.subtitle2 = convertView.findViewById(R.id.bookingSubtitle2);
            vh.subtitle3 = convertView.findViewById(R.id.bookingSubtitle3);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        BookingItem booking = bookingList.get(position);

        vh.title.setText(booking.getTitle());
        vh.subtitle.setText(booking.getDate().isEmpty() ? "" : booking.getDate());
        vh.subtitle2.setText(booking.getTime().isEmpty() ? "" : booking.getTime());
        vh.subtitle3.setText("Status: " + booking.getStatus());

        int white = ContextCompat.getColor(context, android.R.color.white);
        vh.title.setTextColor(white);
        vh.subtitle.setTextColor(white);
        vh.subtitle2.setTextColor(white);
        vh.subtitle3.setTextColor(white);

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            vh.subtitle3.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_light));
        }

        return convertView;
    }
}
