package com.example.kolonnawabarbellgym.Repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.example.kolonnawabarbellgym.Model.Member;
import com.example.kolonnawabarbellgym.Model.MemberSales;
import com.example.kolonnawabarbellgym.Model.SalesData;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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

            // Total Admission Price - sum of all monthlyFee from new_users where status = 1 (paid members)
            double totalAdmissionPrice = getTotalAdmissionPrice(db);
            salesData.setTotalSales(totalAdmissionPrice); // This will now show total admission price

            // Total Amount of Prices - sum of all price from payment table (actual money received)
            double totalAmountOfPrices = getTotalAmountOfPrices(db);
            salesData.setTotalAmountOfPrices(totalAmountOfPrices);

            // Pending admission fees - sum of monthlyFee where status = 0
            double pendingFees = getPendingAdmissionFees(db);
            salesData.setPendingAdmissionFees(pendingFees);

            // Existing members fees - sum of monthlyFee where status = 2
            double existingMembersFees = getExistingMembersFees(db);
            salesData.setExistingMembersFees(existingMembersFees);

            double otherAmount = getOtherAmountTotal(db);
            salesData.setOtherAmount(otherAmount);

            // Get member counts
            int[] memberCounts = getMemberCounts(db);
            salesData.setTotalMembers(memberCounts[0]);
            salesData.setPaidMembers(memberCounts[1]);
            salesData.setPendingMembers(memberCounts[2]);


            // Add expense and profit calculations
            salesData.setTodayExpenses(getTodayExpenses());
            salesData.setTotalExpenses(getTotalExpenses());
            salesData.setTodayProfit(getTodayProfit());
            salesData.setTotalProfit(getTotalProfit());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return salesData;
    }

    @Override
    public void refreshData() {
        // Implementation for refreshing data if needed
    }

    // Add these methods at the end of your class:

    @Override
    public double getTodayExpenses() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        double todayExpenses = 0.0;

        // Get today's date in format yyyy-MM-dd
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

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

    @Override
    public double getTotalExpenses() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        double totalExpenses = 0.0;

        Cursor cursor = db.rawQuery("SELECT SUM(price) FROM expenses", null);

        if (cursor != null && cursor.moveToFirst()) {
            totalExpenses = cursor.getDouble(0);
            cursor.close();
        }

        return totalExpenses;
    }

    @Override
    public double getTodayProfit() {
        double todaySales = getTodaySales();
        double todayExpenses = getTodayExpenses();
        return todaySales - todayExpenses;
    }

    @Override
    public double getTotalProfit() {
        double totalSales = getTotalSales();
        double totalExpenses = getTotalExpenses();
        return totalSales - totalExpenses;
    }


    public double getTodaySales() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        double todaySales = 0.0;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Cursor cursor = db.rawQuery(
                "SELECT SUM(price) FROM payment WHERE DATE(created_at) = ?",
                new String[]{today}
        );

        if (cursor != null && cursor.moveToFirst()) {
            todaySales = cursor.getDouble(0);
            cursor.close();
        }

        return todaySales;
    }


    public double getTotalSales() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        double totalSales = 0.0;

        Cursor cursor = db.rawQuery("SELECT SUM(price) FROM payment", null);

        if (cursor != null && cursor.moveToFirst()) {
            totalSales = cursor.getDouble(0);
            cursor.close();
        }

        return totalSales;
    }

    // ... REST OF YOUR EXISTING METHODS (getTodaySales with db parameter, getTotalAdmissionPrice, etc.)
    // Keep all your existing methods below this point

    private double getTodaySales(SQLiteDatabase db, String today) {
        double todaySales = 0;
        Cursor cursor = null;

        try {
            Log.d("TODAY_SALES", "=== Starting Today Sales Calculation ===");
            Log.d("TODAY_SALES", "Today's date: " + today);

            // Get payments made today from payment table
            String query = "SELECT SUM(price) FROM payment WHERE date(created_at) = date(?)";
            cursor = db.rawQuery(query, new String[]{today});

            if (cursor != null && cursor.moveToFirst()) {
                todaySales = cursor.getDouble(0);
                Log.d("TODAY_SALES", "Raw sum result: " + todaySales);
            } else {
                Log.d("TODAY_SALES", "No results found for today's sales");
            }

            // Let's also check individual payments for today using correct column names
            String detailedQuery = "SELECT payment_id, unique_id, price, month, firstname, lastname, handovered_to, created_at, other_description, other_amount " +
                    "FROM payment WHERE date(created_at) = date(?)";
            Cursor detailCursor = db.rawQuery(detailedQuery, new String[]{today});

            if (detailCursor != null) {
                Log.d("TODAY_SALES", "Found " + detailCursor.getCount() + " payments for today");
                if (detailCursor.moveToFirst()) {
                    do {
                        int paymentId = detailCursor.getInt(detailCursor.getColumnIndexOrThrow("payment_id"));
                        String uniqueId = detailCursor.getString(detailCursor.getColumnIndexOrThrow("unique_id"));
                        double price = detailCursor.getDouble(detailCursor.getColumnIndexOrThrow("price"));
                        String month = detailCursor.getString(detailCursor.getColumnIndexOrThrow("month"));
                        String firstName = detailCursor.getString(detailCursor.getColumnIndexOrThrow("firstname"));
                        String lastName = detailCursor.getString(detailCursor.getColumnIndexOrThrow("lastname"));
                        String handoveredTo = detailCursor.getString(detailCursor.getColumnIndexOrThrow("handovered_to"));
                        String createdAt = detailCursor.getString(detailCursor.getColumnIndexOrThrow("created_at"));
                        String otherDescription = detailCursor.getString(detailCursor.getColumnIndexOrThrow("other_description"));
                        double otherAmount = detailCursor.getDouble(detailCursor.getColumnIndexOrThrow("other_amount"));

                        Log.d("TODAY_SALES", "Payment - ID: " + paymentId +
                                ", UniqueID: " + uniqueId +
                                ", Price: " + price +
                                ", Month: " + month +
                                ", Name: " + firstName + " " + lastName +
                                ", Handovered to: " + handoveredTo +
                                ", Other Desc: " + otherDescription +
                                ", Other Amount: " + otherAmount +
                                ", Created: " + createdAt);
                    } while (detailCursor.moveToNext());
                } else {
                    Log.d("TODAY_SALES", "No individual payments found for today");
                }
                detailCursor.close();
            }

            // If still 0, let's check all payments to see what dates we have
            if (todaySales == 0) {
                Log.d("TODAY_SALES", "No sales found for today, checking all payment dates...");
                Cursor allPaymentsCursor = db.rawQuery(
                        "SELECT payment_id, created_at, price, month, firstname, lastname FROM payment ORDER BY created_at DESC LIMIT 10", null);

                if (allPaymentsCursor != null) {
                    Log.d("TODAY_SALES", "Recent payments (last 10):");
                    if (allPaymentsCursor.moveToFirst()) {
                        do {
                            int paymentId = allPaymentsCursor.getInt(allPaymentsCursor.getColumnIndexOrThrow("payment_id"));
                            String createdAt = allPaymentsCursor.getString(allPaymentsCursor.getColumnIndexOrThrow("created_at"));
                            double price = allPaymentsCursor.getDouble(allPaymentsCursor.getColumnIndexOrThrow("price"));
                            String month = allPaymentsCursor.getString(allPaymentsCursor.getColumnIndexOrThrow("month"));
                            String firstName = allPaymentsCursor.getString(allPaymentsCursor.getColumnIndexOrThrow("firstname"));
                            String lastName = allPaymentsCursor.getString(allPaymentsCursor.getColumnIndexOrThrow("lastname"));

                            Log.d("TODAY_SALES", "Payment - ID: " + paymentId +
                                    ", Date: " + createdAt +
                                    ", Price: " + price +
                                    ", Month: " + month +
                                    ", Name: " + firstName + " " + lastName);
                        } while (allPaymentsCursor.moveToNext());
                    } else {
                        Log.d("TODAY_SALES", "No payments found in the entire payment table");
                    }
                    allPaymentsCursor.close();
                }
            }

            Log.d("TODAY_SALES", "Final today sales: " + todaySales);
            Log.d("TODAY_SALES", "=== Finished Today Sales Calculation ===");

        } catch (Exception e) {
            Log.e("TODAY_SALES", "Error in getTodaySales: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        return todaySales;
    }

    private double getTotalAdmissionPrice(SQLiteDatabase db) {
        double totalAdmissionPrice = 0;
        Cursor cursor = null;

        try {
            // Get all monthly fees from new_users table for paid members (status = 1)
            String query = "SELECT SUM(CAST(monthlyFee AS REAL)) FROM new_users WHERE status = 1";
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                totalAdmissionPrice = cursor.getDouble(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        return totalAdmissionPrice;
    }

    private double getTotalAmountOfPrices(SQLiteDatabase db) {
        double totalAmountOfPrices = 0;
        Cursor cursor = null;

        try {
            // Get all payments from payment table (actual money received)
            String query = "SELECT SUM(price) FROM payment";
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                totalAmountOfPrices = cursor.getDouble(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        return totalAmountOfPrices;
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

    // ... CONTINUE WITH ALL YOUR OTHER EXISTING METHODS
    // (getExistingMembersWithUnpaidFees, getExistingMembersFees, calculateMonthsFromJoinDate,
    // calculateMonthsFromLastPayment, getMemberCounts, getTotalAdmissionSales, getOtherAmountTotal)

    public List<Member> getExistingMembersWithUnpaidFees() {
        List<Member> members = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String currentMonthYear = new SimpleDateFormat("MMMM_yyyy", Locale.getDefault()).format(new Date());

            String query = "SELECT nu.unique_id, nu.firstName, nu.profileImage, nu.monthlyFee, nu.created_time, " +
                    "CASE WHEN EXISTS (SELECT 1 FROM payment p WHERE p.unique_id = nu.unique_id AND p.month = ?) " +
                    "THEN NULL " +
                    "ELSE (SELECT month FROM payment p WHERE p.unique_id = nu.unique_id ORDER BY p.created_at DESC LIMIT 1) " +
                    "END as last_payment_month " +
                    "FROM new_users nu " +
                    "WHERE nu.status IN (1, 2)";

            cursor = db.rawQuery(query, new String[]{currentMonthYear});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String uniqueId = cursor.getString(cursor.getColumnIndexOrThrow("unique_id"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("firstName"));

                    byte[] profileImageBytes = null;
                    int profileImageIndex = cursor.getColumnIndex("profileImage");
                    if (!cursor.isNull(profileImageIndex)) {
                        profileImageBytes = cursor.getBlob(profileImageIndex);
                    }


                    double monthlyFee = cursor.getDouble(cursor.getColumnIndexOrThrow("monthlyFee"));
                    String createdTime = cursor.getString(cursor.getColumnIndexOrThrow("created_time"));
                    String lastPaymentMonth = cursor.getString(cursor.getColumnIndexOrThrow("last_payment_month"));

                    int unpaidMonths;
                    if (lastPaymentMonth == null) {
                        unpaidMonths = 0;
                    } else {
                        Calendar calendar = Calendar.getInstance();
                        int currentYear = calendar.get(Calendar.YEAR);
                        int currentMonth = calendar.get(Calendar.MONTH);
                        unpaidMonths = calculateMonthsFromLastPayment(lastPaymentMonth, currentYear, currentMonth);
                    }

                    double totalDue = monthlyFee * unpaidMonths;

                    // Only add members who have unpaid fees
                    if (unpaidMonths > 0) {
                        Member member = new Member(uniqueId, name, profileImageBytes, monthlyFee,
                                lastPaymentMonth, unpaidMonths, totalDue);
                        members.add(member);
                    }

                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e("EXISTING_MEMBERS", "Error getting existing members: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        return members;
    }

    private double getExistingMembersFees(SQLiteDatabase db) {
        double existingFees = 0;
        Cursor cursor = null;

        try {
            // Get current month in proper format (November_2025)
            String currentMonthYear = new SimpleDateFormat("MMMM_yyyy", Locale.getDefault()).format(new Date());

            Log.d("EXISTING_FEES", "=== Starting Existing Members Fees Calculation ===");
            Log.d("EXISTING_FEES", "Current month: " + currentMonthYear);

            // Get all members with status 1 OR 2
            // Check if they've paid for the CURRENT month
            String membersQuery = "SELECT nu.unique_id, nu.monthlyFee, nu.created_time, " +
                    "CASE WHEN EXISTS (SELECT 1 FROM payment p WHERE p.unique_id = nu.unique_id AND p.month = ?) " +
                    "THEN NULL " + // If paid current month, no unpaid months
                    "ELSE (SELECT month FROM payment p WHERE p.unique_id = nu.unique_id ORDER BY p.created_at DESC LIMIT 1) " +
                    "END as last_payment_month " +
                    "FROM new_users nu " +
                    "WHERE nu.status IN (1, 2)";

            cursor = db.rawQuery(membersQuery, new String[]{currentMonthYear});

            if (cursor != null && cursor.moveToFirst()) {
                Log.d("EXISTING_FEES", "Found " + cursor.getCount() + " members (status=1 or 2)");

                do {
                    String uniqueId = cursor.getString(cursor.getColumnIndexOrThrow("unique_id"));
                    double monthlyFee = cursor.getDouble(cursor.getColumnIndexOrThrow("monthlyFee"));
                    String createdTime = cursor.getString(cursor.getColumnIndexOrThrow("created_time"));
                    String lastPaymentMonth = cursor.getString(cursor.getColumnIndexOrThrow("last_payment_month"));

                    Log.d("EXISTING_FEES", "Processing member: " + uniqueId +
                            ", Monthly Fee: " + monthlyFee +
                            ", Last Payment: " + lastPaymentMonth);

                    int unpaidMonths;
                    if (lastPaymentMonth == null) {
                        // Member has paid for current month - 0 unpaid months
                        unpaidMonths = 0;
                        Log.d("EXISTING_FEES", "Member " + uniqueId + " has paid for current month - 0 unpaid months");
                    } else {
                        // Member hasn't paid for current month - calculate from last payment
                        Calendar calendar = Calendar.getInstance();
                        int currentYear = calendar.get(Calendar.YEAR);
                        int currentMonth = calendar.get(Calendar.MONTH);
                        unpaidMonths = calculateMonthsFromLastPayment(lastPaymentMonth, currentYear, currentMonth);
                    }

                    double memberFees = monthlyFee * unpaidMonths;
                    existingFees += memberFees;

                    Log.d("EXISTING_FEES", "Member " + uniqueId + ": " + unpaidMonths + " unpaid months = " + memberFees);

                } while (cursor.moveToNext());
            } else {
                Log.d("EXISTING_FEES", "No members found with status=1 or 2");
            }

            Log.d("EXISTING_FEES", "Total Existing Members Fees: " + existingFees);
            Log.d("EXISTING_FEES", "=== Finished Existing Members Fees Calculation ===");

        } catch (Exception e) {
            Log.e("EXISTING_FEES", "Error in getExistingMembersFees: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        return existingFees;
    }

    private int calculateMonthsFromJoinDate(String createdTime, int currentYear, int currentMonth) {
        try {
            Log.d("JOIN_DATE_CALC", "Calculating unpaid months from join date: " + createdTime);

            // Parse the join date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date joinDate = dateFormat.parse(createdTime);

            Calendar joinCalendar = Calendar.getInstance();
            joinCalendar.setTime(joinDate);

            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.set(currentYear, currentMonth, 1); // Set to first day of current month

            // If member joined in current month or future, no unpaid months
            if (!joinCalendar.before(currentCalendar)) {
                Log.d("JOIN_DATE_CALC", "Member joined this month or in future - 0 unpaid months");
                return 0;
            }

            // Count months from the month AFTER joining to current month
            Calendar tempCalendar = Calendar.getInstance();
            tempCalendar.setTime(joinDate);
            tempCalendar.add(Calendar.MONTH, 1); // Start from the month after joining
            tempCalendar.set(Calendar.DAY_OF_MONTH, 1); // Set to first day of month

            int unpaidMonths = 0;

            // Count each month until current month
            while (tempCalendar.before(currentCalendar) ||
                    (tempCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                            tempCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH))) {

                unpaidMonths++;
                tempCalendar.add(Calendar.MONTH, 1);
            }

            Log.d("JOIN_DATE_CALC", "Accurate unpaid months: " + unpaidMonths);
            return unpaidMonths;

        } catch (Exception e) {
            Log.e("JOIN_DATE_CALC", "Error in calculateMonthsFromJoinDate: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private int calculateMonthsFromLastPayment(String lastPaymentMonth, int currentYear, int currentMonth) {
        try {
            Log.d("MONTH_CALC", "Calculating months from: " + lastPaymentMonth + " to current (" + currentYear + "-" + currentMonth + ")");

            // Parse the last payment month - handle both "January 2025" and "January_2025" formats
            SimpleDateFormat monthFormat;
            if (lastPaymentMonth.contains("_")) {
                monthFormat = new SimpleDateFormat("MMMM_yyyy", Locale.getDefault());
            } else {
                monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            }

            Date lastPaymentDate = monthFormat.parse(lastPaymentMonth);

            Calendar lastPaymentCalendar = Calendar.getInstance();
            lastPaymentCalendar.setTime(lastPaymentDate);

            int lastPaymentYear = lastPaymentCalendar.get(Calendar.YEAR);
            int lastPaymentMonthValue = lastPaymentCalendar.get(Calendar.MONTH);

            Log.d("MONTH_CALC", "Parsed last payment: Year=" + lastPaymentYear + ", Month=" + lastPaymentMonthValue);
            Log.d("MONTH_CALC", "Current: Year=" + currentYear + ", Month=" + currentMonth);

            // Calculate months difference
            int yearDifference = currentYear - lastPaymentYear;
            int monthDifference = currentMonth - lastPaymentMonthValue;

            int totalMonthsDifference = (yearDifference * 12) + monthDifference;

            Log.d("MONTH_CALC", "Year difference: " + yearDifference);
            Log.d("MONTH_CALC", "Month difference: " + monthDifference);
            Log.d("MONTH_CALC", "Total months difference: " + totalMonthsDifference);

            // If the difference is 0, they paid for current month
            // If difference is 1, they missed 1 month, etc.
            int result = Math.max(0, totalMonthsDifference);
            Log.d("MONTH_CALC", "Final unpaid months: " + result);

            return result;

        } catch (Exception e) {
            Log.e("MONTH_CALC", "Error in calculateMonthsFromLastPayment: " + e.getMessage());
            e.printStackTrace();
            return 0; // Default to 0 months if there's an error
        }
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

    public List<MemberSales> getTotalAdmissionSales() {
        List<MemberSales> memberSalesList = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            Log.d("TOTAL_ADMISSION", "=== Fetching Total Admission Sales ===");

            // Query to get all paid members (status = 1) with their admission fees
            String query = "SELECT unique_id, firstName, profileImage, monthlyFee, created_time, status " +
                    "FROM new_users WHERE status = 1 ORDER BY created_time DESC";

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                Log.d("TOTAL_ADMISSION", "Found " + cursor.getCount() + " paid members");

                do {
                    String uniqueId = cursor.getString(cursor.getColumnIndexOrThrow("unique_id"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("firstName"));

                    byte[] profileImageBytes = null;
                    int profileImageIndex = cursor.getColumnIndex("profileImage");
                    if (!cursor.isNull(profileImageIndex)) {
                        profileImageBytes = cursor.getBlob(profileImageIndex);
                    }

                    double monthlyFee = cursor.getDouble(cursor.getColumnIndexOrThrow("monthlyFee"));
                    String createdTime = cursor.getString(cursor.getColumnIndexOrThrow("created_time"));
                    int status = cursor.getInt(cursor.getColumnIndexOrThrow("status"));

                    String statusText = "Paid";
                    if (status == 0) {
                        statusText = "Pending";
                    } else if (status == 2) {
                        statusText = "Existing";
                    }

                    Log.d("TOTAL_ADMISSION", "Member: " + name +
                            ", ID: " + uniqueId +
                            ", Fee: " + monthlyFee +
                            ", Joined: " + createdTime);

                    MemberSales memberSales = new MemberSales(
                            uniqueId,
                            name,
                            profileImageBytes,
                            monthlyFee,
                            createdTime,
                            statusText
                    );

                    memberSalesList.add(memberSales);

                } while (cursor.moveToNext());
            } else {
                Log.d("TOTAL_ADMISSION", "No paid members found");
            }

            Log.d("TOTAL_ADMISSION", "Total members in list: " + memberSalesList.size());

        } catch (Exception e) {
            Log.e("TOTAL_ADMISSION", "Error fetching total admission sales: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        return memberSalesList;
    }


    private double getOtherAmountTotal(SQLiteDatabase db)
    {
        double otherAmountTotal = 0;
        Cursor cursor = null;

        try {

            Log.d("OTHER_AMOUNT", "=== Calculating Other Amount Total ===");

            // Query to sum all other_amount from payment table
            String query = "SELECT SUM(other_amount) FROM payment WHERE other_amount IS NOT NULL AND other_amount > 0";
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                otherAmountTotal = cursor.getDouble(0);
                Log.d("OTHER_AMOUNT", "Raw sum result: " + otherAmountTotal);
            } else {
                Log.d("OTHER_AMOUNT", "No other_amount records found");
            }

            // Let's also check individual other_amount entries for debugging
            String detailedQuery = "SELECT payment_id, other_description, other_amount, created_at " +
                    "FROM payment WHERE other_amount IS NOT NULL AND other_amount > 0";
            Cursor detailCursor = db.rawQuery(detailedQuery, null);

            if (detailCursor != null) {
                Log.d("OTHER_AMOUNT", "Found " + detailCursor.getCount() + " records with other_amount");
                if (detailCursor.moveToFirst()) {
                    do {
                        int paymentId = detailCursor.getInt(detailCursor.getColumnIndexOrThrow("payment_id"));
                        String otherDescription = detailCursor.getString(detailCursor.getColumnIndexOrThrow("other_description"));
                        double otherAmount = detailCursor.getDouble(detailCursor.getColumnIndexOrThrow("other_amount"));
                        String createdAt = detailCursor.getString(detailCursor.getColumnIndexOrThrow("created_at"));

                        Log.d("OTHER_AMOUNT", "Record - ID: " + paymentId +
                                ", Description: " + otherDescription +
                                ", Amount: " + otherAmount +
                                ", Created: " + createdAt);
                    } while (detailCursor.moveToNext());
                } else {
                    Log.d("OTHER_AMOUNT", "No individual other_amount records found");
                }
                detailCursor.close();
            }

            Log.d("OTHER_AMOUNT", "Final other amount total: " + otherAmountTotal);
            Log.d("OTHER_AMOUNT", "=== Finished Other Amount Calculation ===");

        } catch (Exception e) {
            Log.e("OTHER_AMOUNT", "Error in getOtherAmountTotal: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        return otherAmountTotal;
    }
}