package com.rocmine.wip.hydrify.ui.settings;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.rocmine.wip.hydrify.MainActivity;
import com.rocmine.wip.hydrify.R;
import com.rocmine.wip.hydrify.util.HydrationTracker;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private HydrationTracker hydrationTracker;
    
    // Goal settings
    private TextView goalAmountTextView;
    private CardView goalSettingsCard;
    
    // Profile settings
    private TextView weightValueTextView;
    private TextView activityLevelValueTextView;
    private CardView profileSettingsCard;
    
    // Theme settings
    private CardView themeSettingsCard;
    private RadioGroup themeRadioGroup;
    private RadioButton radioLight, radioDark, radioSystem;
    
    // Reminder settings
    private TextView reminderStatusTextView;
    private TextView reminderIntervalTextView;
    private TextView wakeTimeTextView;
    private TextView sleepTimeTextView;
    private CardView reminderSettingsCard;
    private Switch reminderSwitch;
    
    // Save button
    private Button saveSettingsButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize hydration tracker
        hydrationTracker = new HydrationTracker(requireContext());

        // Initialize views
        initializeViews(root);
        setupClickListeners();
        updateSettingsDisplay();

        return root;
    }

    private void initializeViews(View root) {
        // Goal settings
        goalAmountTextView = root.findViewById(R.id.goalAmountTextView);
        goalSettingsCard = root.findViewById(R.id.goalSettingsCard);

        // Profile settings
        weightValueTextView = root.findViewById(R.id.weightValueTextView);
        activityLevelValueTextView = root.findViewById(R.id.activityLevelValueTextView);
        profileSettingsCard = root.findViewById(R.id.profileSettingsCard);
        
        // Theme settings
        themeSettingsCard = root.findViewById(R.id.themeSettingsCard);
        themeRadioGroup = root.findViewById(R.id.themeRadioGroup);
        radioLight = root.findViewById(R.id.radioLight);
        radioDark = root.findViewById(R.id.radioDark);
        radioSystem = root.findViewById(R.id.radioSystem);

        // Reminder settings
        reminderStatusTextView = root.findViewById(R.id.reminderStatusTextView);
        reminderIntervalTextView = root.findViewById(R.id.reminderIntervalTextView);
        wakeTimeTextView = root.findViewById(R.id.wakeTimeTextView);
        sleepTimeTextView = root.findViewById(R.id.sleepTimeTextView);
        reminderSettingsCard = root.findViewById(R.id.reminderSettingsCard);
        reminderSwitch = root.findViewById(R.id.reminderSwitch);
        
        // Save button
        saveSettingsButton = root.findViewById(R.id.saveSettingsButton);
    }

    private void setupClickListeners() {
        // Goal settings
        goalSettingsCard.setOnClickListener(v -> showDailyGoalDialog());
        
        // Profile settings
        profileSettingsCard.setOnClickListener(v -> showProfileDialog());
        
        // Theme settings
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioLight) {
                setAppTheme(MainActivity.THEME_LIGHT);
            } else if (checkedId == R.id.radioDark) {
                setAppTheme(MainActivity.THEME_DARK);
            } else {
                setAppTheme(MainActivity.THEME_SYSTEM);
            }
        });
        
        // Reminder settings
        reminderSettingsCard.setOnClickListener(v -> showReminderDialog());
        
        // Reminder switch
        reminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hydrationTracker.setReminderEnabled(isChecked);
            updateSettingsDisplay();
            if (isChecked) {
                Toast.makeText(requireContext(), R.string.reminders_enabled, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), R.string.reminders_disabled, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Save button
        saveSettingsButton.setOnClickListener(v -> {
            // All settings are already saved when changed through their respective dialogs
            // This just serves as a visual confirmation for the user
            Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateSettingsDisplay() {
        // Update goal value
        goalAmountTextView.setText(getString(R.string.ml_value, hydrationTracker.getDailyGoal()));
        
        // Update profile values
        weightValueTextView.setText(getString(R.string.kg_value, hydrationTracker.getWeight()));
        
        String activityLevel = hydrationTracker.getActivityLevel();
        String activityLevelDisplay;
        switch (activityLevel) {
            case "low":
                activityLevelDisplay = getString(R.string.activity_low);
                break;
            case "high":
                activityLevelDisplay = getString(R.string.activity_high);
                break;
            default:
                activityLevelDisplay = getString(R.string.activity_moderate);
                break;
        }
        activityLevelValueTextView.setText(activityLevelDisplay);
        
        // Update theme selection
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String currentTheme = preferences.getString(MainActivity.THEME_PREF_KEY, MainActivity.THEME_SYSTEM);
        
        if (MainActivity.THEME_LIGHT.equals(currentTheme)) {
            radioLight.setChecked(true);
        } else if (MainActivity.THEME_DARK.equals(currentTheme)) {
            radioDark.setChecked(true);
        } else {
            radioSystem.setChecked(true);
        }
        
        // Update reminder values
        boolean remindersEnabled = hydrationTracker.isReminderEnabled();
        reminderSwitch.setChecked(remindersEnabled);
        
        reminderStatusTextView.setText(remindersEnabled ? 
                R.string.status_enabled : R.string.status_disabled);
        
        reminderIntervalTextView.setText(
                getString(R.string.minutes_value, hydrationTracker.getReminderInterval()));
        
        wakeTimeTextView.setText(formatTimeDisplay(hydrationTracker.getWakeTime()));
        sleepTimeTextView.setText(formatTimeDisplay(hydrationTracker.getSleepTime()));
    }
    
    /**
     * Set the app theme and save the preference
     */
    private void setAppTheme(String themeName) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(MainActivity.THEME_PREF_KEY, themeName);
        editor.apply();
        
        // Apply the theme immediately
        switch (themeName) {
            case MainActivity.THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case MainActivity.THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case MainActivity.THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    private String formatTimeDisplay(int hourOfDay) {
        String amPm = hourOfDay >= 12 ? "PM" : "AM";
        int displayHour = hourOfDay > 12 ? hourOfDay - 12 : hourOfDay;
        if (displayHour == 0) displayHour = 12;
        return String.format(Locale.getDefault(), "%d:00 %s", displayHour, amPm);
    }

    private void showDailyGoalDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_daily_goal, null);
        EditText goalEditText = dialogView.findViewById(R.id.goalEditText);
        Button calculateButton = dialogView.findViewById(R.id.calculateButton);
        
        // Set current goal
        goalEditText.setText(String.valueOf(hydrationTracker.getDailyGoal()));
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.set_daily_goal)
                .setView(dialogView)
                .setPositiveButton(R.string.save, null) // Set in onShow to prevent auto-dismiss
                .setNegativeButton(R.string.cancel, null)
                .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                String goalStr = goalEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(goalStr)) {
                    try {
                        int goal = Integer.parseInt(goalStr);
                        if (goal > 0) {
                            hydrationTracker.setDailyGoal(goal);
                            updateSettingsDisplay();
                            dialog.dismiss();
                        } else {
                            showToast(R.string.enter_valid_goal);
                        }
                    } catch (NumberFormatException e) {
                        showToast(R.string.enter_valid_goal);
                    }
                } else {
                    showToast(R.string.enter_valid_goal);
                }
            });
        });
        
        calculateButton.setOnClickListener(v -> {
            int recommendedGoal = hydrationTracker.calculateRecommendedDailyIntake();
            goalEditText.setText(String.valueOf(recommendedGoal));
        });
        
        dialog.show();
    }

    private void showProfileDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_profile, null);
        EditText weightEditText = dialogView.findViewById(R.id.weightEditText);
        RadioGroup activityLevelGroup = dialogView.findViewById(R.id.activityLevelGroup);
        
        // Set current values
        weightEditText.setText(String.valueOf(hydrationTracker.getWeight()));
        
        // Select the correct radio button
        String currentActivityLevel = hydrationTracker.getActivityLevel();
        int radioButtonId;
        switch (currentActivityLevel) {
            case "low":
                radioButtonId = R.id.radioLow;
                break;
            case "high":
                radioButtonId = R.id.radioHigh;
                break;
            default:
                radioButtonId = R.id.radioModerate;
                break;
        }
        activityLevelGroup.check(radioButtonId);
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.profile_settings)
                .setView(dialogView)
                .setPositiveButton(R.string.save, null) // Set in onShow to prevent auto-dismiss
                .setNegativeButton(R.string.cancel, null)
                .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                // Get weight
                String weightStr = weightEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(weightStr)) {
                    try {
                        int weight = Integer.parseInt(weightStr);
                        if (weight > 0) {
                            hydrationTracker.setWeight(weight);
                            
                            // Get activity level
                            int selectedRadioId = activityLevelGroup.getCheckedRadioButtonId();
                            String activityLevel;
                            if (selectedRadioId == R.id.radioLow) {
                                activityLevel = "low";
                            } else if (selectedRadioId == R.id.radioHigh) {
                                activityLevel = "high";
                            } else {
                                activityLevel = "moderate";
                            }
                            hydrationTracker.setActivityLevel(activityLevel);
                            
                            updateSettingsDisplay();
                            dialog.dismiss();
                        } else {
                            showToast(R.string.enter_valid_weight);
                        }
                    } catch (NumberFormatException e) {
                        showToast(R.string.enter_valid_weight);
                    }
                } else {
                    showToast(R.string.enter_valid_weight);
                }
            });
        });
        
        dialog.show();
    }

    private void showReminderDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reminders, null);
        EditText intervalEditText = dialogView.findViewById(R.id.intervalEditText);
        Button setWakeTimeButton = dialogView.findViewById(R.id.setWakeTimeButton);
        Button setSleepTimeButton = dialogView.findViewById(R.id.setSleepTimeButton);
        
        // Set current values
        intervalEditText.setText(String.valueOf(hydrationTracker.getReminderInterval()));
        
        setWakeTimeButton.setText(formatTimeDisplay(hydrationTracker.getWakeTime()));
        setSleepTimeButton.setText(formatTimeDisplay(hydrationTracker.getSleepTime()));
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.reminder_settings)
                .setView(dialogView)
                .setPositiveButton(R.string.save, null) // Set in onShow to prevent auto-dismiss
                .setNegativeButton(R.string.cancel, null)
                .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                // Get reminder interval
                String intervalStr = intervalEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(intervalStr)) {
                    try {
                        int interval = Integer.parseInt(intervalStr);
                        if (interval > 0) {
                            hydrationTracker.setReminderInterval(interval);
                            updateSettingsDisplay();
                            dialog.dismiss();
                        } else {
                            showToast(R.string.enter_valid_interval);
                        }
                    } catch (NumberFormatException e) {
                        showToast(R.string.enter_valid_interval);
                    }
                } else {
                    showToast(R.string.enter_valid_interval);
                }
            });
        });
        
        setWakeTimeButton.setOnClickListener(v -> showTimePickerDialog(true));
        setSleepTimeButton.setOnClickListener(v -> showTimePickerDialog(false));
        
        dialog.show();
    }

    private void showTimePickerDialog(boolean isWakeTime) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_time_picker, null);
        TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
        
        // 24-hour view
        timePicker.setIs24HourView(true);
        
        // Set current time
        int currentHour = isWakeTime ? 
                hydrationTracker.getWakeTime() : hydrationTracker.getSleepTime();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            timePicker.setHour(currentHour);
            timePicker.setMinute(0); // Always set to on the hour
        } else {
            timePicker.setCurrentHour(currentHour);
            timePicker.setCurrentMinute(0);
        }
        
        String title = isWakeTime ? getString(R.string.set_wake_time) : getString(R.string.set_sleep_time);
        
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    int hourOfDay;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        hourOfDay = timePicker.getHour();
                    } else {
                        hourOfDay = timePicker.getCurrentHour();
                    }
                    
                    if (isWakeTime) {
                        hydrationTracker.setWakeTime(hourOfDay);
                    } else {
                        hydrationTracker.setSleepTime(hourOfDay);
                    }
                    
                    updateSettingsDisplay();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showToast(int stringResId) {
        Toast.makeText(requireContext(), stringResId, Toast.LENGTH_SHORT).show();
    }
} 