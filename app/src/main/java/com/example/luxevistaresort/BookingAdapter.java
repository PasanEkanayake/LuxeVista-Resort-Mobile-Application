package com.example.luxevistaresort;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class BookingAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<BookingItem> bookingList;

    public BookingAdapter(Context context, ArrayList<BookingItem> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @Override
    public int getCount() {
        return bookingList.size();
    }

    @Override
    public Object getItem(int position) {
        return bookingList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_booking, parent, false);
        }

        TextView title = convertView.findViewById(R.id.bookingTitle);
        TextView subtitle1 = convertView.findViewById(R.id.bookingSubtitle);
        TextView subtitle2 = convertView.findViewById(R.id.bookingSubtitle2);
        TextView subtitle3 = convertView.findViewById(R.id.bookingSubtitle3);

        BookingItem item = bookingList.get(position);

        title.setText(item.title);
        subtitle1.setText(item.date);
        subtitle2.setText(item.time != null ? item.time : "");
        subtitle3.setText("Status: " + item.status);

        return convertView;
    }
}
