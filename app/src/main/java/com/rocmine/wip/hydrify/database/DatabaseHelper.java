package com.rocmine.wip.hydrify.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rocmine.wip.hydrify.model.HydrationEntry;
import com.rocmine.wip.hydrify.model.UserProfile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "hydrify.db";
    private static final int DATABASE_VERSION = 2;

    // Table names
    private static final String TABLE_HYDRATION = "hydration_entries";
    private static final String TABLE_USER_PROFILE = "user_profile";

    // Hydration table column names
    private static final String KEY_HYDRATION_ID = "id";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_AMOUNT = "amount_ml";
    private static final String KEY_TYPE = "type";

    // User profile table column names
    private static final String KEY_PROFILE_ID = "id";
    private static final String KEY_WEIGHT = "weight";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_AGE = "age";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_WAKE_TIME = "wake_time";
    private static final String KEY_SLEEP_TIME = "sleep_time";
    private static final String KEY_DAILY_GOAL = "daily_goal";

    // Date format for storing dates
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create hydration entries table
        String CREATE_HYDRATION_TABLE = "CREATE TABLE " + TABLE_HYDRATION + "("
                + KEY_HYDRATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TIMESTAMP + " TEXT,"
                + KEY_AMOUNT + " INTEGER,"
                + KEY_TYPE + " TEXT"
                + ")";

        // Create user profile table
        String CREATE_USER_PROFILE_TABLE = "CREATE TABLE " + TABLE_USER_PROFILE + "("
                + KEY_PROFILE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_WEIGHT + " REAL,"
                + KEY_GENDER + " TEXT,"
                + KEY_AGE + " INTEGER,"
                + KEY_HEIGHT + " REAL,"
                + KEY_WAKE_TIME + " TEXT,"
                + KEY_SLEEP_TIME + " TEXT,"
                + KEY_DAILY_GOAL + " INTEGER"
                + ")";

        db.execSQL(CREATE_HYDRATION_TABLE);
        db.execSQL(CREATE_USER_PROFILE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HYDRATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PROFILE);

        // Create tables again
        onCreate(db);
    }

    // ===== Hydration Entry Methods =====

    // Adding new hydration entry
    public long addHydrationEntry(HydrationEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIMESTAMP, dateFormat.format(entry.getTimestamp()));
        values.put(KEY_AMOUNT, entry.getAmountMl());
        values.put(KEY_TYPE, entry.getType());

        long id = db.insert(TABLE_HYDRATION, null, values);
        db.close();

        return id;
    }

    // Getting single hydration entry
    public HydrationEntry getHydrationEntry(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        HydrationEntry entry = new HydrationEntry();

        Cursor cursor = db.query(TABLE_HYDRATION,
                new String[] { KEY_HYDRATION_ID, KEY_TIMESTAMP, KEY_AMOUNT, KEY_TYPE },
                KEY_HYDRATION_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            try {
                entry.setId(cursor.getLong(0));
                entry.setTimestamp(dateFormat.parse(cursor.getString(1)));
                entry.setAmountMl(cursor.getInt(2));
                entry.setType(cursor.getString(3));
            } catch (ParseException e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }

        db.close();
        return entry;
    }

    // Getting all hydration entries
    public List<HydrationEntry> getAllHydrationEntries() {
        List<HydrationEntry> entryList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_HYDRATION + " ORDER BY " + KEY_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                HydrationEntry entry = new HydrationEntry();
                entry.setId(cursor.getLong(0));
                try {
                    entry.setTimestamp(dateFormat.parse(cursor.getString(1)));
                } catch (ParseException e) {
                    e.printStackTrace();
                    entry.setTimestamp(new Date());
                }
                entry.setAmountMl(cursor.getInt(2));
                entry.setType(cursor.getString(3));

                entryList.add(entry);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return entryList;
    }

    // Getting entries for today
    public List<HydrationEntry> getTodaysEntries() {
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dayFormat.format(new Date());
        return getEntriesForDate(today);
    }

    // Get entries for a specific date
    public List<HydrationEntry> getEntriesForDate(String dateString) {
        List<HydrationEntry> entryList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_HYDRATION +
                " WHERE " + KEY_TIMESTAMP + " LIKE '" + dateString + "%'" +
                " ORDER BY " + KEY_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                HydrationEntry entry = new HydrationEntry();
                entry.setId(cursor.getLong(0));
                try {
                    entry.setTimestamp(dateFormat.parse(cursor.getString(1)));
                } catch (ParseException e) {
                    e.printStackTrace();
                    entry.setTimestamp(new Date());
                }
                entry.setAmountMl(cursor.getInt(2));
                entry.setType(cursor.getString(3));

                entryList.add(entry);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return entryList;
    }

    // Get total intake for today
    public int getTodayTotalIntake() {
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dayFormat.format(new Date());
        return getDayTotalIntake(today);
    }

    // Get total intake for a specific day
    public int getDayTotalIntake(String dateString) {
        int total = 0;

        String selectQuery = "SELECT SUM(" + KEY_AMOUNT + ") FROM " + TABLE_HYDRATION +
                " WHERE " + KEY_TIMESTAMP + " LIKE '" + dateString + "%'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            total = cursor.getInt(0);
        }

        cursor.close();
        db.close();

        return total;
    }

    // Delete a hydration entry
    public void deleteHydrationEntry(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HYDRATION, KEY_HYDRATION_ID + " = ?", new String[] { String.valueOf(id) });
        db.close();
    }

    // ===== User Profile Methods =====

    // Insert a new user profile
    public long insertUserProfile(UserProfile profile) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_WEIGHT, profile.getWeight());
        values.put(KEY_GENDER, profile.getGender());
        values.put(KEY_AGE, profile.getAge());
        values.put(KEY_HEIGHT, profile.getHeight());
        values.put(KEY_WAKE_TIME, profile.getWakeTime());
        values.put(KEY_SLEEP_TIME, profile.getSleepTime());
        values.put(KEY_DAILY_GOAL, profile.getDailyGoal());

        long id = db.insert(TABLE_USER_PROFILE, null, values);
        db.close();
        return id;
    }

    // Get user profile
    public UserProfile getUserProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        UserProfile profile = null;

        Cursor cursor = db.query(TABLE_USER_PROFILE, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            profile = new UserProfile();
            profile.setId(cursor.getLong(cursor.getColumnIndex(KEY_PROFILE_ID)));
            profile.setWeight(cursor.getDouble(cursor.getColumnIndex(KEY_WEIGHT)));
            profile.setGender(cursor.getString(cursor.getColumnIndex(KEY_GENDER)));
            profile.setAge(cursor.getInt(cursor.getColumnIndex(KEY_AGE)));
            profile.setHeight(cursor.getDouble(cursor.getColumnIndex(KEY_HEIGHT)));
            profile.setWakeTime(cursor.getString(cursor.getColumnIndex(KEY_WAKE_TIME)));
            profile.setSleepTime(cursor.getString(cursor.getColumnIndex(KEY_SLEEP_TIME)));
            profile.setDailyGoal(cursor.getInt(cursor.getColumnIndex(KEY_DAILY_GOAL)));
            cursor.close();
        }

        db.close();
        return profile;
    }

    // Update user profile
    public int updateUserProfile(UserProfile profile) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_WEIGHT, profile.getWeight());
        values.put(KEY_GENDER, profile.getGender());
        values.put(KEY_AGE, profile.getAge());
        values.put(KEY_HEIGHT, profile.getHeight());
        values.put(KEY_WAKE_TIME, profile.getWakeTime());
        values.put(KEY_SLEEP_TIME, profile.getSleepTime());
        values.put(KEY_DAILY_GOAL, profile.getDailyGoal());

        int result = db.update(TABLE_USER_PROFILE, values, KEY_PROFILE_ID + " = ?",
                new String[] { String.valueOf(profile.getId()) });
        db.close();
        return result;
    }
}