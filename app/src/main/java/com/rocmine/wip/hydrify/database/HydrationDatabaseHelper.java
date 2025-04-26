package com.rocmine.wip.hydrify.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rocmine.wip.hydrify.model.HydrationEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HydrationDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "hydration.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_HYDRATION = "hydration_entries";

    // Column names
    private static final String KEY_ID = "id";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_AMOUNT = "amount_ml";
    private static final String KEY_TYPE = "type";

    // Date format for storing dates
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public HydrationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_HYDRATION_TABLE = "CREATE TABLE " + TABLE_HYDRATION + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TIMESTAMP + " TEXT,"
                + KEY_AMOUNT + " INTEGER,"
                + KEY_TYPE + " TEXT"
                + ")";
        db.execSQL(CREATE_HYDRATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HYDRATION);
        
        // Create tables again
        onCreate(db);
    }

    // Adding new hydration entry
    public long addHydrationEntry(HydrationEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(KEY_TIMESTAMP, dateFormat.format(entry.getTimestamp()));
        values.put(KEY_AMOUNT, entry.getAmountMl());
        values.put(KEY_TYPE, entry.getType());
        
        // Insert row
        long id = db.insert(TABLE_HYDRATION, null, values);
        db.close();
        
        return id;
    }

    // Getting single hydration entry
    public HydrationEntry getHydrationEntry(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        HydrationEntry entry = new HydrationEntry();
        
        Cursor cursor = db.query(TABLE_HYDRATION, new String[] { KEY_ID, KEY_TIMESTAMP, KEY_AMOUNT, KEY_TYPE },
                KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);
        
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
        
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_HYDRATION + " ORDER BY " + KEY_TIMESTAMP + " DESC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        // Loop through all rows and add to list
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
        List<HydrationEntry> entryList = new ArrayList<>();
        
        // Get today's date in the format "yyyy-MM-dd"
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dayFormat.format(new Date());
        
        // Get entries for today
        entryList = getEntriesForDate(today);
        
        return entryList;
    }
    
    // Get entries for a specific date
    public List<HydrationEntry> getEntriesForDate(String dateString) {
        List<HydrationEntry> entryList = new ArrayList<>();
        
        // Query to get entries for the specified date
        String selectQuery = "SELECT * FROM " + TABLE_HYDRATION + 
                            " WHERE " + KEY_TIMESTAMP + " LIKE '" + dateString + "%'" +
                            " ORDER BY " + KEY_TIMESTAMP + " DESC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        // Loop through all rows and add to list
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
        // Get today's date in the format "yyyy-MM-dd"
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dayFormat.format(new Date());
        
        return getDayTotalIntake(today);
    }
    
    // Get total intake for a specific day
    public int getDayTotalIntake(String dateString) {
        int total = 0;
        
        // Query to sum up intake for the specified date
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
        db.delete(TABLE_HYDRATION, KEY_ID + " = ?", new String[] { String.valueOf(id) });
        db.close();
    }
} 