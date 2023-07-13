package com.example.virtualwaitress.util;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.virtualwaitress.models.CartItem;
import com.example.virtualwaitress.models.Category;
import com.example.virtualwaitress.models.Dish;
import com.example.virtualwaitress.models.Order;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseManager {
    private FirebaseFirestore db;
    private CollectionReference categoriesRef;
    private CollectionReference dishesRef;
    private CollectionReference cartRef;
    private CollectionReference ordersRef;

    public FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        categoriesRef = db.collection("Categories");
        dishesRef = db.collection("Dishes");
        cartRef = db.collection("Cart");
        ordersRef = db.collection("Orders");
    }

    public void getCategories(Callback <List<Category>> callback) {
       categoriesRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Category> categories = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Category category = document.toObject(Category.class);
                                categories.add(category);
                            }
                            callback.onSuccess(categories);
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                }).addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void getDishes(Callback <List<Dish>> callback) {
        dishesRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Dish> dishes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Dish dish = document.toObject(Dish.class);
                        dishes.add(dish);
                    }
                    callback.onSuccess(dishes);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void updateDish(Dish dish, Callback<Void> callback) {
        DocumentReference documentRef = dishesRef.document(dish.getDishId());

        Map<String, Object> updates = new HashMap<>();
        updates.put("dishId", dish.getDishId());
        updates.put("name", dish.getName());
        updates.put("description", dish.getDescription());
        updates.put("price", dish.getPrice());
        updates.put("imageUrl", dish.getImageUrl());
        updates.put("addedToCart", dish.isAddedToCart());

        documentRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void getDishesByCategory(String categoryId, Callback <List<Dish>> callback) {
        dishesRef.whereEqualTo("categoryId", categoryId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Dish> dishList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Dish dish = document.toObject(Dish.class);
                                dishList.add(dish);
                            }
                            callback.onSuccess(dishList);
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    }
                });
    }

    public void addCartItem(CartItem item, Callback<String> callback) {
        cartRef.add(item)
                .addOnSuccessListener(documentReference -> {
                    // CartItem added successfully
                    String cartItemId = documentReference.getId();
                    callback.onSuccess(cartItemId);
                })
                .addOnFailureListener(e -> {
                    // Error adding CartItem
                    callback.onError(e.getMessage());
                });
    }

    public void updateCartItem(CartItem item, Callback<Void> callback) {
        DocumentReference documentRef = cartRef.document(item.getCartItemId());

        Map<String, Object> updates = new HashMap<>();
        updates.put("cartItemId", item.getCartItemId());
        updates.put("dish", item.getDish());
        updates.put("quantity", item.getQuantity());

        documentRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void deleteCartItem(String cartItemId, Callback<Void> callback) {
        cartRef.document(cartItemId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    public void getCart(Callback <List<CartItem>> callback) {
        try {
            cartRef.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                List<CartItem> items = new ArrayList<>();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    CartItem item = document.toObject(CartItem.class);
                                    items.add(item);
                                    Log.d("TAG", document.getId() + " => " + document.getData());
                                }
                                callback.onSuccess(items);
                            } else {
                                Log.d("TAG", "Error getting documents: ", task.getException());
                            }
                        }
                    }).addOnFailureListener(e -> {
                        callback.onError(e.getMessage());
                    }).addOnSuccessListener(s -> {
                    });
        } catch (Exception e) {
            Log.d("Try catch", e.getMessage());
        }
    }

    public void createOrder(Order order, Callback<String> callback) {
        ordersRef.add(order)
                .addOnSuccessListener(documentReference -> {
                    String orderId = documentReference.getId();
                    callback.onSuccess(orderId);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void updateOrder(Order order, Callback<Void> callback) {
        DocumentReference documentRef = ordersRef.document(order.getOrderId());

        Map<String, Object> updates = new HashMap<>();
        updates.put("orderId", order.getOrderId());
        updates.put("items", order.getItems());
        updates.put("orderDateAndTime", order.getOrderDateAndTime());
        updates.put("orderStatus", order.getOrderStatus());
        updates.put("billPrice", order.getBillPrice());

        documentRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
}
