package com.example.virtualwaitress.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.virtualwaitress.R;
import com.example.virtualwaitress.models.CartItem;
import com.example.virtualwaitress.util.FirebaseManager;

import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartViewHolder>{
    private Context context;
    private List<CartItem> items;
    private FirebaseManager firebaseManager;
    private OnItemClickListener listener;
    private OnLoadingCompleteListener loadingCompleteListener;
    private float totalPrice;

    public interface OnItemClickListener {
        void OnCartItemClick(int position);
    }

    public interface OnLoadingCompleteListener {
        void onLoadingComplete();
    }

    public void setOnLoadingCompleteListener(OnLoadingCompleteListener listener) {
        this.loadingCompleteListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public CartItemAdapter(List<CartItem> items) {
        this.items = items;
        this.firebaseManager = new FirebaseManager();
        this.totalPrice = 0;

    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView quantity;
        public TextView price;

        public CartViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            title = itemView.findViewById(R.id.dishName);
            quantity = itemView.findViewById(R.id.itemCount);
            price = itemView.findViewById(R.id.itemPrice);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.OnCartItemClick(position);
                        }
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        context = parent.getContext();
        return new CartViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = items.get(position);
        String dishTitle = item.getDish().getName();
        int itemCount = item.getQuantity();
        float dishPrice = item.getDish().getPrice();
        float dishesPrice = dishPrice * itemCount;

        if (position == items.size() - 1) {
            calculateTotalPrice();
        }

        holder.title.setText(dishTitle);
        holder.quantity.setText(String.format("x" + itemCount));
        holder.price.setText(String.format(dishesPrice + " лв."));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public float getPrice() {
        return totalPrice;
    }

    private void calculateTotalPrice() {
        for (CartItem item : items) {
            float dishPrice = item.getDish().getPrice();
            int itemCount = item.getQuantity();
            totalPrice += dishPrice * itemCount;
        }

        // Call the loading complete listener here
        if (loadingCompleteListener != null) {
            loadingCompleteListener.onLoadingComplete();
        }
    }
}
