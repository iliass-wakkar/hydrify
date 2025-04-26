package com.rocmine.wip.hydrify.ui.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.rocmine.wip.hydrify.R;
import com.rocmine.wip.hydrify.database.HydrationDatabaseHelper;
import com.rocmine.wip.hydrify.util.HydrationTracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private HydrationTracker hydrationTracker;
    private ProgressBar todayProgressBar;
    private TextView currentIntakeTextView;
    private TextView goalIntakeTextView;
    private TextView percentageTextView;
    private BarChart weeklyChart;
    private CardView quickAddCard;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize hydration tracker
        hydrationTracker = new HydrationTracker(requireContext());

        // Initialize views
        todayProgressBar = root.findViewById(R.id.todayProgressBar);
        currentIntakeTextView = root.findViewById(R.id.currentIntakeTextView);
        goalIntakeTextView = root.findViewById(R.id.goalIntakeTextView);
        percentageTextView = root.findViewById(R.id.percentageTextView);
        weeklyChart = root.findViewById(R.id.weeklyChart);
        quickAddCard = root.findViewById(R.id.quickAddCard);

        setupQuickAddButtons(root);
        updateDashboardData();
        setupWeeklyChart();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDashboardData();
        setupWeeklyChart();
    }

    private void setupQuickAddButtons(View root) {
        CardView smallGlassCard = root.findViewById(R.id.smallGlassCard);
        CardView mediumGlassCard = root.findViewById(R.id.mediumGlassCard);
        CardView largeGlassCard = root.findViewById(R.id.largeGlassCard);

        smallGlassCard.setOnClickListener(v -> {
            hydrationTracker.addHydrationEntry(150, "Water");
            updateDashboardData();
        });

        mediumGlassCard.setOnClickListener(v -> {
            hydrationTracker.addHydrationEntry(250, "Water");
            updateDashboardData();
        });

        largeGlassCard.setOnClickListener(v -> {
            hydrationTracker.addHydrationEntry(500, "Water");
            updateDashboardData();
        });
    }

    private void updateDashboardData() {
        int totalIntake = hydrationTracker.getTodayTotalIntake();
        int dailyGoal = hydrationTracker.getDailyGoal();
        int percentage = hydrationTracker.getDailyGoalPercentage();

        // Update progress bar (max is 100)
        todayProgressBar.setProgress(percentage);

        // Update text views
        currentIntakeTextView.setText(totalIntake + " ml");
        goalIntakeTextView.setText("/ " + dailyGoal + " ml");
        percentageTextView.setText(percentage + "%");
        
        // Set progress bar color based on progress
        if (percentage < 30) {
            todayProgressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FF5252")));
        } else if (percentage < 70) {
            todayProgressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFA726")));
        } else {
            todayProgressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        }
    }

    private void setupWeeklyChart() {
        // Customize chart appearance
        weeklyChart.getDescription().setEnabled(false);
        weeklyChart.setDrawGridBackground(false);
        weeklyChart.setDrawBarShadow(false);
        weeklyChart.setHighlightFullBarEnabled(false);
        weeklyChart.getLegend().setEnabled(false);
        weeklyChart.setPinchZoom(false);
        weeklyChart.setScaleEnabled(false);
        weeklyChart.setDoubleTapToZoomEnabled(false);
        
        // Customize X axis
        XAxis xAxis = weeklyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        
        // Disable right Y axis
        weeklyChart.getAxisRight().setEnabled(false);
        
        // Customize left Y axis
        weeklyChart.getAxisLeft().setDrawGridLines(false);
        weeklyChart.getAxisLeft().setAxisMinimum(0f);
        
        // Get the data for the past 7 days
        List<BarEntry> entries = getWeeklyData();
        
        // Create dataset and customize appearance
        BarDataSet dataSet = new BarDataSet(entries, "Weekly Hydration");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setDrawValues(false);
        
        // Create and set the data
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);
        weeklyChart.setData(data);
        
        // Set the X axis labels (abbreviated day names)
        ArrayList<String> dayLabels = getDayLabels();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dayLabels));
        
        // Refresh the chart
        weeklyChart.invalidate();
    }

    private List<BarEntry> getWeeklyData() {
        List<BarEntry> entries = new ArrayList<>();
        HydrationDatabaseHelper dbHelper = new HydrationDatabaseHelper(requireContext());
        
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        // Get data for the past 7 days (including today)
        for (int i = 6; i >= 0; i--) {
            Calendar dayCal = (Calendar) calendar.clone();
            dayCal.add(Calendar.DAY_OF_YEAR, -i);
            String dateString = dateFormat.format(dayCal.getTime());
            
            // Here you would get the intake for the specific day
            // For now, we'll use a dummy implementation
            int intake = dbHelper.getDayTotalIntake(dateString);
            
            // Add entry where X is the day index (0-6) and Y is the intake
            entries.add(new BarEntry(6-i, intake));
        }
        
        return entries;
    }

    private ArrayList<String> getDayLabels() {
        ArrayList<String> labels = new ArrayList<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        
        for (int i = 6; i >= 0; i--) {
            Calendar dayCal = (Calendar) calendar.clone();
            dayCal.add(Calendar.DAY_OF_YEAR, -i);
            labels.add(dayFormat.format(dayCal.getTime()));
        }
        
        return labels;
    }
} 