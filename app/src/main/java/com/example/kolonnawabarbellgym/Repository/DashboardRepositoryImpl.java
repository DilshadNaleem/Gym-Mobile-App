package com.example.kolonnawabarbellgym.Repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.example.kolonnawabarbellgym.Model.SalesData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardRepositoryImpl implements DashboardRepo {
    private DatabaseHelperClass databaseHelper;

    public DashboardRepositoryImpl(DatabaseHelperClass databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    @Override
    public SalesData getSalesData() {
        SalesData salesData = new SalesData();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            // Today's sales - only from payment table (actual payments made today)
            double todaySales = getTodaySales(db, today);
            salesData.setTodaySales(todaySales);

            // Total sales - sum of all payments in payment table
            double totalSales = getTotalSales(db);
            salesData.setTotalSales(totalSales);

            // Pending admission fees - sum of monthlyFee where status = 0
            double pendingFees = getPendingAdmissionFees(db);
            salesData.setPendingAdmissionFees(pendingFees);

            // Existing members fees - sum of monthlyFee where status = 2
            double existingMembersFees = getExistingMembersFees(db);
            salesData.setExistingMembersFees(existingMembersFees);

            // Get member counts
            int[] memberCounts = getMemberCounts(db);
            salesData.setTotalMembers(memberCounts[0]);
            salesData.setPaidMembers(memberCounts[1]);
            salesData.setPendingMembers(memberCounts[2]);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return salesData;
    }

    @Override
    public void refreshData() {
        // Implementation for refreshing data if needed
    }

    private double getTodaySales(SQLiteDatabase db, String today) {
        double todaySales = 0;
        Cursor cursor = null;

        try {
            // Get payments made today from payment table
            String query = "SELECT SUM(price) FROM payment WHERE date(created_at) = date(?)";
            cursor = db.rawQuery(query, new String[]{today});

            if (cursor != null && cursor.moveToFirst()) {
                todaySales = cursor.getDouble(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        return todaySales;
    }

    private double getTotalSales(SQLiteDatabase db) {
        double totalSales = 0;
        Cursor cursor = null;

        try {
            // Get all payments from payment table (actual money received)
            String query = "SELECT SUM(CAST(monthlyFee AS REAL)) FROM new_users WHERE status IN (1)";
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                totalSales = cursor.getDouble(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        return totalSales;
    }

    private double getPendingAdmissionFees(SQLiteDatabase db) {
        double pendingFees = 0;
        Cursor cursor = null;

        try {
            // Assuming status 0 means pending admission
            String query = "SELECT SUM(CAST(monthlyFee AS REAL)) FROM new_users WHERE status = 0";
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                pendingFees = cursor.getDouble(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        return pendingFees;
    }

    private double getExistingMembersFees(SQLiteDatabase db) {
        double existingFees = 0;
        Cursor cursor = null;

        try {
            // Assuming status 2 means existing members
            String query = "SELECT SUM(CAST(monthlyFee AS REAL)) FROM new_users WHERE status = 2";
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                existingFees = cursor.getDouble(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        return existingFees;
    }

    private int[] getMemberCounts(SQLiteDatabase db) {
        int totalMembers = 0;
        int paidMembers = 0;
        int pendingMembers = 0;
        Cursor cursor = null;

        try {
            // Total members
            cursor = db.rawQuery("SELECT COUNT(*) FROM new_users", null);
            if (cursor != null && cursor.moveToFirst()) {
                totalMembers = cursor.getInt(0);
                cursor.close();
            }

            // Paid members (status = 1)
            cursor = db.rawQuery("SELECT COUNT(*) FROM new_users WHERE status = 1", null);
            if (cursor != null && cursor.moveToFirst()) {
                paidMembers = cursor.getInt(0);
                cursor.close();
            }

            // Pending members (status = 0)
            cursor = db.rawQuery("SELECT COUNT(*) FROM new_users WHERE status = 0", null);
            if (cursor != null && cursor.moveToFirst()) {
                pendingMembers = cursor.getInt(0);
                cursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        return new int[]{totalMembers, paidMembers, pendingMembers};
    }
}