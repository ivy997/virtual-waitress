package com.example.virtualwaitress.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.virtualwaitress.activities.CategoryDetailsActivity;
import com.example.virtualwaitress.R;
import com.example.virtualwaitress.adapters.CategoryAdapter;
import com.example.virtualwaitress.adapters.DishAdapter;
import com.example.virtualwaitress.databinding.FragmentHomeBinding;
import com.example.virtualwaitress.models.Category;
import com.example.virtualwaitress.models.Dish;
import com.example.virtualwaitress.util.Callback;
import com.example.virtualwaitress.util.FirebaseManager;
import com.example.virtualwaitress.util.RestaurantUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements CategoryAdapter.OnItemClickListener, DishAdapter.OnItemClickListener {

    private FragmentHomeBinding binding;
    private RecyclerView categoryRecyclerView;
    private LinearLayout containerLayout;
    private List<Category> categories;
    private CategoryAdapter categoryAdapter;
    private FirebaseManager firebaseManager;
    private List<Dish> dishes;
    private String currentUserId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Initialize RecyclerViews
        categoryRecyclerView = root.findViewById(R.id.categoryRecyclerView);

        // Initialize data lists for menu items and categories
        dishes = new ArrayList<>();
        categories = new ArrayList<>();
        firebaseManager = new FirebaseManager();

        // Initialize adapters and set them to respective RecyclerViews
        categoryAdapter = new CategoryAdapter(categories);
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Set layout managers for RecyclerViews (e.g., LinearLayoutManager or GridLayoutManager)
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true));
        categoryAdapter.setOnItemClickListener(this);

        containerLayout = root.findViewById(R.id.containerLayout);

        if (RestaurantUser.getInstance() != null) {
            currentUserId = RestaurantUser.getInstance().getUserId();
        }

        getCategories(currentUserId);
        getDishes();
        return root;
    }

    @Override
    public void onItemClick(int position) {
    }

    @Override
    public void OnCategoryClick(int position) {
        Category category = categories.get(position);
        Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
        String categoryId = category.getCategoryId();
        List<Dish> filteredDishes = getDishesByCategory(categoryId);
        intent.putExtra("categoryId", categoryId);
        intent.putExtra("dishes", (Serializable) filteredDishes);
        startActivity(intent);
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

    private Category findCategoryById(String categoryId) {
        if (categories != null) {
            for (Category category : categories) {
                if (category.getCategoryId().equals(categoryId)) {
                    return category;
                }
            }
        }
        return null;
    }

    private void getCategories(String userId) {
        firebaseManager.getCategories(userId, new Callback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result) {
                categories = result;
                categoryAdapter.setCategories(categories);
                //dishAdapter.setCategories(categories);
                //getDishes();
            }

            @Override
            public void onError(String errorMessage) {
                // Handle error
            }
        });
    }

    private void getDishes() {
        firebaseManager.getDishes(currentUserId, new Callback<List<Dish>>() {
            @Override
            public void onSuccess(List<Dish> result) {
                dishes = result;
                //dishAdapter.setDishes(dishes);
                fillHorizontalScrollView();
            }

            @Override
            public void onError(String errorMessage) {
                // Handle error
            }
        });
    }

    private void fillHorizontalScrollView() {
        for (Dish dish : dishes) {
            CardView cardView = new CardView(getContext());
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.menu_card_item, cardView, false);

            cardView.addView(itemView);
            cardView.setClickable(true);

            TextView dishNameTextView = itemView.findViewById(R.id.dishTitleTxt);
            TextView dishPriceTextView = itemView.findViewById(R.id.dishPriceTxt);
            ImageView dishImage = itemView.findViewById(R.id.cardViewImg);
            TextView categoryTextView = itemView.findViewById(R.id.categoryTxt);

            Category category = findCategoryById(dish.getCategoryId());

            if (category != null) {
                categoryTextView.setText(category.getName());
            }

            dishNameTextView.setText(dish.getName());
            dishPriceTextView.setText(String.valueOf("$" + dish.getPrice()));
            String imageUrl = dish.getImageUrl();

            Glide.with(getContext())
                    .load(imageUrl)
                    .fitCenter()
                    .into(dishImage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "Clicked " + dish.getName(), Toast.LENGTH_SHORT).show();
                }
            });

            containerLayout.addView(cardView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}