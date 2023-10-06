package com.example.virtualwaitress.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.virtualwaitress.activities.CategoryDetailsActivity;
import com.example.virtualwaitress.R;
import com.example.virtualwaitress.adapters.CategoryAdapter;
import com.example.virtualwaitress.adapters.DishAdapter;
import com.example.virtualwaitress.databinding.FragmentHomeBinding;
import com.example.virtualwaitress.dialogs.CartDialog;
import com.example.virtualwaitress.models.CartItem;
import com.example.virtualwaitress.models.Category;
import com.example.virtualwaitress.models.Dish;
import com.example.virtualwaitress.util.Callback;
import com.example.virtualwaitress.util.FirebaseManager;
import com.example.virtualwaitress.util.RestaurantUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment implements CategoryAdapter.OnItemClickListener, DishAdapter.OnItemClickListener {
    private FragmentHomeBinding binding;
    private RecyclerView categoryRecyclerView;
    private RecyclerView dishRecyclerView;
    private LinearLayout containerLayout;
    private SearchView searchView;
    private List<Category> categories;
    private CategoryAdapter categoryAdapter;
    private DishAdapter dishAdapter;
    private FirebaseManager firebaseManager;
    private List<Dish> dishes;
    private String currentUserId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        searchView = root.findViewById(R.id.searchView);
        searchView.setQueryHint("Search");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getDishes();
                return true;
            }
        });

        // Initialize RecyclerViews
        categoryRecyclerView = root.findViewById(R.id.categoryRecyclerView);
        dishRecyclerView = root.findViewById(R.id.dishRecyclerView);

        // Initialize data lists for menu items and categories
        dishes = new ArrayList<>();
        categories = new ArrayList<>();
        firebaseManager = new FirebaseManager();

        // Initialize adapters and set them to respective RecyclerViews
        categoryAdapter = new CategoryAdapter(categories);
        categoryRecyclerView.setAdapter(categoryAdapter);
        dishAdapter = new DishAdapter(dishes);
        dishRecyclerView.setAdapter(dishAdapter);

        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true));
        categoryAdapter.setOnItemClickListener(this);
        dishRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        dishAdapter.setOnItemClickListener(this);

        containerLayout = root.findViewById(R.id.containerLayout);

        if (RestaurantUser.getInstance() != null) {
            currentUserId = RestaurantUser.getInstance().getUserId();
        }

        getCategories(currentUserId);
        getDishes();

        return root;
    }

    @Override
    public void onMenuItemClick(int position) {
        Dish dish = dishes.get(position);
        showAddToCartDialog(dish);
    }

    @Override
    public void OnCategoryClick(int position) {
        firebaseManager.getDishes(currentUserId, new Callback<List<Dish>>() {
                    @Override
                    public void onSuccess(List<Dish> result) {
                        dishes = result;
                        Category category = categories.get(position);
                        Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                        String categoryId = category.getCategoryId();
                        List<Dish> filteredDishes = getDishesByCategory(categoryId);
                        intent.putExtra("categoryId", categoryId);
                        intent.putExtra("dishes", (Serializable) filteredDishes);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(String errorMessage) {
                    }
                });
    }

    private List<Dish> getDishesByCategory(String categoryId) {
        List<Dish> filteredDishes = new ArrayList<>();
        if (dishes != null) {
            for (Dish dish : dishes) {
                if (dish.getCategoryId().equals(categoryId)) {
                    filteredDishes.add(dish);
                }
            }
            return filteredDishes;
        }
        return null;
    }

    private void getCategories(String userId) {
        firebaseManager.getCategories(userId, new Callback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result) {
                categories = result;
                categoryAdapter.setCategories(categories);
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void getDishes() {
        firebaseManager.getDishes(currentUserId, new Callback<List<Dish>>() {
            @Override
            public void onSuccess(List<Dish> result) {
                dishes = result;
                dishAdapter.setDishes(dishes);
            }

            @Override
            public void onError(String errorMessage) {
                // Handle error
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showAddToCartDialog(Dish dish) {
        CartDialog dialog = new CartDialog(getContext(), dish);
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
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
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
                Toast.makeText(getActivity(), "Item added to cart successfully.", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(), "Menu item is already added to cart", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                CartItem item = new CartItem(dish, count, savedTableNumber, currentUserId);
                addCartItem(item);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSearch(String query) {
        if (!TextUtils.isEmpty(query)) {
            getDishesByName(currentUserId, query);
        }
    }

    private void getDishesByName(String userId, String query) {
        firebaseManager.getDishesByName(userId, query, new Callback<List<Dish>>() {
            @Override
            public void onSuccess(List<Dish> result) {
                dishes.clear();
                dishAdapter.setDishes(result);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            hideKeyboard();
        }
        return true; // Consume the event
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }*/
}