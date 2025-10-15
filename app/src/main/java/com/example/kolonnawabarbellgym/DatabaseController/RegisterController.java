package com.example.kolonnawabarbellgym.DatabaseController;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.kolonnawabarbellgym.DTO.User;
import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;

import java.util.UUID;

public class RegisterController
{
    private DatabaseHelperClass dbHelper;

    public RegisterController(DatabaseHelperClass dbHelper) {
        this.dbHelper = dbHelper;
    }


    public boolean registerUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Check if email already exists
        if (isEmailExists(db, user.getEmail(),1)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put("unique_id", UUID.randomUUID().toString());
        values.put("firstName", user.getFirstName());
        values.put("lastName", user.getLastName());
        values.put("email", user.getEmail());
        values.put("phoneNumber", user.getPhoneNumber());
        values.put("nic", user.getNic());
        values.put("password", user.getPassword());
        values.put("status", user.getStatus());
        values.put("loggedIn", user.getLoggedIn());

        long result = db.insert("users", null, values);
        db.close();

        return result != -1;
    }

    private boolean isEmailExists(SQLiteDatabase db, String email, int status) {
        Cursor cursor = db.query("users",
                new String[]{"userid"},
                "email=? AND status = ?",
                new String[]{email, String.valueOf(status)},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

}
