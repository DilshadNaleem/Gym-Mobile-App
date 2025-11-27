package com.example.kolonnawabarbellgym.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kolonnawabarbellgym.Model.Payment;
import com.example.kolonnawabarbellgym.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TodaySalesAdapter extends RecyclerView.Adapter<TodaySalesAdapter.ViewHolder> {

    private List<Payment> paymentsList;
    private Context context;

    public TodaySalesAdapter(Context context, List<Payment> paymentsList) {
        this.context = context;
        this.paymentsList = paymentsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_today_sale, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Payment payment = paymentsList.get(position);

        // Set member name
        holder.tvMemberName.setText(payment.getMemberName());

        // Set payment amount
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("si", "LK"));
        format.setMaximumFractionDigits(2);
        holder.tvAmount.setText(format.format(payment.getPrice()));

        // Set payment month
        holder.tvMonth.setText(payment.getMonth());

        // Set payment time
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String paymentTime = timeFormat.format(payment.getCreatedAt());
        holder.tvTime.setText(paymentTime);

        // Set payment type

    }

    @Override
    public int getItemCount() {
        return paymentsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName, tvAmount, tvMonth, tvTime, tvPaymentType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            tvTime = itemView.findViewById(R.id.tvTime);

        }
    }
}