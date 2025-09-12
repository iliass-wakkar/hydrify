package com.rocmine.wip.hydrify.ui.UserInfoActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;
import com.rocmine.wip.hydrify.MainActivity;
import com.rocmine.wip.hydrify.R;
import com.rocmine.wip.hydrify.database.DatabaseHelper;
import com.rocmine.wip.hydrify.model.UserProfile;

public class UserInfoActivity extends AppCompatActivity {
    private String selectedGender = null; // "male" or "female"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userinfopage);

        // Link UI elements
        FrameLayout maleContainer = findViewById(R.id.genderMaleContainer);
        FrameLayout femaleContainer = findViewById(R.id.genderFemaleContainer);
        ImageView ivMale = findViewById(R.id.ivMale);
        ImageView ivFemale = findViewById(R.id.ivFemale);
        Slider ageSlider = findViewById(R.id.sliderAge);
        TextView ageValue = findViewById(R.id.tvAgeValue);
        Slider weightSlider = findViewById(R.id.sliderWeight);
        TextView weightValue = findViewById(R.id.tvWeightValue);
        Slider heightSlider = findViewById(R.id.sliderHeight);
        TextView heightValue = findViewById(R.id.tvHeightValue);
        TimePicker timePickerWakeup = findViewById(R.id.timePickerWakeup);
        TimePicker timePickerSleep = findViewById(R.id.timePickerSleep);
        Button btnDone = findViewById(R.id.btnDone);

        // Gender selection logic
        maleContainer.setOnClickListener(v -> {
            selectedGender = "male";
            ivMale.setImageResource(R.drawable.male_selected);
            ivFemale.setImageResource(R.drawable.female_unselected);
        });
        femaleContainer.setOnClickListener(v -> {
            selectedGender = "female";
            ivFemale.setImageResource(R.drawable.female_selected);
            ivMale.setImageResource(R.drawable.male_unselected);
        });

        // Sliders update value TextViews
        ageSlider.addOnChangeListener((slider, value, fromUser) -> ageValue.setText(String.valueOf((int) value)));
        weightSlider
                .addOnChangeListener((slider, value, fromUser) -> weightValue.setText(String.format("%.1f", value)));
        heightSlider
                .addOnChangeListener((slider, value, fromUser) -> heightValue.setText(String.format("%.1f", value)));

        // Done button logic
        btnDone.setOnClickListener(v -> {
            // Gather all data
            if (selectedGender == null) {
                Toast.makeText(this, "Please select your gender.", Toast.LENGTH_SHORT).show();
                return;
            }
            int age = (int) ageSlider.getValue();
            double weight = weightSlider.getValue();
            double height = heightSlider.getValue();
            String wakeupTime = getTimeString(timePickerWakeup);
            String sleepTime = getTimeString(timePickerSleep);

            // Basic validation
            if (age <= 0 || weight <= 0 || height <= 0) {
                Toast.makeText(this, "Please enter valid age, weight, and height.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create UserProfile and save to DB
            UserProfile userProfile = new UserProfile();
            userProfile.setGender(selectedGender);
            userProfile.setAge(age);
            userProfile.setWeight(weight);
            userProfile.setHeight(height);
            userProfile.setWakeTime(wakeupTime);
            userProfile.setSleepTime(sleepTime);
            userProfile.setDailyGoal(0); // Set a default or calculated value if needed

            DatabaseHelper dbHelper = new DatabaseHelper(this);
            long result = dbHelper.insertUserProfile(userProfile);
            if (result != -1) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to save profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getTimeString(TimePicker picker) {
        int hour, minute;
        if (Build.VERSION.SDK_INT >= 23) {
            hour = picker.getHour();
            minute = picker.getMinute();
        } else {
            hour = picker.getCurrentHour();
            minute = picker.getCurrentMinute();
        }
        return String.format("%02d:%02d", hour, minute);
    }

    public String getSelectedGender() {
        return selectedGender;
    }
}