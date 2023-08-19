package com.example.virtualwaitress.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.virtualwaitress.R;
import com.example.virtualwaitress.models.Category;
import com.example.virtualwaitress.models.Dish;
import com.example.virtualwaitress.util.Callback;
import com.example.virtualwaitress.util.FirebaseManager;

import java.util.List;

public class DishAdapter extends RecyclerView.Adapter<DishAdapter.DishViewHolder>{
    private Context context;
    private List<Dish> dishes;
    private DishAdapter.OnItemClickListener listener;
    private FirebaseManager firebaseManager = new FirebaseManager();

    public interface OnItemClickListener {
        void onMenuItemClick(int position);
    }

    public void setOnItemClickListener(DishAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public DishAdapter(List<Dish> dishes) {
        this.dishes = dishes;
    }

    public static class DishViewHolder extends RecyclerView.ViewHolder {
        public TextView dishNameTextView;
        public TextView dishPriceTextView;
        public ImageView dishImage;
        public TextView categoryTextView;

        public DishViewHolder(@NonNull View itemView, final DishAdapter.OnItemClickListener listener) {
            super(itemView);
            dishNameTextView = itemView.findViewById(R.id.dishTitleTxt);
            dishPriceTextView = itemView.findViewById(R.id.dishPriceTxt);
            dishImage = itemView.findViewById(R.id.cardViewImg);
            categoryTextView = itemView.findViewById(R.id.categoryTxt);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onMenuItemClick(position);
                        }
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public DishAdapter.DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_card_item, parent, false);
        context = parent.getContext();
        return new DishAdapter.DishViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull DishAdapter.DishViewHolder holder, int position) {
        String imageUrl;

        Dish dish = dishes.get(position);

        holder.dishNameTextView.setText(dish.getName());
        holder.dishPriceTextView.setText(String.valueOf("$" + dish.getPrice()));
        imageUrl = dish.getImageUrl();

        Glide.with(context)
                .load(imageUrl)
                .fitCenter()
                .into(holder.dishImage);
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    public void updateDish(Dish dishToUpdate) {
        firebaseManager.updateDish(dishToUpdate, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(context.getApplicationContext(), "Dish updated successfully", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(context.getApplicationContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
        notifyDataSetChanged();
    }
}
