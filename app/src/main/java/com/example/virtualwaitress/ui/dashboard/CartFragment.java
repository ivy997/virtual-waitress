package com.example.virtualwaitress.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.virtualwaitress.R;
import com.example.virtualwaitress.adapters.CartAdapter;
import com.example.virtualwaitress.databinding.FragmentCartBinding;
import com.example.virtualwaitress.enums.OrderStatus;
import com.example.virtualwaitress.models.CartItem;
import com.example.virtualwaitress.models.Order;
import com.example.virtualwaitress.util.Callback;
import com.example.virtualwaitress.util.FirebaseManager;
import com.google.type.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnItemClickListener, CartAdapter.OnCartItemChangedListener {

    private FragmentCartBinding binding;
    private FirebaseManager firebaseManager;
    private RecyclerView cartRecyclerView;
    private Button placeOrderButton;
    private TextView totalCartPrice;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private float billPrice;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CartViewModel cartViewModel =
                new ViewModelProvider(this).get(CartViewModel.class);

        binding = FragmentCartBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        cartViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        firebaseManager = new FirebaseManager();
        cartItems = new ArrayList<>();
        totalCartPrice = root.findViewById(R.id.totalTxtCart);

        // Initialize RecyclerView
        cartRecyclerView = root.findViewById(R.id.cartRecyclerView);
        /*cartRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, true));
        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(cartItems);
        cartRecyclerView.setAdapter(cartAdapter);*/

        cartAdapter = new CartAdapter(cartItems);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        cartAdapter.setOnItemClickListener(this);
        cartRecyclerView.setAdapter(cartAdapter);

        getCartItems();

        // Initialize Place Order button
        placeOrderButton = root.findViewById(R.id.orderBtn);
        placeOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform actions to place the order
                // ...
                Date today = getCurrentDateTime();
                OrderStatus initialStatus = OrderStatus.PLACED;
                billPrice = cartAdapter.getBillPrice();
                Order order = new Order(cartItems, today, initialStatus, billPrice);
                createOrder(order);
                //Toast.makeText(getContext(), "Order placed.", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private Date getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Month is 0-based, so add 1
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        // Create a Calendar instance and set the components
        Calendar dateTimeCalendar = Calendar.getInstance();
        dateTimeCalendar.set(year, month, day, hour, minute, second);

        // Get the Date object from the Calendar instance
        Date date = dateTimeCalendar.getTime();
        return date;
    }

    @Override
    public void onCartItemChanged() {
        // Update the total price dynamically
        billPrice = cartAdapter.getBillPrice();
        //totalCartPrice.setText(String.format("%.2f лв.", billPrice));
    }

    @Override
    public void OnCartItemClick(int position) {
    }

    /*private void addCartItem(CartItem cartItem) {
        cartItems.add(cartItem);
        cartAdapter.setCartItems(cartItems);
    }*/

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void getCartItems() {
        cartAdapter.setOnCartItemChangedListener(this);
        firebaseManager.getCart(new Callback<List<CartItem>>() {
            @Override
            public void onSuccess(List<CartItem> result) {
                cartItems = result;
                cartAdapter.setCartItems(cartItems);
                cartAdapter.setTotalPriceTV(totalCartPrice);
                //billPrice = cartAdapter.getBillPrice();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createOrder(Order order) {
        firebaseManager.createOrder(order, new Callback<String>() {
            @Override
            public void onSuccess(String result) {
                order.setOrderId(result);
                updateOrder(order);
                Toast.makeText(getActivity(), "Order placed successfully.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void updateOrder(Order order) {
        firebaseManager.updateOrder(order, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }
}