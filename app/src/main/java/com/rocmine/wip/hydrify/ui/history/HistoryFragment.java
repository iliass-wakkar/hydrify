package com.rocmine.wip.hydrify.ui.history;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rocmine.wip.hydrify.R;
import com.rocmine.wip.hydrify.adapter.HydrationEntryAdapter;
import com.rocmine.wip.hydrify.database.DatabaseHelper;
import com.rocmine.wip.hydrify.model.HydrationEntry;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment implements HydrationEntryAdapter.OnEntryDeleteListener {

    private RecyclerView entriesRecyclerView;
    private TextView dateTextView;
    private Button prevDateButton;
    private Button nextDateButton;
    private Button calendarButton;
    private TextView emptyStateTextView;
    private TextView totalIntakeTextView;

    private DatabaseHelper dbHelper;
    private HydrationEntryAdapter adapter;
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        // Initialize database helper
        dbHelper = new DatabaseHelper(requireContext());

        // Initialize date handling
        selectedDate = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault());

        // Initialize views
        entriesRecyclerView = root.findViewById(R.id.entriesRecyclerView);
        dateTextView = root.findViewById(R.id.dateTextView);
        prevDateButton = root.findViewById(R.id.prevDateButton);
        nextDateButton = root.findViewById(R.id.nextDateButton);
        calendarButton = root.findViewById(R.id.calendarButton);
        emptyStateTextView = root.findViewById(R.id.emptyStateTextView);
        totalIntakeTextView = root.findViewById(R.id.totalIntakeTextView);

        // Set up RecyclerView
        entriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up date navigation
        setupDateNavigation();

        // Load today's entries initially
        updateDateDisplay();
        loadEntriesForSelectedDate();

        return root;
    }

    private void setupDateNavigation() {
        prevDateButton.setOnClickListener(v -> {
            selectedDate.add(Calendar.DAY_OF_YEAR, -1);
            updateDateDisplay();
            loadEntriesForSelectedDate();
        });

        nextDateButton.setOnClickListener(v -> {
            // Don't allow selecting future dates
            Calendar today = Calendar.getInstance();
            if (selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                selectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                return;
            }
            
            selectedDate.add(Calendar.DAY_OF_YEAR, 1);
            updateDateDisplay();
            loadEntriesForSelectedDate();
        });

        calendarButton.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateDisplay();
                    loadEntriesForSelectedDate();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set the maximum date to today
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        dateTextView.setText(dateFormat.format(selectedDate.getTime()));
        
        // Disable next button if today is selected
        Calendar today = Calendar.getInstance();
        boolean isToday = selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                selectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
        
        nextDateButton.setEnabled(!isToday);
        nextDateButton.setAlpha(isToday ? 0.5f : 1.0f);
    }

    private void loadEntriesForSelectedDate() {
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = apiDateFormat.format(selectedDate.getTime());
        
        List<HydrationEntry> entries = dbHelper.getEntriesForDate(dateString);
        
        // Show/hide empty state
        if (entries.isEmpty()) {
            emptyStateTextView.setVisibility(View.VISIBLE);
            entriesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.GONE);
            entriesRecyclerView.setVisibility(View.VISIBLE);
        }
        
        // Update or create adapter
        if (adapter == null) {
            adapter = new HydrationEntryAdapter(entries, this);
            entriesRecyclerView.setAdapter(adapter);
        } else {
            adapter.updateEntries(entries);
        }
        
        // Update total intake
        int totalIntake = dbHelper.getDayTotalIntake(dateString);
        totalIntakeTextView.setText(getString(R.string.total_intake, totalIntake));
    }

    @Override
    public void onEntryDelete(int position, HydrationEntry entry) {
        dbHelper.deleteHydrationEntry(entry.getId());
        loadEntriesForSelectedDate();
    }
} 