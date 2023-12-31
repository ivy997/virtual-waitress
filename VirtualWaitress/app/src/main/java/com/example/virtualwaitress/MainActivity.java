package com.example.virtualwaitress;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import com.example.virtualwaitress.adapters.CategoryAdapter;
import com.example.virtualwaitress.adapters.DishAdapter;
import com.example.virtualwaitress.databinding.FragmentHomeBinding;
import com.example.virtualwaitress.models.Category;
import com.example.virtualwaitress.models.Dish;
import com.example.virtualwaitress.util.Callback;
import com.example.virtualwaitress.util.FirebaseManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.virtualwaitress.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_cart, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    // Method to refresh the activity
    public void refreshActivity() {
        recreate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.burger_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // Sign out the current user
        firebaseAuth.signOut();

        // Redirect the user to the login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("logout", "logout");
        startActivity(intent);

        // Finish the current activity to prevent the user from going back to it after logout
        finish();
    }
}