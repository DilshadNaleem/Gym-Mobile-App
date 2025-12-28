package com.example.kolonnawabarbellgym.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.kolonnawabarbellgym.Model.OtherAmount;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DatabaseHelperClass extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "Gym_DB";
    private static final int DATABASE_VERSION = 13;

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
            "other_description TEXT," +
            "other_amount REAL,"+
            "FOREIGN KEY (unique_id) REFERENCES new_users(unique_id));";

    private static final String CREATE_EXPENSE_TABLE = "CREATE TABLE expenses(" +
            "expense_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "description TEXT NOT NULL," +
            "image BLOB," +
            "price REAL NOT NULL," +
            "created_at TIMESTAMP DEFAULT (datetime('now', 'localtime')));";

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
        db.execSQL(CREATE_EXPENSE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Create expenses table if it doesn't exist (for versions that didn't have it)
        if (oldVersion < DATABASE_VERSION) {
            db.execSQL("CREATE TABLE IF NOT EXISTS expenses(" +
                    "expense_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "description TEXT NOT NULL," +
                    "image BLOB," +
                    "price REAL NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);");

            db.execSQL("CREATE TABLE IF NOT EXISTS payment(" +
                    "payment_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "unique_id TEXT," +
                    "firstname TEXT," +
                    "lastname TEXT," +
                    "month TEXT," +
                    "price REAL," +
                    "handovered_to TEXT," +
                    "sessioned_email TEXT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "other_description TEXT," +
                    "other_amount REAL," +
                    "FOREIGN KEY (unique_id) REFERENCES new_users(unique_id))");
        }


        //db.execSQL("DROP TABLE IF EXISTS payment");

        // Remove the categories table logic since you don't need it
    }

    // Add these missing methods:

    public boolean exportDatabase(String destinationPath) {
        try {
            File currentDB = new File(getReadableDatabase().getPath());
            File backupDB = new File(destinationPath);

            if (currentDB.exists()) {
                // Create parent directories if they don't exist
                File parentDir = backupDB.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }

                java.io.FileInputStream fis = new java.io.FileInputStream(currentDB);
                java.io.FileOutputStream fos = new java.io.FileOutputStream(backupDB);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }

                fos.flush();
                fos.close();
                fis.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean importDatabase(String sourcePath) {
        try {
            File currentDB = new File(getWritableDatabase().getPath());
            File backupDB = new File(sourcePath);

            if (backupDB.exists()) {
                // Close the database before importing
                close();

                java.io.FileInputStream fis = new java.io.FileInputStream(backupDB);
                java.io.FileOutputStream fos = new java.io.FileOutputStream(currentDB);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }

                fos.flush();
                fos.close();
                fis.close();

                // Reopen the database
                getWritableDatabase();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean mergeDatabases(String sourcePath) {
        SQLiteDatabase sourceDb = null;
        SQLiteDatabase targetDb = getWritableDatabase();

        try {
            sourceDb = SQLiteDatabase.openDatabase(sourcePath, null, SQLiteDatabase.OPEN_READONLY);
            targetDb.beginTransaction();

            // Merge new_users table
            Cursor cursor = sourceDb.rawQuery("SELECT * FROM new_users", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String phone = cursor.getString(cursor.getColumnIndexOrThrow("phoneNumber"));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));

                    if (!isUserExists(targetDb, phone, email)) {
                        ContentValues values = new ContentValues();
                        values.put("unique_id", getNextAvailableMemberId(targetDb));
                        values.put("firstName", cursor.getString(cursor.getColumnIndexOrThrow("firstName")));
                        values.put("lastName", cursor.getString(cursor.getColumnIndexOrThrow("lastName")));
                        values.put("email", email);
                        values.put("phoneNumber", phone);
                        values.put("nic", cursor.getString(cursor.getColumnIndexOrThrow("nic")));
                        values.put("profileImage", cursor.getBlob(cursor.getColumnIndexOrThrow("profileImage")));
                        values.put("monthlyFee", cursor.getString(cursor.getColumnIndexOrThrow("monthlyFee")));
                        values.put("status", cursor.getInt(cursor.getColumnIndexOrThrow("status")));

                        targetDb.insert("new_users", null, values);
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }

            targetDb.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (targetDb != null) targetDb.endTransaction();
            if (sourceDb != null) sourceDb.close();
        }
    }

    private boolean isUserExists(SQLiteDatabase db, String phone, String email) {
        Cursor cursor = db.rawQuery(
                "SELECT * FROM new_users WHERE phoneNumber = ? OR email = ?",
                new String[]{phone, email}
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    private String getNextAvailableMemberId(SQLiteDatabase db) {
        String nextMemberId = null;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT unique_id FROM new_users ORDER BY userid DESC LIMIT 1", null);

            if (cursor != null && cursor.moveToFirst()) {
                String lastUniqueId = cursor.getString(cursor.getColumnIndexOrThrow("unique_id"));

                if (lastUniqueId != null && lastUniqueId.startsWith("mem_")) {
                    try {
                        String numberPart = lastUniqueId.substring(4);
                        int nextNumber = Integer.parseInt(numberPart) + 1;
                        nextMemberId = "mem_" + String.format("%02d", nextNumber);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        nextMemberId = "mem_01";
                    }
                } else {
                    nextMemberId = "mem_01";
                }
            } else {
                nextMemberId = "mem_01";
            }
        } catch (Exception e) {
            e.printStackTrace();
            nextMemberId = "mem_01";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return nextMemberId;
    }

    public StatusData getStatusData() {
        StatusData data = new StatusData();
        SQLiteDatabase db = getReadableDatabase();

        // Total members
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM new_users", null);
        if (cursor != null && cursor.moveToFirst()) {
            data.totalMembers = cursor.getInt(0);
            cursor.close();
        }

        // Database size
        File dbFile = new File(db.getPath());
        if (dbFile.exists()) {
            long sizeInBytes = dbFile.length();
            long sizeInKB = sizeInBytes / 1024;
            data.dbSize = sizeInKB + " KB";
        } else {
            data.dbSize = "0 KB";
        }

        // Last backup
        data.lastBackup = "Never";

        return data;
    }

    // Status data class
    public static class StatusData {
        public int totalMembers;
        public String dbSize;
        public String lastBackup;
    }

    // Your existing methods:

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

        // Get current time in proper format
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(calendar.getTime());

        Log.d("DB_DEBUG", "Java current time: " + currentTime);

        values.put("unique_id", userUniqueId);
        values.put("firstname", userName);
        values.put("month", monthYear);
        values.put("price", amount);
        values.put("handovered_to", handoveredBy);
        values.put("created_at", currentTime); // Override the default

        long result = db.insert("payment", null, values);
        Log.d("DB_DEBUG", "savePaymentRecord - After insert, result: " + result);

        return result != -1;
    }

    public boolean saveOtherPaymentRecord(String userUniqueId, String userName,
                                          String description, String amount, String handoveredBy) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Get current time in proper format
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(calendar.getTime());

        Log.d("DB_DEBUG", "Java current time: " + currentTime);

        values.put("unique_id", userUniqueId);
        values.put("firstname", userName);
        values.put("other_description", description);
        values.put("other_amount", amount);
        values.put("handovered_to", handoveredBy);
        values.put("created_at", currentTime); // Override the default

        long result = db.insert("payment", null, values);
        Log.d("DB_DEBUG", "saveOtherPaymentRecord - After insert, result: " + result);

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


    // Add this method to your DatabaseHelperClass
    public List<OtherAmount> getAllOtherAmounts() {
        List<OtherAmount> otherAmounts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT p.payment_id, p.unique_id, p.firstname, p.lastname, p.month, " +
                    "p.price, p.handovered_to, p.sessioned_email, p.created_at, " +
                    "p.other_description, p.other_amount, nu.profileImage " +
                    "FROM payment p " +
                    "LEFT JOIN new_users nu ON p.unique_id = nu.unique_id " +
                    "WHERE p.other_amount IS NOT NULL AND p.other_amount > 0 " +
                    "ORDER BY p.created_at DESC";

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int paymentId = cursor.getInt(cursor.getColumnIndexOrThrow("payment_id"));
                    String uniqueId = cursor.getString(cursor.getColumnIndexOrThrow("unique_id"));
                    String firstName = cursor.getString(cursor.getColumnIndexOrThrow("firstname"));
                    String lastName = cursor.getString(cursor.getColumnIndexOrThrow("lastname"));
                    String month = cursor.getString(cursor.getColumnIndexOrThrow("month"));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                    String handoveredTo = cursor.getString(cursor.getColumnIndexOrThrow("handovered_to"));
                    String sessionedEmail = cursor.getString(cursor.getColumnIndexOrThrow("sessioned_email"));
                    String createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("other_description"));
                    double otherAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("other_amount"));

                    byte[] profileImage = null;
                    int profileImageIndex = cursor.getColumnIndex("profileImage");
                    if (!cursor.isNull(profileImageIndex)) {
                        profileImage = cursor.getBlob(profileImageIndex);
                    }

                    OtherAmount otherAmountObj = new OtherAmount(
                            paymentId, uniqueId, firstName, month, price,
                            handoveredTo, sessionedEmail, createdAt, description,
                            otherAmount, profileImage
                    );

                    otherAmounts.add(otherAmountObj);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting other amounts: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        return otherAmounts;
    }

    public boolean addExpense(String description, double price, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("description", description);
        values.put("price", price);
        if (image != null) {
            values.put("image", image);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Colombo")); // Sri Lanka timezone
        String currentTime = sdf.format(new Date());

        values.put("created_at", currentTime);

        long result = db.insert("expenses", null, values);
        return result != -1;
    }

    // Get today's expenses
    public double getTodayExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        double todayExpenses = 0.0;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Cursor cursor = db.rawQuery(
                "SELECT SUM(price) FROM expenses WHERE DATE(created_at) = ?",
                new String[]{today}
        );

        if (cursor != null && cursor.moveToFirst()) {
            todayExpenses = cursor.getDouble(0);
            cursor.close();
        }

        return todayExpenses;
    }

    // Get all expenses
    public Cursor getAllExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query("expenses",
                null,
                null,
                null,
                null, null, "created_at DESC");
    }

    // Get expenses for a specific date
    public Cursor getExpensesByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query("expenses",
                null,
                "DATE(created_at) = ?",
                new String[]{date},
                null, null, "created_at DESC");
    }
}

