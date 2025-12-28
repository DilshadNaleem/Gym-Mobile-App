package com.example.kolonnawabarbellgym.Adapter;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kolonnawabarbellgym.Model.ExpenseDetail;
import com.example.kolonnawabarbellgym.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseDetailAdapter extends RecyclerView.Adapter<ExpenseDetailAdapter.ExpenseViewHolder> {

    private List<ExpenseDetail> expenseList;
    private NumberFormat currencyFormat;

    public ExpenseDetailAdapter() {
        this.expenseList = new ArrayList<>();
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("si", "LK"));
        this.currencyFormat.setMaximumFractionDigits(2);
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_details, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseDetail expense = expenseList.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public void updateList(List<ExpenseDetail> newList) {
        expenseList.clear();
        expenseList.addAll(newList);
        notifyDataSetChanged();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivExpense;
        private TextView tvDescription, tvAmount, tvDate;
        private ImageView ivExpand;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivExpense = itemView.findViewById(R.id.ivExpense);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivExpand = itemView.findViewById(R.id.ivExpand);
        }

        public void bind(ExpenseDetail expense) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("si", "LK"));
            currencyFormat.setMaximumFractionDigits(2);

            tvDescription.setText(expense.getDescription());
            tvAmount.setText(currencyFormat.format(expense.getAmount()));

            // Format date
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            tvDate.setText(displayFormat.format(expense.getDate()));

            // Set image if available
            if (expense.getImage() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(expense.getImage(), 0, expense.getImage().length);
                ivExpense.setImageBitmap(bitmap);

                // Set click listener for image preview
                ivExpense.setOnClickListener(v -> showFullScreenImage(v, expense.getImage()));
            } else {
                ivExpense.setImageResource(R.drawable.ic_receipt);
                ivExpense.setOnClickListener(null);
            }
        }

        private void showFullScreenImage(View view, byte[] imageBytes) {
            if (imageBytes == null) return;

            // Create dialog
            Dialog dialog = new Dialog(view.getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(R.layout.dialog_image_preview);

            ImageView ivFullScreen = dialog.findViewById(R.id.ivFullScreen);
            ImageButton btnClose = dialog.findViewById(R.id.btnClose);
            ImageButton btnZoomIn = dialog.findViewById(R.id.btnZoomIn);
            ImageButton btnZoomOut = dialog.findViewById(R.id.btnZoomOut);

            // Load and display image
            Bitmap originalBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            ivFullScreen.setImageBitmap(originalBitmap);

            // Zoom functionality
            final float[] scale = {1.0f};
            final Matrix matrix = new Matrix();

            btnZoomIn.setOnClickListener(v -> {
                scale[0] += 0.5f;
                matrix.setScale(scale[0], scale[0]);
                ivFullScreen.setImageMatrix(matrix);
            });

            btnZoomOut.setOnClickListener(v -> {
                if (scale[0] > 0.5f) {
                    scale[0] -= 0.5f;
                    matrix.setScale(scale[0], scale[0]);
                    ivFullScreen.setImageMatrix(matrix);
                }
            });

            // Reset zoom on double tap
            ivFullScreen.setOnClickListener(v -> {
                scale[0] = 1.0f;
                matrix.setScale(scale[0], scale[0]);
                ivFullScreen.setImageMatrix(matrix);
            });

            // Close button
            btnClose.setOnClickListener(v -> dialog.dismiss());

            // Swipe to dismiss
            ivFullScreen.setOnTouchListener(new OnSwipeTouchListener(view.getContext()) {
                @Override
                public void onSwipeRight() {
                    dialog.dismiss();
                }

                @Override
                public void onSwipeLeft() {
                    dialog.dismiss();
                }

                @Override
                public void onSwipeTop() {
                    dialog.dismiss();
                }

                @Override
                public void onSwipeBottom() {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }
}