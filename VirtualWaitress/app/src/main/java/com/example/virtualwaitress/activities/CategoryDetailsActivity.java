package com.example.virtualwaitress.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.virtualwaitress.R;
import com.example.virtualwaitress.adapters.DishAdapter;
import com.example.virtualwaitress.databinding.ActivityCategoryDetailsBinding;
import com.example.virtualwaitress.dialogs.CartDialog;
import com.example.virtualwaitress.models.CartItem;
import com.example.virtualwaitress.models.Dish;
import com.example.virtualwaitress.util.Callback;
import com.example.virtualwaitress.util.FirebaseManager;
import com.example.virtualwaitress.util.RestaurantUser;

import org.checkerframework.checker.units.qual.C;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

public class CategoryDetailsActivity extends AppCompatActivity implements DishAdapter.OnItemClickListener {

    private FirebaseManager firebaseManager;
    private DishAdapter dishAdapter;
    private List<Dish> dishes;
    private RecyclerView dishRecyclerView;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_details);

        // Enable the up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Category Details");

        firebaseManager = new FirebaseManager();

        if (RestaurantUser.getInstance() != null) {
            currentUserId = RestaurantUser.getInstance().getUserId();
        }

        dishes = (List<Dish>) getIntent().getSerializableExtra("dishes");
        dishRecyclerView = findViewById(R.id.dishRecyclerView);
        dishAdapter = new DishAdapter(dishes);
        dishRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        dishRecyclerView.setHasFixedSize(true);
        dishAdapter.setOnItemClickListener(this);
        dishRecyclerView.setAdapter(dishAdapter);
    }

    @Override
    public void onMenuItemClick(int position) {
        Dish dish = dishes.get(position);
        showAddToCartDialog(dish);
    }

    private void showAddToCartDialog(Dish dish) {
        CartDialog dialog = new CartDialog(this, dish);
        dialog.show();

        Button btnAddToCart = dialog.findViewById(R.id.addToCartButton);
        btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = dialog.getCount();
                addToCart(dish, count);
                dialog.dismiss();
            }
        });
    }

    private void addToCart(Dish dish, int count) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int savedTableNumber = sharedPreferences.getInt("tableNumber", -1); // -1 is the default value if not found
        if (savedTableNumber != -1) {
            getCartItems(savedTableNumber, dish, count);
        }
    }

    private void addCartItem(CartItem item) {
        firebaseManager.addCartItem(item, new Callback<String>() {
            @Override
            public void onSuccess(String result) {
                item.setCartItemId(result);
                updateCartItem(item);
                Toast.makeText(CategoryDetailsActivity.this, "Item added to cart successfully.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
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

    private void getCartItems(int savedTableNumber, Dish dish, int count) {
        firebaseManager.getCart(currentUserId, savedTableNumber, new Callback<List<CartItem>>() {
            @Override
            public void onSuccess(List<CartItem> result) {
                for (CartItem cartItem : result) {
                    String currDishId = cartItem.getDish().getDishId();
                    if (Objects.equals(currDishId, dish.getDishId())) {
                        Toast.makeText(CategoryDetailsActivity.this,
                                "Menu item is already added to cart", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                CartItem item = new CartItem(dish, count, savedTableNumber, currentUserId);
                addCartItem(item);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}