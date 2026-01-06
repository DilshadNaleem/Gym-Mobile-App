package com.example.kolonnawabarbellgym.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kolonnawabarbellgym.DTO.UserModel;
import com.example.kolonnawabarbellgym.R;
import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private Context context;
    private List<UserModel> userList;
    private OnItemClickListener onItemClickListener;
     OnItemDeleteListener onItemDeleteListener;

    public interface OnItemClickListener {
        void onItemClick(UserModel user);
    }

    public interface OnItemDeleteListener {
        void onItemDelete(UserModel user);
    }

    public UserListAdapter(Context context, List<UserModel> userList, OnItemClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.onItemClickListener = listener;
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.onItemDeleteListener = listener;
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
            holder.ivProfile.setImageResource(R.drawable.ic_person);
        }

        holder.itemView.setOnClickListener(v -> {
            // ADD LOGS HERE - This is where the click is handled
            Log.d("UserListAdapter", "Item clicked at position: " + position);
            Log.d("UserListAdapter", "User name: " + user.getFullName());
            Log.d("UserListAdapter", "User ID: " + user.getUniqueId());

            // Test if onItemClickListener is null
            if (onItemClickListener == null) {
                Log.e("UserListAdapter", "onItemClickListener is NULL!");
                // You can also show a toast for debugging
                Toast.makeText(context, "ClickListener is null", Toast.LENGTH_SHORT).show();
                return;
            } else {
                Log.d("UserListAdapter", "onItemClickListener is NOT null, calling onItemClick...");
            }

            onItemClickListener.onItemClick(user);
        });

        // Add swipe background and icon
        holder.swipeBackground.setVisibility(View.GONE);
        holder.swipeDeleteIcon.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateList(List<UserModel> newList) {
        userList = newList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < userList.size()) {
            userList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public UserModel getItem(int position) {
        if (position >= 0 && position < userList.size()) {
            return userList.get(position);
        }
        return null;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvUserName, tvUserEmail, tvMonthlyFee;
        View swipeBackground;
        ImageView swipeDeleteIcon;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvMonthlyFee = itemView.findViewById(R.id.tvMonthlyFee);
            swipeBackground = itemView.findViewById(R.id.swipeBackground);
            swipeDeleteIcon = itemView.findViewById(R.id.swipeDeleteIcon);
        }
    }
}