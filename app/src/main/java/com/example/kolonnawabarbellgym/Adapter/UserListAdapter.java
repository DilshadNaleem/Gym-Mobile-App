package com.example.kolonnawabarbellgym.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kolonnawabarbellgym.DTO.UserModel;
import com.example.kolonnawabarbellgym.R;
import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private Context context;
    private List<UserModel> userList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(UserModel user);
    }

    public UserListAdapter(Context context, List<UserModel> userList, OnItemClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_list, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);

        holder.tvUserName.setText(user.getFullName());
        holder.tvUserEmail.setText(user.getEmail());

        // Set monthly fee if available
        if (user.getMonthlyFee() != null && !user.getMonthlyFee().isEmpty()) {
            holder.tvMonthlyFee.setText("Fee: Rs. " + user.getMonthlyFee());
            holder.tvMonthlyFee.setVisibility(View.VISIBLE);
        } else {
            holder.tvMonthlyFee.setVisibility(View.GONE);
        }

        // Set profile image
        if (user.getProfileImage() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(user.getProfileImage(), 0, user.getProfileImage().length);
            holder.ivProfile.setImageBitmap(bitmap);
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_person); // Create this drawable
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateList(List<UserModel> newList) {
        userList = newList;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvUserName, tvUserEmail, tvMonthlyFee;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvMonthlyFee = itemView.findViewById(R.id.tvMonthlyFee);
        }
    }
}