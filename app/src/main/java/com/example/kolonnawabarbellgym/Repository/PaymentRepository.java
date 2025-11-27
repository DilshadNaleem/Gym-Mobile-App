package com.example.kolonnawabarbellgym.Repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.example.kolonnawabarbellgym.Model.Payment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentRepository
{
    private DatabaseHelperClass databaseHelper;

    public PaymentRepository(DatabaseHelperClass databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public List<Payment> getTodayPayments() {
        List<Payment> payments = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            String query = "SELECT p.*, nu.firstName as member_name " +
                    "FROM payment p " +
                    "LEFT JOIN new_users nu ON p.unique_id = nu.unique_id " +
                    "WHERE date(p.created_at) = date(?) " +
                    "ORDER BY p.created_at DESC";

            cursor = db.rawQuery(query, new String[]{today});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Payment payment = new Payment();

                    payment.setUniqueId(cursor.getString(cursor.getColumnIndexOrThrow("unique_id")));
                    payment.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
                    payment.setMonth(cursor.getString(cursor.getColumnIndexOrThrow("month")));

                    // Parse created_at date
                    String createdAtStr = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    payment.setCreatedAt(dateFormat.parse(createdAtStr));

                    // Set member name
                    payment.setMemberName(cursor.getString(cursor.getColumnIndexOrThrow("member_name")));

                    payments.add(payment);

                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        return payments;
    }
}
