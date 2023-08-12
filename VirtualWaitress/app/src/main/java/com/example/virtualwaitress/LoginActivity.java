package com.example.virtualwaitress;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.virtualwaitress.util.RestaurantUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    // Widgets
    private Button login;
    private EditText email;
    private EditText password;
    private EditText tableNumber;

    // Firebase Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Firebase Authentication
    private FirebaseAuth firebaseAuth;
    private CollectionReference collectionReference = db.collection("Users");
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = findViewById(R.id.loginBtn);
        //register = findViewById(R.id.registerBtn);
        email = findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);
        tableNumber = findViewById(R.id.etTableNumber);

        firebaseAuth = FirebaseAuth.getInstance();


        /*register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });*/

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginEmailPasswordUser(
                        email.getText().toString().trim(),
                        password.getText().toString().trim()
                );
            }
        });
    }

    private void LoginEmailPasswordUser(String email, String password) {
        // Checking for empty text
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            assert user != null;
                            final String currentUserId = user.getUid();
                            collectionReference.whereEqualTo("userId", currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            if (error != null) {

                                            }
                                            assert value != null;
                                            if (!value.isEmpty()) {
                                                for (QueryDocumentSnapshot snapshot : value) {
                                                    RestaurantUser restaurantUser = RestaurantUser.getInstance();
                                                    restaurantUser.setEmail(snapshot.getString("email"));
                                                    restaurantUser.setUserId(snapshot.getString("userId"));
                                                    saveTableNumber(Integer.parseInt(tableNumber.getText().toString().trim()));
                                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                }
                                            }
                                        }
                                    });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this, "Something went wrong" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(LoginActivity.this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveTableNumber(int tableNumber) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("tableNumber", tableNumber);
        editor.apply();
    }
}