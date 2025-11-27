package com.example.kolonnawabarbellgym.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kolonnawabarbellgym.Model.MemberSales;
import com.example.kolonnawabarbellgym.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MemberSalesAdapter extends RecyclerView.Adapter<MemberSalesAdapter.ViewHolder> {

    private List<MemberSales> memberSalesList;
    private NumberFormat currencyFormat;

    public MemberSalesAdapter(List<MemberSales> memberSalesList) {
        this.memberSalesList = memberSalesList;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("si", "LK"));
        this.currencyFormat.setMaximumFractionDigits(2);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member_sales, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemberSales member = memberSalesList.get(position);

        // Set member name
        holder.tvMemberName.setText(member.getName());

        // Set member ID
        holder.tvMemberId.setText("ID: " + member.getUniqueId());

        // Set join date
        if (member.getJoinDate() != null && !member.getJoinDate().isEmpty()) {
            String formattedDate = formatDate(member.getJoinDate());
            holder.tvJoinDate.setText("Joined: " + formattedDate);
        } else {
            holder.tvJoinDate.setText("Joined: N/A");
        }

        // Set admission fee
        holder.tvAdmissionFee.setText(currencyFormat.format(member.getAdmissionFee()));

        // Set profile image
        if (member.getProfileImage() != null && member.getProfileImage().length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(member.getProfileImage(), 0, member.getProfileImage().length);
            holder.ivProfile.setImageBitmap(bitmap);
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_person);
        }
    }

    @Override
    public int getItemCount() {
        return memberSalesList.size();
    }

    public void updateData(List<MemberSales> newList) {
        memberSalesList.clear();
        memberSalesList.addAll(newList);
        notifyDataSetChanged();
    }

    private String formatDate(String dateString) {
        try {
            // Assuming date is in format "yyyy-MM-dd HH:mm:ss"
            if (dateString.length() >= 10) {
                return dateString.substring(0, 10); // Return only yyyy-MM-dd part
            }
            return dateString;
        } catch (Exception e) {
            return dateString;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvMemberName;
        TextView tvMemberId;
        TextView tvJoinDate;
        TextView tvAdmissionFee;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberId = itemView.findViewById(R.id.tvMemberId);
            tvJoinDate = itemView.findViewById(R.id.tvJoinDate);
            tvAdmissionFee = itemView.findViewById(R.id.tvAdmissionFee);
        }
    }
}