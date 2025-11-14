package com.example.kolonnawabarbellgym.DatabaseController;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.kolonnawabarbellgym.DTO.User;
import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;

import java.io.ByteArrayOutputStream;

public class ProfileController
{
    private DatabaseHelperClass dbHelper;

    public ProfileController(DatabaseHelperClass dbHelper)
    {
        this.dbHelper = dbHelper;
    }

    public User getUserByEmail (String email)
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(
                "users",
                null,
                "email=?",
                new String[]{email},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst())
        {
            user = new User();
            user.setUserid(cursor.getInt(cursor.getColumnIndexOrThrow("userid")));
            user.setUnique_id(cursor.getString(cursor.getColumnIndexOrThrow("unique_id")));
            user.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow("firstName")));
            user.setLastName(cursor.getString(cursor.getColumnIndexOrThrow("lastName")));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            user.setPhoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("phoneNumber")));
            user.setNic(cursor.getString(cursor.getColumnIndexOrThrow("nic")));

            // Get profile image if exists
            byte[] imageBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("profileImage"));
            user.setProfileImagge(imageBytes);

            cursor.close();
        }
        return user;
    }

    public boolean updateUserProfile(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("firstName", user.getFirstName());
        values.put("lastName", user.getLastName());
        values.put("phoneNumber", user.getPhoneNumber());
        values.put("nic", user.getNic());

        if (user.getProfileImagge() != null) {
            values.put("profileImage", user.getProfileImagge());
        }

        int rowsAffected = db.update("users", values, "email=?", new String[]{user.getEmail()});
        return rowsAffected > 0;
    }

    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    // Convert byte array to bitmap
    public Bitmap byteArrayToBitmap(byte[] byteArray) {
        if (byteArray == null) return null;
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}
