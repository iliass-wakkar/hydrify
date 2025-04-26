package com.rocmine.wip.hydrify.ui.add;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.rocmine.wip.hydrify.R;
import com.rocmine.wip.hydrify.util.HydrationTracker;

public class AddFragment extends Fragment {

    private SeekBar amountSeekBar;
    private TextView amountTextView;
    private EditText customAmountEditText;
    private Spinner drinkTypeSpinner;
    private Button addButton;
    private Button cancelButton;
    private Button presetSmallButton;
    private Button presetMediumButton;
    private Button presetLargeButton;

    private HydrationTracker hydrationTracker;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add, container, false);

        // Initialize navigation controller
        navController = NavHostFragment.findNavController(this);

        // Initialize hydration tracker
        hydrationTracker = new HydrationTracker(requireContext());

        // Initialize views
        amountSeekBar = root.findViewById(R.id.amountSeekBar);
        amountTextView = root.findViewById(R.id.amountTextView);
        customAmountEditText = root.findViewById(R.id.customAmountEditText);
        drinkTypeSpinner = root.findViewById(R.id.drinkTypeSpinner);
        addButton = root.findViewById(R.id.addButton);
        cancelButton = root.findViewById(R.id.cancelButton);
        presetSmallButton = root.findViewById(R.id.presetSmallButton);
        presetMediumButton = root.findViewById(R.id.presetMediumButton);
        presetLargeButton = root.findViewById(R.id.presetLargeButton);

        setupSpinner();
        setupSeekBar();
        setupButtons();

        return root;
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.drink_types,
                R.layout.spinner_item
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        drinkTypeSpinner.setAdapter(adapter);
    }

    private void setupSeekBar() {
        // Set min to 50ml and max to 1000ml
        amountSeekBar.setMax(950);
        amountSeekBar.setProgress(200);  // Default 250ml (50 + 200)
        updateAmountTextView(250);

        amountSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Add 50 to the progress to set the minimum value to 50ml
                int actualAmount = progress + 50;
                updateAmountTextView(actualAmount);
                
                // Clear the custom amount field when using the slider
                if (fromUser) {
                    customAmountEditText.setText("");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
        });
    }

    private void setupButtons() {
        presetSmallButton.setOnClickListener(v -> {
            amountSeekBar.setProgress(100);  // 150ml
            customAmountEditText.setText("");
        });

        presetMediumButton.setOnClickListener(v -> {
            amountSeekBar.setProgress(200);  // 250ml
            customAmountEditText.setText("");
        });

        presetLargeButton.setOnClickListener(v -> {
            amountSeekBar.setProgress(450);  // 500ml
            customAmountEditText.setText("");
        });

        addButton.setOnClickListener(v -> addHydrationEntry());

        cancelButton.setOnClickListener(v -> navController.navigateUp());
    }

    private void updateAmountTextView(int amount) {
        amountTextView.setText(getString(R.string.amount_ml, amount));
    }

    private void addHydrationEntry() {
        int amount;
        String customAmountStr = customAmountEditText.getText().toString().trim();
        
        if (!TextUtils.isEmpty(customAmountStr)) {
            try {
                amount = Integer.parseInt(customAmountStr);
                if (amount <= 0) {
                    Toast.makeText(requireContext(), R.string.enter_valid_amount, Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), R.string.enter_valid_amount, Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            // Use the seek bar value
            amount = amountSeekBar.getProgress() + 50;
        }

        String type = drinkTypeSpinner.getSelectedItem().toString();

        // Add the entry
        hydrationTracker.addHydrationEntry(amount, type);
        
        // Show success message
        Toast.makeText(requireContext(), getString(R.string.added_amount, amount, type), Toast.LENGTH_SHORT).show();
        
        // Navigate back
        navController.navigateUp();
    }
} 