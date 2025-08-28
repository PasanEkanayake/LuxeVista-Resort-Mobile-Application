package com.example.luxevistaresort;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String bookingType = intent.getStringExtra("BOOKING_TYPE");
        Toast.makeText(context, "Reminder: Upcoming " + bookingType + " booking!", Toast.LENGTH_LONG).show();
    }
}
