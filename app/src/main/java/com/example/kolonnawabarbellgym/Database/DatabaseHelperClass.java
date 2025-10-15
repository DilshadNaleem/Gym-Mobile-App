package com.example.kolonnawabarbellgym.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelperClass extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "Gym_DB";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_USER_TABLE = "CREATE TABLE users(" +
            "userid INTEGER PRIMARY KEY AUTOINCREMENT," +
            "unique_id TEXT," +
            "firstName TEXT," +
            "lastName TEXT," +
            "email TEXT," +
            "phoneNumber TEXT," +
            "nic TEXT," +
            "password TEXT," +
            "status INTEGER DEFAULT 0," +
            "loggedIn TEXT DEFAULT 'unverify');";

    public DatabaseHelperClass (@Nullable Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // It's good practice to handle database upgrades properly
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS users");
        onCreate(sqLiteDatabase);
    }

    public SQLiteDatabase openDB()
    {
        return this.getWritableDatabase();
    }
}