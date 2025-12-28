package com.example.kolonnawabarbellgym.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kolonnawabarbellgym.Model.Member;
import com.example.kolonnawabarbellgym.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ExistingMemberAdapter extends RecyclerView.Adapter<ExistingMemberAdapter.ViewHolder> {

    private List<Member> membersList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Member member);
    }

    public ExistingMemberAdapter(android.content.Context context, List<Member> membersList) {
        this.membersList = membersList;
        if (context instanceof OnItemClickListener) {
            this.listener = (OnItemClickListener) context;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_existing_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Member member = membersList.get(position);

        // Set member name
        holder.tvMemberName.setText(member.getName());

        // Set profile image
       if (member.getProfileImage() != null && member.getProfileImage().length> 0)
       {
           Glide.with(holder.itemView.getContext())
                   .asBitmap()
                   .load(member.getProfileImage())
                   .placeholder(R.drawable.ic_person)
                   .error(R.drawable.ic_person)
                   .into(holder.ivProfile);
       }
       else {
           holder.ivProfile.setImageResource(R.drawable.ic_person);
       }

        // Set monthly fee
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("si", "LK"));
        format.setMaximumFractionDigits(2);

        holder.tvMonthlyFee.setText(format.format(member.getMonthlyFee()));
        holder.tvUnpaidMonths.setText(String.valueOf(member.getUnpaidMonths()));
        holder.tvTotalDue.setText(format.format(member.getTotalDue()));

        // Set last payment info
        if (member.getLastPaymentMonth() != null) {
            holder.tvLastPayment.setText("Last paid: " + member.getLastPaymentMonth());
        } else {
            holder.tvLastPayment.setText("Last paid: Current month");
        }

        // Item click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(member);
            }
        });
    }

    @Override
    public int getItemCount() {
        return membersList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvMemberName, tvMonthlyFee, tvUnpaidMonths, tvTotalDue, tvLastPayment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMonthlyFee = itemView.findViewById(R.id.tvMonthlyFee);
            tvUnpaidMonths = itemView.findViewById(R.id.tvUnpaidMonths);
            tvTotalDue = itemView.findViewById(R.id.tvTotalDue);
            tvLastPayment = itemView.findViewById(R.id.tvLastPayment);
        }
    }


}