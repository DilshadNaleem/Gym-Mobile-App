package com.example.kolonnawabarbellgym.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelperClass extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "Gym_DB";
    private static final int DATABASE_VERSION = 6;

    private static final String CREATE_USER_TABLE = "CREATE TABLE users(" +
            "userid INTEGER PRIMARY KEY AUTOINCREMENT," +
            "unique_id TEXT," +
            "firstName TEXT," +
            "lastName TEXT," +
            "email TEXT," +
            "phoneNumber TEXT," +
            "nic TEXT," +
            "password TEXT," +
            "profileImage BLOB," +
            "status INTEGER DEFAULT 0," +
            "loggedIn TEXT DEFAULT 'unverify');";

    private static final String CREATE_NEW_USER_TABLE = "CREATE TABLE new_users(" +
            "userid INTEGER PRIMARY KEY AUTOINCREMENT," +
            "unique_id TEXT," +
            "firstName TEXT," +
            "lastName TEXT," +
            "email TEXT," +
            "phoneNumber TEXT," +
            "nic TEXT," +
            "profileImage BLOB," +
            "monthlyFee TEXT," +
            "status INTEGER DEFAULT 0," +
            "created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

    private static final String CREATE_PAYMENT_TABLE = "CREATE TABLE payment(" +
            "payment_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "unique_id TEXT," +
            "firstname TEXT," +
            "lastname TEXT," +
            "month TEXT," +
            "price REAL," +
            "handovered_to TEXT," +
            "sessioned_email TEXT," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (unique_id) REFERENCES new_users(unique_id));";


    // Add these methods to your DatabaseHelperClass
    public Cursor getAllNewUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query("new_users",
                null,
                null,
                null,
                null, null, "created_time DESC");
    }

    public Cursor searchNewUsers(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = "firstName LIKE ? OR lastName LIKE ? OR email LIKE ? OR phoneNumber LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%", "%" + query + "%"};

        return db.query("new_users",
                null,
                selection,
                selectionArgs,
                null, null, "created_time DESC");
    }

    public boolean updateUserMonthlyFee(String userUniqueId, String monthlyFee) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("monthlyFee", monthlyFee);

        int result = db.update(
                "new_users",
                values,
                "unique_id = ?",
                new String[]{userUniqueId}
        );

        return result > 0;
    }

    public Cursor getNewUserByUniqueId(String uniqueId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query("new_users",
                null,
                "unique_id =?",
                new String[]{uniqueId},
                null, null, null);
    }

    public DatabaseHelperClass (@Nullable Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_NEW_USER_TABLE);
        db.execSQL(CREATE_PAYMENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       db.execSQL("DROP TABLE IF EXISTS users");
       db.execSQL("DROP TABLE IF EXISTS new_users");
       db.execSQL("DROP TABLE IF EXISTS payment");
        onCreate(db);
    }

    public List<String> getPaidMonthsForUser(String userUniqueId) {
        List<String> paidMonths = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                "payment",
                new String[]{"month"},
                "unique_id = ?",
                new String[]{userUniqueId},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            do {
                paidMonths.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return paidMonths;
    }

    public boolean savePaymentRecord(String userUniqueId, String userName,
                                     String monthYear, String amount, String handoveredBy) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("unique_id", userUniqueId);
        values.put("firstname", userName);
        values.put("month", monthYear);
        values.put("price", amount);
        values.put("handovered_to", handoveredBy);

        long result = db.insert("payment", null, values);
        return result != -1;
    }



    public SQLiteDatabase openDB()
    {
        return this.getWritableDatabase();
    }

    public Cursor getUserByEmail(String email)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query("users",
                null,
                "email =? ",
                new String[]{email},
                null, null, null);
    }

    public boolean updateUserProfile(String email, String firstName, String lastName, String phoneNumber, String nic)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("firstName", firstName);
        values.put("lastName", lastName);
        values.put("phoneNumber", phoneNumber);
        values.put("nic", nic);

        int rowsEffected = db.update("users",values, "email=?", new String[]{email});
        return rowsEffected > 0;
    }

    public boolean updateUserProfileWithImage(String email, String firstName, String lastName, String phoneNumber, String nic, byte[] profileImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("firstName", firstName);
        values.put("lastName", lastName);
        values.put("phoneNumber", phoneNumber);
        values.put("nic", nic);
        if (profileImage != null) {
            values.put("profileImage", profileImage);
        }

        int rowsAffected = db.update("users", values, "email=?", new String[]{email});
        return rowsAffected > 0;
    }
}