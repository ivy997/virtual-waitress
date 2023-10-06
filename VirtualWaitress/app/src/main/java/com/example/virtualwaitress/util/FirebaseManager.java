package com.example.virtualwaitress.util;

import android.nfc.Tag;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.virtualwaitress.enums.OrderStatus;
import com.example.virtualwaitress.models.CartItem;
import com.example.virtualwaitress.models.Category;
import com.example.virtualwaitress.models.Dish;
import com.example.virtualwaitress.models.Order;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    public void getCategories(String userId, Callback<List<Category>> callback) {
        categoriesRef
                .whereEqualTo("userId", userId)
                .get()
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
                        } else {}
                    }
                }).addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
    public void getDishes(String userId, Callback<List<Dish>> callback) {
        dishesRef
                .whereEqualTo("userId", userId)
                .get()
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
    public void getDishesByName(String userId, String query, Callback<List<Dish>> callback) {
        dishesRef
                .whereEqualTo("userId", userId)
                .whereArrayContains("keywords", query.toLowerCase())
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
                            Log.d("FirestoreDebug", "No matching documents.");
                        }
                    }
                }).addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
    public void addCartItem(CartItem item, Callback<String> callback) {
        cartRef.add(item)
                .addOnSuccessListener(documentReference -> {
                    String cartItemId = documentReference.getId();
                    callback.onSuccess(cartItemId);
                })
                .addOnFailureListener(e -> {
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
    public void deleteCartItems(String userId, int tableNumber, Callback<Boolean> callback) {
        Query query = cartRef
                .whereEqualTo("userId", userId)
                .whereEqualTo("tableNumber", tableNumber);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Delete each cart item document
                    for (DocumentSnapshot document : task.getResult()) {
                        cartRef.document(document.getId())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                    }
                    callback.onSuccess(true);
                } else {
                    // Handle query failure
                    callback.onError(task.getException().getMessage());
                }
            }
        });
    }
    public void getCart(String userId, int tableNumber, Callback<List<CartItem>> callback) {
        try {
            cartRef.whereEqualTo("userId", userId)
                    .whereEqualTo("tableNumber", tableNumber)
                    .get()
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
    public void getTableOrder(String userId, int tableNumber, List<OrderStatus> excludedStatuses, Callback<Order> callback) {
        ordersRef
                .whereEqualTo("userId", userId)
                .whereEqualTo("tableNumber", tableNumber)
                .whereNotIn("orderStatus", excludedStatuses)
                .limit(1)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            if (documentSnapshot.exists()) {
                                Order order = documentSnapshot.toObject(Order.class);
                                callback.onSuccess(order);
                            } else {
                                Log.d("FirestoreDebug", "Document doesn't exist.");
                            }
                        } else {
                            callback.onError("");
                            Log.d("FirestoreDebug", "No matching documents.");
                        }
                    } else {
                        Log.e("FirestoreError", "Query failed: " + task.getException());
                        callback.onError(task.getException().getMessage());
                    }
                }).addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
    public void listenForOrderChanges(String orderId, Callback<Order> callback) {
        DocumentReference orderDocument = ordersRef.document(orderId);
        orderDocument.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    callback.onError(e.getMessage());
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Order order = documentSnapshot.toObject(Order.class);
                    callback.onSuccess(order);
                } else {}
            }
        });
    }
}
