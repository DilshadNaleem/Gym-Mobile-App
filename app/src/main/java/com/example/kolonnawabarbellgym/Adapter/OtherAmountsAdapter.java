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

import com.example.kolonnawabarbellgym.Model.OtherAmount;
import com.example.kolonnawabarbellgym.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OtherAmountsAdapter extends RecyclerView.Adapter<OtherAmountsAdapter.ViewHolder> {

    private List<OtherAmount> otherAmountList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(OtherAmount otherAmount);
    }

    public OtherAmountsAdapter(android.content.Context context, List<OtherAmount> otherAmountList) {
        this.otherAmountList = otherAmountList;
        if (context instanceof OnItemClickListener) {
            this.listener = (OnItemClickListener) context;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_other_amount, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OtherAmount otherAmount = otherAmountList.get(position);
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("si", "LK"));
        format.setMaximumFractionDigits(2);

        // Set profile image
        if (otherAmount.getProfileImage() != null && otherAmount.getProfileImage().length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(otherAmount.getProfileImage(), 0, otherAmount.getProfileImage().length);
            holder.profileImage.setImageBitmap(bitmap);
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_person); // Add a placeholder drawable
        }

        holder.tvName.setText(otherAmount.getFirstName());
        holder.tvDescription.setText(otherAmount.getDescription());
        holder.tvAmount.setText(format.format(otherAmount.getOtherAmount()));
        holder.tvDate.setText(otherAmount.getFormattedDate());
        holder.tvHandoveredTo.setText("Handovered to: " + otherAmount.getHandoveredTo());

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(otherAmount);
            }
        });
    }

    @Override
    public int getItemCount() {
        return otherAmountList.size();
    }

    public void updateList(List<OtherAmount> newList) {
        otherAmountList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView tvName, tvDescription, tvAmount, tvDate, tvHandoveredTo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvHandoveredTo = itemView.findViewById(R.id.tvHandoveredTo);
        }
    }
}