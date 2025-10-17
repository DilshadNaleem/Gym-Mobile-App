package com.example.kolonnawabarbellgym.DatabaseController;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;

public class LoginController
{
    private DatabaseHelperClass dbHelper;

    public LoginController(DatabaseHelperClass dbHelper)
    {
        this.dbHelper = dbHelper;
    }

    public boolean loginUser(String email, String password)
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {"userid"};
        String selection = "email = ? AND password = ? AND status = ?";
        String[] selectionArgs = {email,password, "1"};

        Cursor cursor = db.query("users", columns, selection, selectionArgs, null, null, null);
        boolean loginSuccess = cursor.getCount() > 0;

        if (loginSuccess)
        {
            updateLoggedInStatus(db,email,1);
        }

        cursor.close();
        db.close();

        return loginSuccess;
    }


    private void updateLoggedInStatus(SQLiteDatabase db, String email, int status)
    {
        ContentValues values = new ContentValues();
        values.put("loggedIn", status);

        db.update("users", values, "email = ?", new String[] {email});
    }

    public void logoutUser(String email)
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        updateLoggedInStatus(db, email, 0);
        db.close();
    }
}
