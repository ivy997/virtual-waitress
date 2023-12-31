package com.example.virtualwaitress.ui.notifications;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.virtualwaitress.LoginActivity;
import com.example.virtualwaitress.MainActivity;
import com.example.virtualwaitress.R;
import com.example.virtualwaitress.activities.OrderDetailsActivity;
import com.example.virtualwaitress.databinding.FragmentNotificationsBinding;
import com.example.virtualwaitress.enums.OrderStatus;
import com.example.virtualwaitress.models.CartItem;
import com.example.virtualwaitress.models.Order;
import com.example.virtualwaitress.util.Callback;
import com.example.virtualwaitress.util.FirebaseManager;
import com.example.virtualwaitress.util.RestaurantUser;
import com.google.android.material.slider.Slider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotificationsFragment extends Fragment {
    private FragmentNotificationsBinding binding;
    private FirebaseManager firebaseManager;
    private Order order;
    private String currentUserId;
    private Button payBtn;
    private Button orderDetailsBtn;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseManager = new FirebaseManager();

        if (RestaurantUser.getInstance() != null) {
            currentUserId = RestaurantUser.getInstance().getUserId();
        }

        orderDetailsBtn = root.findViewById(R.id.orderDetailsBtn);
        payBtn = root.findViewById(R.id.payBtn);
        orderDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), OrderDetailsActivity.class);
                intent.putExtra("order", (Serializable) order);
                startActivity(intent);
            }
        });

        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order.setOrderStatus(OrderStatus.COMPLETED);
                updateOrder(order);
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).refreshActivity();
                }
            }
        });

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int savedTableNumber = sharedPreferences.getInt("tableNumber", -1); // -1 is the default value if not found
        if (savedTableNumber != -1) {
            getOrder(currentUserId, savedTableNumber, root);
        } else {
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void getOrder(String userId, int tableNumber, View root) {
        List<OrderStatus> excludedStatuses = new ArrayList<>();
        excludedStatuses.add(OrderStatus.CANCELED);
        excludedStatuses.add(OrderStatus.PAID);
        firebaseManager.getTableOrder(userId, tableNumber, excludedStatuses, new Callback<Order>() {
            @Override
            public void onSuccess(Order result) {
                order = result;

                updateOrderStatus(result.getOrderStatus(), root);

                if (result.getOrderStatus() == OrderStatus.SERVED) {
                    payBtn.setVisibility(View.VISIBLE);
                }

                FrameLayout includeContainer = root.findViewById(R.id.includeContainer);
                includeContainer.setVisibility(View.VISIBLE);
                orderDetailsBtn.setVisibility(View.VISIBLE);

                final TextView textView = binding.textNotifications;
                textView.setVisibility(View.GONE);

                // Set up a real-time listener for the Order document
                firebaseManager.listenForOrderChanges(result.getOrderId(), new Callback<Order>() {
                    @Override
                    public void onSuccess(Order updatedOrder) {
                        // Handle the updated Order object
                        if (updatedOrder != null && !updatedOrder.getOrderStatus().equals(result.getOrderStatus())) {
                            // The order has changed, update the UI or perform necessary actions
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).refreshActivity();
                            }
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Handle error
                        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                FrameLayout includeContainer = root.findViewById(R.id.includeContainer);
                final TextView textView = binding.textNotifications;
                textView.setVisibility(View.VISIBLE);
                includeContainer.setVisibility(View.GONE); // To make the included layout visible
            }
        });
    }

    private void updateOrderStatus(OrderStatus status, View root) {
        OrderStatus[] statusArr = OrderStatus.values();
        int pos = findPosition(statusArr, status);
        View[] viewArr = getUiComponents(root);
        View currView = viewArr[pos];
        if (pos == 0) {
            for (int i = pos + 1; i < viewArr.length; i++) {
                View viewInLoop = viewArr[i];
                if (pos == 1) {
                    viewInLoop.setBackgroundResource(R.drawable.in_progress);
                    continue;
                }
                viewInLoop.setBackgroundResource(R.drawable.not_started);
            }
        } else if (pos == viewArr.length - 1) {
            for (int i = pos - 1; i > 0; i--) {
                View viewInLoop = viewArr[i];
                viewInLoop.setBackgroundResource(R.drawable.done);
            }
            currView.setBackgroundResource(R.drawable.done);
        } else {
            for (int i = pos - 1; i > 0; i--) {
                View viewInLoop = viewArr[i];
                viewInLoop.setBackgroundResource(R.drawable.done);
            }
            currView.setBackgroundResource(R.drawable.in_progress);
            for (int i = pos + 1; i < viewArr.length; i++) {
                View viewInLoop = viewArr[i];
                viewInLoop.setBackgroundResource(R.drawable.not_started);
            }
        }
    }

    public static int findPosition(OrderStatus[] array, OrderStatus target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null && array[i].equals(target)) {
                return i; // Found the target string at position i
            }
        }
        return -1; // Target string not found in the array
    }

    private View[] getUiComponents(View root) {
        View placed = root.findViewById(R.id.PLACED);
        View preparing = root.findViewById(R.id.PREPARING);
        View ready = root.findViewById(R.id.READY);
        View served = root.findViewById(R.id.SERVED);
        View completed = root.findViewById(R.id.COMPLETED);
        return new View[] {placed, preparing, ready, served, completed};
    }

    private void updateOrder(Order order) {
        firebaseManager.updateOrder(order, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}