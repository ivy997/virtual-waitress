package com.example.virtualwaitress.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.virtualwaitress.R;
import com.example.virtualwaitress.adapters.CartItemAdapter;
import com.example.virtualwaitress.models.CartItem;
import com.example.virtualwaitress.models.Order;
import com.example.virtualwaitress.util.FirebaseManager;

import java.util.List;

public class OrderDetailsActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;
    private CartItemAdapter itemAdapter;
    private List<CartItem> items;
    private RecyclerView itemsRecyclerView;
    private TextView totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        totalPrice = findViewById(R.id.infoTxt);
        firebaseManager = new FirebaseManager();

        Order order = (Order) getIntent().getSerializableExtra("order");
        items = order.getItems();

        itemsRecyclerView = findViewById(R.id.cartItemRecyclerView);

        itemAdapter = new CartItemAdapter(items);
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        itemsRecyclerView.setAdapter(itemAdapter);
        itemAdapter.setOnLoadingCompleteListener(() -> {
            // Loading is complete, you can update the UI or perform actions here
            totalPrice.setText("Total: " + itemAdapter.getPrice() + " лв.");
        });
    }
}