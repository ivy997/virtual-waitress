package com.example.virtualwaitress.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.virtualwaitress.R;
import com.example.virtualwaitress.models.CartItem;
import com.example.virtualwaitress.models.Dish;
import com.example.virtualwaitress.util.Callback;
import com.example.virtualwaitress.util.FirebaseManager;
import com.google.firebase.ktx.Firebase;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder>{
    private Context context;
    private List<CartItem> items;
    private FirebaseManager firebaseManager;
    private CartAdapter.OnItemClickListener listener;
    private OnCartItemChangedListener cartItemChangedListener;
    private TextView totalPrice;

    public interface OnCartItemChangedListener {
        void onCartItemChanged();
    }

    public interface OnItemClickListener {
        void OnCartItemClick(int position);
    }

    public void setOnCartItemChangedListener(OnCartItemChangedListener cartItemChangedListener) {
        this.cartItemChangedListener = cartItemChangedListener;
    }

    public void setOnItemClickListener(CartAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public CartAdapter(List<CartItem> items) {
        this.items = items;
        this.firebaseManager = new FirebaseManager();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        public ImageView pic;
        public TextView title;
        public TextView price;
        public TextView count;
        public TextView btnMinus;
        public TextView btnPlus;
        public TextView totalPrice;

        public CartViewHolder(@NonNull View itemView, final CartAdapter.OnItemClickListener listener) {
            super(itemView);

            pic = itemView.findViewById(R.id.menuItemPic);
            title = itemView.findViewById(R.id.titleTxt);
            price = itemView.findViewById(R.id.totalEachItem);
            count = itemView.findViewById(R.id.numberItemTxt);
            btnMinus = itemView.findViewById(R.id.minusCartBtn);
            btnPlus = itemView.findViewById(R.id.plusCartBtn);
            totalPrice = itemView.findViewById(R.id.totalTxtCart);

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
    public CartAdapter.CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        context = parent.getContext();
        return new CartAdapter.CartViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.CartViewHolder holder, int position) {
        String imageUrl;

        CartItem item = items.get(position);
        Dish dish = item.getDish();
        holder.title.setText(dish.getName());
        holder.price.setText(String.format("%.2f лв.", dish.getPrice()));
        holder.count.setText(String.valueOf(item.getQuantity()));
        totalPrice.setText(String.format("%.2f лв.", getTotalOfCart()));
        imageUrl = dish.getImageUrl();
        Glide.with(context)
                .load(imageUrl)
                .fitCenter()
                .into(holder.pic);

        holder.btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = item.getQuantity();
                if (count > 1) {
                    item.setQuantity(--count);
                    updateCartItem(item);
                    holder.count.setText(String.valueOf(count));
                    totalPrice.setText(String.format("%.2f лв.", getTotalOfCart()));
                    if (cartItemChangedListener != null) {
                        cartItemChangedListener.onCartItemChanged();
                    }
                } else if (count == 1) {
                    deleteCartItem(item.getCartItemId());
                    dish.setAddedToCart(false);
                    updateDish(dish);
                    items.remove(item);
                    setCartItems(items);
                    totalPrice.setText(String.format("%.2f лв.", getTotalOfCart()));
                    if (cartItemChangedListener != null) {
                        cartItemChangedListener.onCartItemChanged();
                    }
                }
            }
        });

        holder.btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = item.getQuantity();
                item.setQuantity(++count);
                updateCartItem(item);
                holder.count.setText(String.valueOf(count));
                totalPrice.setText(String.format("%.2f лв.", getTotalOfCart()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public float getBillPrice() {
        return getTotalOfCart();
    }

    public void setCartItems(List<CartItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setTotalPriceTV(TextView totalPrice) {
        this.totalPrice = totalPrice;
    }

    private float getTotalOfCart() {
        float totalSum = 0;
        if (items.size() > 0) {
            for (CartItem item : items) {
                Dish dish = item.getDish();
                totalSum += dish.getPrice() * item.getQuantity();
            }
        }
        return totalSum;
    }

    private void updateCartItem(CartItem item) {
        firebaseManager.updateCartItem(item, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void deleteCartItem(String cartItemId) {
        firebaseManager.deleteCartItem(cartItemId, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void updateDish(Dish dish) {
        firebaseManager.updateDish(dish, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }
}
