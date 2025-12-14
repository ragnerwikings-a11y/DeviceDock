package com.example.devicedock.data.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DeviceDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "device_dock.db";
    private static final int DATABASE_VERSION = 1;

    // Table and Columns
    private static final String TABLE_DEVICES = "devices";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_IP = "ip_address";
    private static final String COLUMN_TYPE = "service_type";

    public DeviceDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_DEVICES_TABLE = "CREATE TABLE " +
                TABLE_DEVICES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL UNIQUE, " + // Name is unique
                COLUMN_IP + " TEXT NOT NULL, " +
                COLUMN_TYPE + " TEXT NOT NULL" +
                ");";
        db.execSQL(SQL_CREATE_DEVICES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
        onCreate(db);
    }

    // --- CRUD Operations ---

    public long saveDevice(Device device) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, device.getName());
        cv.put(COLUMN_IP, device.getIpAddress());
        cv.put(COLUMN_TYPE, device.getServiceType());

        long result = db.insertWithOnConflict(TABLE_DEVICES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);

        return result;
    }

    public List<Device> getAllDevices() {
        List<Device> deviceList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_DEVICES,
                null, null, null, null, null,
                COLUMN_NAME + " ASC"
        );

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int nameIndex = cursor.getColumnIndex(COLUMN_NAME);
            int ipIndex = cursor.getColumnIndex(COLUMN_IP);
            int typeIndex = cursor.getColumnIndex(COLUMN_TYPE);

            do {
                long id = cursor.getLong(idIndex);
                String name = cursor.getString(nameIndex);
                String ip = cursor.getString(ipIndex);
                String type = cursor.getString(typeIndex);

                deviceList.add(new Device(id, name, ip, type, false));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return deviceList;
    }
}
