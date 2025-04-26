package com.rocmine.wip.hydrify.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rocmine.wip.hydrify.util.HydrationTracker;

/**
 * Broadcast receiver for hydration reminders.
 * Triggered by the AlarmManager to show notifications at scheduled intervals.
 */
public class HydrationReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Create a new HydrationTracker instance
        HydrationTracker hydrationTracker = new HydrationTracker(context);
        
        // Show the reminder notification
        hydrationTracker.showReminderNotification();
    }
} 