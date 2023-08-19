package com.example.virtualwaitress.ui.dashboard;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.virtualwaitress.MainActivity;
import com.example.virtualwaitress.R;
import com.example.virtualwaitress.adapters.CartAdapter;
import com.example.virtualwaitress.databinding.FragmentCartBinding;
import com.example.virtualwaitress.enums.OrderStatus;
import com.example.virtualwaitress.models.CartItem;
import com.example.virtualwaitress.models.Dish;
import com.example.virtualwaitress.models.Order;
import com.example.virtualwaitress.util.Callback;
import com.example.virtualwaitress.util.FirebaseManager;
import com.example.virtualwaitress.util.RestaurantUser;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.util.Listener;
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
    private String currentUserId;
    private int savedTableNumber;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CartViewModel cartViewModel =
                new ViewModelProvider(this).get(CartViewModel.class);

        binding = FragmentCartBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        cartViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        if (RestaurantUser.getInstance() != null) {
            currentUserId = RestaurantUser.getInstance().getUserId();
        }

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        savedTableNumber = sharedPreferences.getInt("tableNumber", -1); // -1 is the default value if not found

        firebaseManager = new FirebaseManager();
        cartItems = new ArrayList<>();
        totalCartPrice = root.findViewById(R.id.totalTxtCart);

        cartRecyclerView = root.findViewById(R.id.cartRecyclerView);
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
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
                int savedTableNumber = sharedPreferences.getInt("tableNumber", -1); // -1 is the default value if not found
                if (savedTableNumber != -1) {
                    getOrder(currentUserId, savedTableNumber);
                } else {
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(getContext(), "Order placed successfully.", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private Date getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
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
        billPrice = cartAdapter.getBillPrice();
        //totalCartPrice.setText(String.format("%.2f лв.", billPrice));
    }

    @Override
    public void OnCartItemClick(int position) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void getCartItems() {
        cartAdapter.setOnCartItemChangedListener(this);
        firebaseManager.getCart(currentUserId, savedTableNumber, new Callback<List<CartItem>>() {
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
                //cartAdapter.deleteCartItems();
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
                if (savedTableNumber != -1) {
                    cartAdapter.deleteCartItems(currentUserId, savedTableNumber);
                }
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void getOrder(String userId, int tableNumber) {
        List<OrderStatus> excludedStatuses = new ArrayList<>();
        excludedStatuses.add(OrderStatus.CANCELED);
        excludedStatuses.add(OrderStatus.PAID);
        firebaseManager.getTableOrder(userId, tableNumber, excludedStatuses, new Callback<Order>() {
            @Override
            public void onSuccess(Order result) {
                List<CartItem> currItems = result.getItems();
                currItems.addAll(cartItems);
                float currBill = calculateUpdatedBill(currItems);
                result.setItems(currItems);
                result.setBillPrice(currBill);
                result.setOrderStatus(OrderStatus.PREPARING);
                updateOrder(result);
            }

            @Override
            public void onError(String errorMessage) {
                Date today = getCurrentDateTime();
                OrderStatus initialStatus = OrderStatus.PLACED;
                billPrice = cartAdapter.getBillPrice();
                Order order = new Order(cartItems, today, initialStatus, billPrice, tableNumber, currentUserId);
                createOrder(order);
            }
        });
    }
    private float calculateUpdatedBill(List<CartItem> items) {
        float bill = 0;
        for (CartItem item : items) {
            Dish currDish = item.getDish();
            currDish.setAddedToCart(false);
            updateDish(currDish);
            float currPrice = item.getQuantity() * currDish.getPrice();
            bill += currPrice;
        }
        return bill;
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