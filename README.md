<div align="center">

# 💧 Hydrify: IoT Hydration Ecosystem & Native Android App

**Connected Health Platform with ESP32 Load-Cell Telemetry, Metabolic Formulas & Offline SQLite**

[![Android](https://img.shields.io/badge/Platform-Android%20SDK-3DDC84.svg?logo=android&logoColor=white)](https://developer.android.com/)
[![Java](https://img.shields.io/badge/Language-Java-ED8B00.svg?logo=java&logoColor=white)](https://www.java.com/)
[![SQLite](https://img.shields.io/badge/Database-SQLite%20Offline--First-003B57.svg?logo=sqlite&logoColor=white)](https://www.sqlite.org/)
[![Hardware](https://img.shields.io/badge/Hardware-ESP32%20Microcontroller-E7352C.svg?logo=espressif&logoColor=white)](https://www.espressif.com/)
[![AlarmManager](https://img.shields.io/badge/Scheduler-Doze--Aware%20AlarmManager-blue.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

*Co-Engineered by **[Iliass Wakkar](https://www.wakkar.net)***

---

</div>

## 📌 Executive Summary

Maintaining adequate daily hydration is crucial for metabolic equilibrium, physical recovery, and cognitive focus. Yet, the vast majority of consumer hydration trackers rely entirely on manual user logging—a high-friction habit that experiences rapid attrition and inaccurate estimations.

**Hydrify** closes the loop between physical fluid intake and digital health telemetry. By pairing custom load-cell containers powered by an **ESP32 microcontroller** with a native **Android client engineered in Java (`com.rocmine.wip.hydrify`)**, Hydrify captures real-time fluid consumption telemetry, logs metrics in an offline-first **SQLite database (`hydrify.db`)**, calculates personalized daily intake targets using biometric formulas, and dispatches sleep-aware reminders.

---

## 🏗️ System Architecture

```
┌────────────────────────────────────────────────────────┐
│               Physical Container Layer                 │
│      Strain Gauge Load Cell + HX711 24-bit ADC         │
└───────────────────────────┬────────────────────────────┘
                            │ Raw Analog Signal (mV)
                            ▼
┌────────────────────────────────────────────────────────┐
│             ESP32 Microcontroller (C++)                │
│   ├── Weight Tare & Calibration Filtering              │
│   ├── Fluid Delta Delta-V Estimation (ml)              │
│   └── Wi-Fi HTTP POST / MQTT Telemetry Packet          │
└───────────────────────────┬────────────────────────────┘
                            │ JSON Telemetry Payload
                            ▼
┌────────────────────────────────────────────────────────┐
│           Android Mobile Application (Java)            │
│   ├── Onboarding & Biometric Profile Setup             │
│   ├── Dynamic Metabolic Daily Goal Calculation         │
│   ├── Offline-First SQLite Persistence (`hydrify.db`)  │
│   └── Sleep-Aware Exact AlarmManager & Notifications   │
└────────────────────────────────────────────────────────┘
```

---

## ⚡ Core Technical Features

### 1. Dynamic Biometric Intake Calculation
Rather than enforcing a generic, one-size-fits-all 2,000ml goal, `HydrationTracker.java` computes a personalized hydration quota based on user body weight and self-reported physical activity tiers:

$$\text{Daily Intake Goal (ml)} = \text{Weight (kg)} \times \mu_{\text{activity}}$$

Where the metabolic multiplier $\mu$ is defined as:
- **Low Activity**: $30 \text{ ml/kg}$
- **Moderate Activity**: $35 \text{ ml/kg}$
- **High / Athletic Activity**: $40 \text{ ml/kg}$

```java
public int calculateRecommendedDailyIntake() {
    int weight = prefs.getInt(KEY_WEIGHT, DEFAULT_WEIGHT);
    String activityLevel = prefs.getString(KEY_ACTIVITY_LEVEL, DEFAULT_ACTIVITY_LEVEL);
    
    double multiplier;
    switch (activityLevel) {
        case "low":
            multiplier = 30;
            break;
        case "high":
            multiplier = 40;
            break;
        case "moderate":
        default:
            multiplier = 35;
            break;
    }
    
    int recommendedMl = (int) (weight * multiplier);
    setDailyGoal(recommendedMl);
    return recommendedMl;
}
```

---

### 2. Sleep-Aware & Doze-Proof AlarmManager
Standard reminder applications often wake users during the night or get silenced by Android's aggressive battery-saving **Doze Mode**. Hydrify resolves both challenges:

```
┌────────────────────────────────────────────────────────┐
│             Schedule Next Reminder Event               │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
              ┌───────────────────────────┐
              │ Is Current Time in Sleep  │
              │  Window (Sleep to Wake)?  │
              └─────────────┬─────────────┘
                     Yes    │    No
            ┌───────────────┴───────────────┐
            ▼                               ▼
┌───────────────────────┐       ┌────────────────────────┐
│ Suppress Night Alarms │       │ Schedule Exact Interval│
│ Target: Tomorrow Wake │       │ (e.g. 60 Minutes)      │
└───────────┬───────────┘       └───────────┬────────────┘
            │                               │
            └───────────────┬───────────────┘
                            ▼
┌────────────────────────────────────────────────────────┐
│     AlarmManager.setExactAndAllowWhileIdle()           │
│     (Android 12+ canScheduleExactAlarms Verified)      │
└────────────────────────────────────────────────────────┘
```

- **Circadian Window Suppression**: Automatically inspects `sleep_time` (e.g., 22:00) and `wake_time` (e.g., 08:00), holding alerts until the morning wakeup.
- **Doze Piercing**: Utilizes `setExactAndAllowWhileIdle()` on Android 6.0+ to ensure crucial health prompts execute reliably while idling.

---

### 3. Offline-First Relational Architecture (`hydrify.db`)
Using `DatabaseHelper.java`, Hydrify operates completely offline without recurring cloud subscription dependencies:
- **`user_profile`**: Stores demographic attributes, sleep cycles, and daily targets.
- **`hydration_entries`**: Stores timestamped consumption events with beverage classification (water, electrolytes, tea, juice).
- **Sub-Millisecond Aggregation**: Daily totals are calculated directly using optimized SQL wildcard date queries:
  ```sql
  SELECT SUM(amount_ml) FROM hydration_entries 
  WHERE timestamp LIKE '2026-10-11%';
  ```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog / Iguana / Jellyfish (or newer)
- Android SDK 24+ (target SDK 34)
- JDK 17+

### Installation & Build

1. **Clone the repository**:
   ```bash
   git clone https://github.com/iliass-wakkar/hydrify.git
   cd hydrify
   ```

2. **Open in Android Studio**:
   - Open Android Studio $\to$ `File` $\to$ `Open` $\to$ Select `hydrify` folder.
   - Allow Gradle to sync dependencies.

3. **Run on Device or Emulator**:
   - Connect an Android device with USB debugging enabled.
   - Click **Run** (`Shift + F10`).

---

## 📂 Repository Structure

```
hydrify/
├── app/src/main/
│   ├── java/com/rocmine/wip/hydrify/
│   │   ├── adapter/          # Recycler view adapters for consumption logs
│   │   ├── database/         # SQLite DatabaseHelper & CRUD operations
│   │   ├── model/            # HydrationEntry & UserProfile data models
│   │   ├── receiver/         # AlarmManager broadcast receivers
│   │   ├── ui/               # Activities & Fragments (Dashboard, History, Settings)
│   │   └── util/             # HydrationTracker business logic & formulas
│   ├── res/                  # Layouts, drawables, strings & values
│   └── AndroidManifest.xml   # Permissions (Exact Alarms, Notifications)
├── build.gradle.kts          # Project build configuration
├── gradlew / gradlew.bat     # Gradle wrapper scripts
├── LICENSE                   # MIT Open Source License
└── README.md                 # Technical architecture documentation
```

---

## 👥 Contributors

- **Iliass Wakkar** — [wakkar.net](https://www.wakkar.net) | [@iliass-wakkar](https://github.com/iliass-wakkar)
- **Mohamed Amine Rochdi**

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.