package com.rocmine.wip.hydrify.util;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.rocmine.wip.hydrify.R;
import com.rocmine.wip.hydrify.database.HydrationDatabaseHelper;
import com.rocmine.wip.hydrify.model.HydrationEntry;
import com.rocmine.wip.hydrify.receiver.HydrationReminderReceiver;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import android.Manifest;

public class HydrationTracker {
    private static final String PREFS_NAME = "HydrationPrefs";
    private static final String KEY_DAILY_GOAL = "daily_goal_ml";
    private static final String KEY_REMINDER_INTERVAL = "reminder_interval_minutes";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_WEIGHT = "user_weight_kg";
    private static final String KEY_ACTIVITY_LEVEL = "activity_level";
    private static final String KEY_WAKE_TIME = "wake_time";
    private static final String KEY_SLEEP_TIME = "sleep_time";
    
    private static final String NOTIFICATION_CHANNEL_ID = "hydration_reminders";
    private static final int NOTIFICATION_ID = 1001;
    
    private final Context context;
    private final SharedPreferences prefs;
    private final HydrationDatabaseHelper dbHelper;
    
    // Default values
    private static final int DEFAULT_DAILY_GOAL = 2000; // 2 liters (2000ml)
    private static final int DEFAULT_REMINDER_INTERVAL = 60; // 60 minutes
    private static final boolean DEFAULT_REMINDER_ENABLED = true;
    private static final int DEFAULT_WEIGHT = 70; // 70kg
    private static final String DEFAULT_ACTIVITY_LEVEL = "moderate"; // low, moderate, high
    private static final int DEFAULT_WAKE_TIME = 8; // 8 AM
    private static final int DEFAULT_SLEEP_TIME = 22; // 10 PM
    
    public HydrationTracker(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.dbHelper = new HydrationDatabaseHelper(context);
        
        createNotificationChannel();
    }
    
    // Create a notification channel for Android 8.0 and higher
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Hydration Reminders";
            String description = "Reminders to drink water";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    // Add a hydration entry
    public void addHydrationEntry(int amountMl, String type) {
        HydrationEntry entry = new HydrationEntry(amountMl, type);
        dbHelper.addHydrationEntry(entry);
    }
    
    // Get the daily goal in ml
    public int getDailyGoal() {
        return prefs.getInt(KEY_DAILY_GOAL, DEFAULT_DAILY_GOAL);
    }
    
    // Set the daily goal in ml
    public void setDailyGoal(int goalMl) {
        prefs.edit().putInt(KEY_DAILY_GOAL, goalMl).apply();
    }
    
    // Calculate daily goal based on weight and activity level
    public int calculateRecommendedDailyIntake() {
        int weight = prefs.getInt(KEY_WEIGHT, DEFAULT_WEIGHT);
        String activityLevel = prefs.getString(KEY_ACTIVITY_LEVEL, DEFAULT_ACTIVITY_LEVEL);
        
        // Basic formula: weight in kg * multiplier based on activity level
        double multiplier;
        
        switch (activityLevel) {
            case "low":
                multiplier = 30; // Minimal activity
                break;
            case "high":
                multiplier = 40; // High activity
                break;
            case "moderate":
            default:
                multiplier = 35; // Moderate activity
                break;
        }
        
        int recommendedMl = (int) (weight * multiplier);
        
        // Save the calculated value
        setDailyGoal(recommendedMl);
        
        return recommendedMl;
    }
    
    // Get today's total intake
    public int getTodayTotalIntake() {
        return dbHelper.getTodayTotalIntake();
    }
    
    // Get percentage of daily goal achieved
    public int getDailyGoalPercentage() {
        int total = getTodayTotalIntake();
        int goal = getDailyGoal();
        
        if (goal <= 0) return 0;
        
        return (int) ((total * 100.0) / goal);
    }
    
    // Set reminder interval in minutes
    public void setReminderInterval(int minutes) {
        prefs.edit().putInt(KEY_REMINDER_INTERVAL, minutes).apply();
        
        // Update the reminder if enabled
        if (isReminderEnabled()) {
            scheduleReminder();
        }
    }
    
