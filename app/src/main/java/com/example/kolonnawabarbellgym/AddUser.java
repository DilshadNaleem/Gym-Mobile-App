package com.example.kolonnawabarbellgym;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.kolonnawabarbellgym.Database.DatabaseHelperClass;
import com.google.android.material.textfield.TextInputEditText;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddUser extends BaseActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    private TextInputEditText etFirstName, etLastName, etEmail, etPhoneNumber, etNIC, etMonthlyFee;
    private RadioGroup statusRadioGroup;
    private ImageView profileImageView, imagePreview;
    private Button btnGallery, btnCamera, btnRetake, btnDelete, btnSubmit;
    private LinearLayout imagePreviewLayout;

    private Bitmap selectedImageBitmap;
    private byte[] profileImageBytes;
    private boolean isImageSelected = false;

    private DatabaseHelperClass databaseHelper;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        showImagePreview(selectedImageBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    selectedImageBitmap = (Bitmap) extras.get("data");
                    showImagePreview(selectedImageBitmap);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        currentNavItemId = R.id.navigation_newuser;
        setupBottomNavigation(R.id.navigation_newuser);

        // Initialize database helper
        databaseHelper = new DatabaseHelperClass(this);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        // Text inputs
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etNIC = findViewById(R.id.etNIC);
        etMonthlyFee = findViewById(R.id.etMonthlyFee);

        // Radio group
        statusRadioGroup = findViewById(R.id.statusRadioGroup);

        // Image views and buttons
        profileImageView = findViewById(R.id.profileImageView);
        imagePreview = findViewById(R.id.imagePreview);
        btnGallery = findViewById(R.id.btnGallery);
        btnCamera = findViewById(R.id.btnCamera);
        btnRetake = findViewById(R.id.btnRetake);
        btnDelete = findViewById(R.id.btnDelete);
        btnSubmit = findViewById(R.id.btnSubmit);
        imagePreviewLayout = findViewById(R.id.imagePreviewLayout);
    }

    private void setupClickListeners() {
        btnGallery.setOnClickListener(v -> openGallery());
        btnCamera.setOnClickListener(v -> openCamera());
        btnRetake.setOnClickListener(v -> retakeImage());
        btnDelete.setOnClickListener(v -> deleteImage());
        btnSubmit.setOnClickListener(v -> addUserToDatabase());
    }

    private void openGallery() {
        if (checkStoragePermission()) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        } else {
            requestStoragePermission();
        }
    }

    private void openCamera() {
        if (checkCameraPermission()) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        } else {
            requestCameraPermission();
        }
    }

    private void showImagePreview(Bitmap bitmap) {
        imagePreview.setImageBitmap(bitmap);
        imagePreviewLayout.setVisibility(View.VISIBLE);
        isImageSelected = true;

        // Convert bitmap to byte array for database storage
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        profileImageBytes = stream.toByteArray();
    }

    private void retakeImage() {
        imagePreviewLayout.setVisibility(View.GONE);
        isImageSelected = false;
        profileImageBytes = null;
        openCamera();
    }

    private void deleteImage() {
        imagePreviewLayout.setVisibility(View.GONE);
        isImageSelected = false;
        profileImageBytes = null;
        selectedImageBitmap = null;
        profileImageView.setImageResource(R.drawable.ic_person);
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission denied for gallery access", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addUserToDatabase() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String nic = etNIC.getText().toString().trim();
        String monthlyFee = etMonthlyFee.getText().toString().trim();

        // Validate required fields
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get status from radio buttons
        int status = 0; // Default to 0 (No)
        int selectedId = statusRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radioYes) {
            status = 1;
        }

        // Insert into database and get the actual member ID that was inserted
        String insertedMemberId = insertUserIntoDatabase(firstName, lastName, email, phoneNumber, nic, profileImageBytes, monthlyFee, status);
        try {
            if (insertedMemberId != null) {
                // Generate PDF with the actual inserted member ID
                generateUserPDF(insertedMemberId, firstName, lastName, email, phoneNumber, nic, monthlyFee, status);
                clearForm();
            } else {
                Toast.makeText(this, "Failed to add user to database", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to add user", Toast.LENGTH_SHORT).show();
        }
    }

    private String insertUserIntoDatabase(String firstName, String lastName,
                                          String email, String phoneNumber, String nic,
                                          byte[] profileImage, String monthlyFee, int status) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        // Get the next member ID
        String nextMemberId = getNextMemberId(db);
        if (nextMemberId == null) {
            db.close();
            return null;
        }

        ContentValues values = new ContentValues();
        values.put("unique_id", nextMemberId);
        values.put("firstName", firstName);
        values.put("lastName", lastName);
        values.put("email", email);
        values.put("phoneNumber", phoneNumber);
        values.put("nic", nic);
        values.put("profileImage", profileImage);
        values.put("monthlyFee", monthlyFee);
        values.put("status", status);

        long result = db.insert("new_users", null, values);
        db.close();

        if (result != -1) {
            return nextMemberId; // Return the actual member ID that was inserted
        } else {
            return null;
        }
    }

    private String getNextMemberId(SQLiteDatabase db) {
        String nextMemberId = null;
        Cursor cursor = null;

        try {
            // Query to get the highest existing member ID from new_users table
            cursor = db.rawQuery("SELECT unique_id FROM new_users ORDER BY userid DESC LIMIT 1", null);

            if (cursor != null && cursor.moveToFirst()) {
                String lastUniqueId = cursor.getString(cursor.getColumnIndexOrThrow("unique_id"));

                if (lastUniqueId != null && lastUniqueId.startsWith("mem_")) {
                    try {
                        // Extract the number part and increment it
                        String numberPart = lastUniqueId.substring(4); // Remove "mem_" prefix
                        int nextNumber = Integer.parseInt(numberPart) + 1;
                        nextMemberId = "mem_" + String.format("%02d", nextNumber);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        // If parsing fails, start from mem_01
                        nextMemberId = "mem_01";
                    }
                } else {
                    // If no proper format found, start from mem_01
                    nextMemberId = "mem_01";
                }
            } else {
                // If no records exist, start from mem_01
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

    private void generateUserPDF(String memberId, String firstName, String lastName, String email,
                                 String phoneNumber, String nic, String monthlyFee, int status) {
        try {
            // Create PDF file in app-specific directory
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "Member_" + firstName + "_" + lastName + "_" + timeStamp + ".pdf";

            // Use app-specific directory (no permissions needed)
            File documentsDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (documentsDir == null) {
                documentsDir = getFilesDir(); // Fallback to internal storage
            }

            // Create documents directory if it doesn't exist
            if (!documentsDir.exists()) {
                documentsDir.mkdirs();
            }

            File file = new File(documentsDir, fileName);

            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.A4);

            // Add header with logo and title
            addHeader(document);

            // Add member details section with profile image - PASS THE ACTUAL memberId
            addMemberDetailsSection(document, memberId, firstName, lastName, email, phoneNumber, nic, monthlyFee, status);

            // Add footer
            addFooter(document);

            // Add logo watermark
            addLogoWatermark(document);

            document.close();

            // Show success message with option to share
            showPDFSuccessDialog(file, fileName);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void addHeader(Document document) {
        try {
            // Create a table for header with logo and text side by side
            Table headerTable = new Table(2);
            headerTable.setWidth(PageSize.A4.getWidth() - 72); // Account for margins
            headerTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
            headerTable.setMarginTop(20);
            headerTable.setMarginBottom(20);

            // Add logo (bigger size)
            Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mainlogo);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            logoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] logoBytes = stream.toByteArray();

            ImageData imageData = ImageDataFactory.create(logoBytes);
            Image logo = new Image(imageData);
            logo.setWidth(120); // Bigger logo
            logo.setHeight(120);
            logo.setHorizontalAlignment(HorizontalAlignment.LEFT);

            // Add gym name and title
            Paragraph gymName = new Paragraph("KOLONNAWA BARBELL GYM")
                    .setBold()
                    .setFontSize(18)
                    .setFontColor(new DeviceRgb(0, 51, 102)) // Dark blue color
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(5);

            Paragraph title = new Paragraph("Member Registration Certificate")
                    .setBold()
                    .setFontSize(14)
                    .setFontColor(new DeviceRgb(0, 0, 0))
                    .setTextAlignment(TextAlignment.LEFT);

            // Create cell for text content
            Cell textCell = new Cell();
            textCell.setBorder(Border.NO_BORDER);
            textCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            textCell.add(gymName);
            textCell.add(title);

            // Create cell for logo
            Cell logoCell = new Cell();
            logoCell.setBorder(Border.NO_BORDER);
            logoCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            logoCell.add(logo);

            headerTable.addCell(logoCell);
            headerTable.addCell(textCell);

            document.add(headerTable);

            // Add separator line
            Paragraph separator = new Paragraph("")
                    .setHeight(2)
                    .setBackgroundColor(new DeviceRgb(0, 51, 102)) // Dark blue line
                    .setMarginTop(10)
                    .setMarginBottom(20);
            document.add(separator);

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback header without logo
            Paragraph gymName = new Paragraph("KOLONNAWA BARBELL GYM")
                    .setBold()
                    .setFontSize(20)
                    .setFontColor(new DeviceRgb(0, 51, 102))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20)
                    .setMarginBottom(5);

            Paragraph title = new Paragraph("Member Registration Certificate")
                    .setBold()
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);

            document.add(gymName);
            document.add(title);
        }
    }

    private void addMemberDetailsSection(Document document, String memberId, String firstName, String lastName,
                                         String email, String phoneNumber, String nic,
                                         String monthlyFee, int status) {

        // Create main container table with border
        Table containerTable = new Table(1);
        containerTable.setWidth(PageSize.A4.getWidth() - 72);
        containerTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
        containerTable.setMarginBottom(30);
        containerTable.setBorder(new SolidBorder(new DeviceRgb(0, 51, 102), 2)); // Blue border

        // Add section title
        Paragraph sectionTitle = new Paragraph("Member Information")
                .setBold()
                .setFontSize(16)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(new DeviceRgb(0, 51, 102)) // Blue background
                .setPadding(10);

        Cell titleCell = new Cell();
        titleCell.setBorder(Border.NO_BORDER);
        titleCell.add(sectionTitle);
        containerTable.addCell(titleCell);

        // Create a 2-column layout for details and profile image
        Table contentTable = new Table(2);
        contentTable.setWidth(PageSize.A4.getWidth() - 100);
        contentTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
        contentTable.setMarginTop(10);
        contentTable.setMarginBottom(10);

        // Left column - Member details
        Cell detailsCell = new Cell();
        detailsCell.setBorder(Border.NO_BORDER);
        detailsCell.setPadding(10);

        // Create details table
        Table detailsTable = new Table(2);
        detailsTable.setWidth(350);

        // Define colors
        Color labelColor = new DeviceRgb(240, 240, 240); // Light gray for labels
        Color valueColor = ColorConstants.WHITE; // White for values

        // Add member details with better styling - USE THE PASSED memberId
        addStyledTableRow(detailsTable, "Member ID:", memberId, labelColor, valueColor);
        addStyledTableRow(detailsTable, "First Name:", firstName, labelColor, valueColor);
        addStyledTableRow(detailsTable, "Last Name:", lastName, labelColor, valueColor);
        addStyledTableRow(detailsTable, "Email:", email, labelColor, valueColor);
        addStyledTableRow(detailsTable, "Phone Number:", phoneNumber, labelColor, valueColor);
        addStyledTableRow(detailsTable, "NIC:", nic.isEmpty() ? "N/A" : nic, labelColor, valueColor);
        addStyledTableRow(detailsTable, "Monthly Fee:", monthlyFee.isEmpty() ? "N/A" : "Rs. " + monthlyFee, labelColor, valueColor);

        // Use "Admission Paid/Unpaid" instead of "Active Status"
        String admissionStatus = status == 1 ? "Admission Paid" : "Admission Unpaid";
        addStyledTableRow(detailsTable, "Admission Status:", admissionStatus, labelColor, valueColor);

        addStyledTableRow(detailsTable, "Registration Date:",
                new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()), labelColor, valueColor);

        detailsCell.add(detailsTable);

        // Right column - Profile image
        Cell imageCell = new Cell();
        imageCell.setBorder(Border.NO_BORDER);
        imageCell.setPadding(10);
        imageCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        imageCell.setHorizontalAlignment(HorizontalAlignment.CENTER);

        if (selectedImageBitmap != null) {
            try {
                // Add profile image title
                Paragraph imageTitle = new Paragraph("Profile Photo")
                        .setBold()
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(5);
                imageCell.add(imageTitle);

                // Convert bitmap to PDF image
                ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
                selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, imageStream);
                byte[] imageBytes = imageStream.toByteArray();

                ImageData profileImageData = ImageDataFactory.create(imageBytes);
                Image profileImage = new Image(profileImageData);
                profileImage.setWidth(100);
                profileImage.setHeight(120);
                profileImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
                profileImage.setBorder(new SolidBorder(ColorConstants.BLACK, 1));

                imageCell.add(profileImage);
            } catch (Exception e) {
                e.printStackTrace();
                // If image fails to load, show placeholder
                Paragraph noImage = new Paragraph("No Profile\nImage")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(10)
                        .setFontColor(ColorConstants.GRAY);
                imageCell.add(noImage);
            }
        } else {
            // No image selected
            Paragraph noImage = new Paragraph("No Profile\nImage Available")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY);
            imageCell.add(noImage);
        }

        // Add cells to content table
        contentTable.addCell(detailsCell);
        contentTable.addCell(imageCell);

        // Add content table to container
        Cell contentCell = new Cell();
        contentCell.setBorder(Border.NO_BORDER);
        contentCell.setPadding(5);
        contentCell.add(contentTable);
        containerTable.addCell(contentCell);

        document.add(containerTable);
    }

    private void addStyledTableRow(Table table, String label, String value, Color labelColor, Color valueColor) {
        // Label cell with styling
        Cell labelCell = new Cell();
        labelCell.add(new Paragraph(label).setBold());
        labelCell.setPadding(8);
        labelCell.setBackgroundColor(labelColor);
        labelCell.setBorder(new SolidBorder(ColorConstants.GRAY, 0.5f));

        // Value cell with styling
        Cell valueCell = new Cell();
        valueCell.add(new Paragraph(value));
        valueCell.setPadding(8);
        valueCell.setBackgroundColor(valueColor);
        valueCell.setBorder(new SolidBorder(ColorConstants.GRAY, 0.5f));

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addFooter(Document document) {
        Paragraph footer = new Paragraph("\nThis certificate confirms that the above mentioned person is a registered member of Kolonnawa Barbell Gym.\n")
                .setFontSize(10)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic();

        Paragraph signature = new Paragraph("Authorized Signature\n\nKolonnawa Barbell Gym Management")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(20)
                .setMarginRight(50);

        document.add(footer);
        document.add(signature);
    }

    private void addLogoWatermark(Document document) {
        try {
            // Load logo for watermark
            Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mainlogo);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            logoBitmap.compress(Bitmap.CompressFormat.PNG, 30, stream); // Lower quality for watermark
            byte[] logoBytes = stream.toByteArray();

            ImageData logoImageData = ImageDataFactory.create(logoBytes);

            // Create multiple logo watermarks for better coverage
            for (int i = 0; i < 6; i++) {
                int x = 50 + (i % 2) * 250;
                int y = 150 + (i / 2) * 200;

                Image logoWatermark = new Image(logoImageData);
                logoWatermark.setWidth(80);
                logoWatermark.setHeight(80);
                logoWatermark.setOpacity(0.1f); // Very transparent
                logoWatermark.setRotationAngle(Math.toRadians(45));
                logoWatermark.setFixedPosition(x, y);

                document.add(logoWatermark);
            }

            // Add some text watermarks as well for variety
            for (int i = 0; i < 4; i++) {
                int x = 100 + (i % 2) * 200;
                int y = 100 + (i / 2) * 250;

                Paragraph textWatermark = new Paragraph("KOLONNAWA GYM")
                        .setFontSize(20)
                        .setFontColor(new DeviceRgb(200, 200, 200), 0.08f) // Very light gray
                        .setTextAlignment(TextAlignment.CENTER)
                        .setRotationAngle(Math.toRadians(45))
                        .setFixedPosition(x, y, 150);

                document.add(textWatermark);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to text watermark if logo fails
            try {
                for (int i = 0; i < 9; i++) {
                    int x = 50 + (i % 3) * 180;
                    int y = 150 + (i / 3) * 150;

                    Paragraph watermark = new Paragraph("KOLONNAWA GYM")
                            .setFontSize(24)
                            .setFontColor(new DeviceRgb(200, 200, 200), 0.1f)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setRotationAngle(Math.toRadians(45))
                            .setFixedPosition(x, y, 200);

                    document.add(watermark);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showPDFSuccessDialog(File pdfFile, String fileName) {
        new AlertDialog.Builder(this)
                .setTitle("Success!")
                .setMessage("✓ Member registered successfully!\n✓ PDF certificate generated: " + fileName)
                .setPositiveButton("View & Share PDF", (dialog, which) -> viewAndSharePDF(pdfFile))
                .setNegativeButton("OK", (dialog, which) -> {
                    Toast.makeText(this, "Member registration completed!", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void viewAndSharePDF(File pdfFile) {
        try {
            Uri pdfUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider",
                    pdfFile);

            // Create intent to view PDF
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setDataAndType(pdfUri, "application/pdf");
            viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Create intent to share PDF
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Kolonnawa Barbell Gym - Member Registration");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Member registration certificate from Kolonnawa Barbell Gym");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Create chooser with both options
            Intent chooserIntent = Intent.createChooser(viewIntent, "Member Certificate");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { shareIntent });

            startActivity(chooserIntent);

        } catch (Exception e) {
            Toast.makeText(this, "No PDF viewer app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        etFirstName.setText("");
        etLastName.setText("");
        etEmail.setText("");
        etPhoneNumber.setText("");
        etNIC.setText("");
        etMonthlyFee.setText("");
        statusRadioGroup.check(R.id.radioNo);
        deleteImage(); // This will reset the image
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}