package com.rocmine.wip.hydrify;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rocmine.wip.hydrify.util.HydrationTracker;

public class MainActivity extends AppCompatActivity {

    // Hydration tracker
    private HydrationTracker hydrationTracker;

    private NavController navController;

    // Permission launcher for notifications
    private ActivityResultLauncher<String> requestPermissionLauncher;

    // Theme constants
    public static final String THEME_PREF_KEY = "theme_preference";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_SYSTEM = "system";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register permission launcher first
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    // Handle permission result if needed
                });

        // Show splash screen layout first
        setContentView(R.layout.activity_splash);

        // Wait for 3 seconds then show main layout
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Apply dynamic colors if available (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                DynamicColors.applyToActivityIfAvailable(this);
            }

            // Set the theme based on preferences
            applyThemeFromPreferences();

            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_main);

            // Check for notification permission
            checkNotificationPermission();

            // Set up edge-to-edge display
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            // Initialize hydration tracker
            hydrationTracker = new HydrationTracker(this);

            // Set up the NavController correctly
            FragmentContainerView navHostFragment = findViewById(R.id.nav_host_fragment);
            if (navHostFragment != null) {
                navController = ((NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment))
                        .getNavController();

                // Set up the bottom navigation view
                BottomNavigationView navView = findViewById(R.id.nav_view);
                FloatingActionButton addFab = findViewById(R.id.fab_add);

                // Set up the top-level destinations
                AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                        R.id.navigation_dashboard, R.id.navigation_history, R.id.navigation_settings)
                        .build();

                // Since we're not using a toolbar, just set up the bottom nav
                NavigationUI.setupWithNavController(navView, navController);

                // Set up FAB click listener
                addFab.setOnClickListener(v -> {
                    navController.navigate(R.id.navigation_add);
                });

                // Hide the FAB when in the Add fragment
                navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                    if (destination.getId() == R.id.navigation_add) {
                        addFab.setVisibility(View.GONE);
                        navView.setVisibility(View.GONE);
                    } else {
                        addFab.setVisibility(View.VISIBLE);
                        navView.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 300); // 3000 milliseconds = 3 seconds
    }

    /**
     * Apply theme based on saved preferences
     */
    private void applyThemeFromPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String themePreference = preferences.getString(THEME_PREF_KEY, THEME_SYSTEM);

        switch (themePreference) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    /**
     * Change the app theme programmatically
     * 
     * @param themeName The theme to apply (light, dark, or system)
     */
    public void setAppTheme(String themeName) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(THEME_PREF_KEY, themeName);
        editor.apply();

        applyThemeFromPreferences();
    }

    // Check and request notification permission for Android 13+
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }
}