    // Get reminder interval in minutes
    public int getReminderInterval() {
        return prefs.getInt(KEY_REMINDER_INTERVAL, DEFAULT_REMINDER_INTERVAL);
    }
    
    // Enable or disable reminders
    public void setReminderEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply();
        
        if (enabled) {
            scheduleReminder();
        } else {
            cancelReminder();
        }
    }
    
    // Check if reminders are enabled
    public boolean isReminderEnabled() {
        return prefs.getBoolean(KEY_REMINDER_ENABLED, DEFAULT_REMINDER_ENABLED);
    }
    
    // Schedule reminder
    public void scheduleReminder() {
        if (!isReminderEnabled()) return;
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, HydrationReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // Cancel any existing alarms
        alarmManager.cancel(pendingIntent);
        
        // Schedule the next reminder
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, getReminderInterval());
        
        // Check if we're in the sleeping hours
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int wakeTime = prefs.getInt(KEY_WAKE_TIME, DEFAULT_WAKE_TIME);
        int sleepTime = prefs.getInt(KEY_SLEEP_TIME, DEFAULT_SLEEP_TIME);
        
        // Don't schedule reminders during sleep time
        if (currentHour >= sleepTime || currentHour < wakeTime) {
            // Set the alarm for wake time
            calendar.set(Calendar.HOUR_OF_DAY, wakeTime);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            
            // If it's already past today's wake time, schedule for tomorrow
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    // Fall back to inexact alarm if we don't have permission
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } catch (SecurityException e) {
            // Handle permission exception
        }
    }
    
    // Cancel reminder
    public void cancelReminder() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, HydrationReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        alarmManager.cancel(pendingIntent);
    }
    
    // Show a hydration reminder notification
    public void showReminderNotification() {
        // Check for notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != 
                    PackageManager.PERMISSION_GRANTED) {
                // Skip showing notification if we don't have permission
                return;
            }
        }
        
        Intent intent = new Intent(context, context.getClass()); // Open the app when notification is clicked
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        
        int percentage = getDailyGoalPercentage();
        String message;
        
        if (percentage < 50) {
            message = "You're only at " + percentage + "% of your daily goal. Time to hydrate!";
        } else if (percentage < 80) {
            message = "You're at " + percentage + "% of your daily goal. Keep it up!";
        } else {
            message = "Great job! You're at " + percentage + "% of your daily goal!";
        }
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Hydration Reminder")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            // Handle permission exception
        }
        
        // Reschedule the next reminder
        scheduleReminder();
    }
    
    // Set user's weight
    public void setWeight(int weightKg) {
        prefs.edit().putInt(KEY_WEIGHT, weightKg).apply();
    }
    
    // Get user's weight
    public int getWeight() {
        return prefs.getInt(KEY_WEIGHT, DEFAULT_WEIGHT);
    }
    
    // Set activity level
    public void setActivityLevel(String level) {
        prefs.edit().putString(KEY_ACTIVITY_LEVEL, level).apply();
    }
    
    // Get activity level
    public String getActivityLevel() {
        return prefs.getString(KEY_ACTIVITY_LEVEL, DEFAULT_ACTIVITY_LEVEL);
    }
    
    // Set wake time (hour of day, 0-23)
    public void setWakeTime(int hour) {
        prefs.edit().putInt(KEY_WAKE_TIME, hour).apply();
    }
    
    // Get wake time
    public int getWakeTime() {
        return prefs.getInt(KEY_WAKE_TIME, DEFAULT_WAKE_TIME);
    }
    
    // Set sleep time (hour of day, 0-23)
    public void setSleepTime(int hour) {
        prefs.edit().putInt(KEY_SLEEP_TIME, hour).apply();
    }
    
    // Get sleep time
    public int getSleepTime() {
        return prefs.getInt(KEY_SLEEP_TIME, DEFAULT_SLEEP_TIME);
    }
} 