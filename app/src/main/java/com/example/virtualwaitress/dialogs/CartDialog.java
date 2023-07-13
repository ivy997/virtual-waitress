package com.example.virtualwaitress.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.virtualwaitress.R;
import com.example.virtualwaitress.models.Dish;

public class CartDialog extends Dialog {
    private Button btnMinus;
    private TextView tvCount;
    private Button btnPlus;
    private Button addToCart;
    private TextView title;
    private TextView price;
    private TextView desc;

    private Dish dish;

    private int count = 1;

    public CartDialog(@NonNull Context context, Dish dish) {
        super(context);
        this.dish = dish;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.dialog_add_to_cart);

        // Get the dialog window
        Window dialogWindow = this.getWindow();
        if (dialogWindow != null) {
            // Set the width and height to match the layout's size
            dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        title = findViewById(R.id.titleDialogTV);
        price = findViewById(R.id.priceDialogTV);
        desc = findViewById(R.id.descTV);
        btnMinus = findViewById(R.id.btnMinus);
        tvCount = findViewById(R.id.tvCount);
        btnPlus = findViewById(R.id.btnPlus);
        addToCart = findViewById(R.id.addToCartButton);

        float dishPrice = dish.getPrice();
        title.setText(dish.getName());
        price.setText(String.format("%.2f лв.", dishPrice));
        desc.setText(dish.getDescription());
        addToCart.setText(String.format("%.2f лв.", dishPrice));

        updateCount();

        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count > 1) {
                    count--;
                    updateCount();
                    updateAddToCartBtnTxt(dishPrice);
                }
            }
        });

        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                updateCount();
                updateAddToCartBtnTxt(dishPrice);
            }
        });
    }

    private void updateCount() {
        tvCount.setText(String.valueOf(count));
    }

    public int getCount() {
        return count;
    }

    private void updateAddToCartBtnTxt (float price) {
        float newPrice = price * count;
        addToCart.setText(String.format("%.2f лв.", newPrice));
    }
}
