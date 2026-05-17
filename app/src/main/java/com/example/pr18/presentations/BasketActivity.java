package com.example.pr18.presentations;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.network.datas.baskets.BasketDelete;
import com.example.network.datas.baskets.BasketUpdate;
import com.example.network.datas.orders.OrderCreate;
import com.example.network.domains.callbacks.MyResponseCallback;
import com.example.network.domains.models.BasketParams;
import com.example.network.domains.models.ProductBasket;
import com.example.pr18.R;
import com.example.pr18.domains.PermissionManager;
import com.example.pr18.infrastructure.OrderService;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class BasketActivity extends AppCompatActivity {

    public static String TOKEN = "test";
    ArrayList<ProductBasket> ProductsBasket = new ArrayList<>();

    LinearLayout llItems;
    TextView tvAllSum;
    View bthBasketDelete;
    View bthOrderCreate;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);
        llItems = findViewById(R.id.llItems);
        tvAllSum = findViewById(R.id.tvAllSum);
        bthBasketDelete = findViewById(R.id.bthBasketDelete);
        bthOrderCreate = findViewById(R.id.bthOrderCreate);
        context = this;
        PermissionManager.PermissionNotification(context, this);
        bthBasketDelete.setOnClickListener(v -> {
            BasketDelete RequestBasketDelete = new BasketDelete(
                    TOKEN,
                    new MyResponseCallback() {
                        @Override
                        public void onCompile(String result) {
                            Log.d("BASKET DELETE", result);
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("BASKET DELETE", error);
                        }
                    }
            );
            RequestBasketDelete.execute();
        });

        bthOrderCreate.setOnClickListener(v -> {
            OrderCreate RequestOrderCreate = new OrderCreate(
                    TOKEN,
                    new MyResponseCallback() {
                        @Override
                        public void onCompile(String result) {
                            Log.d("BASKET DELETE", result);
                            Order order = new GsonBuilder().create().fromJson(result, Order.class);
                            onBasketGet();
                            Toast.makeText(context,
                                    "Заказ успешно оформлен, при изменении статуса, Вам придет уведомление",
                                    Toast.LENGTH_SHORT).show();
                            Intent OrderService = new Intent(context, OrderService.class);
                            OrderService.putExtra("id", order.id);
                            startService(OrderService);
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("BASKET DELETE". error);
                        }
                    }
            );
            RequestOrderCreate.execute();
        });
    }

    public void onBasketUpdate(ProductBasket productBasket) {

        BasketParams Data = new BasketParams(productBasket.count, productBasket.product.id);
        BasketUpdate RequestBasketUpdate = new BasketUpdate(
                Data,
                TOKEN,
                new MyResponseCallback() {
                    @Override
                    public void onCompile(String result) {
                        Log.d("BASKET UPDATE", result);
                        onBasketGet();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("BASKET UPDATE", error);
                    }
                }
        );
        RequestBasketUpdate.execute();
    }


    public void CreateItemBasket() {

        llItems.removeAllViews();
        Integer AllSum = 0;
        for(int i = 0; i < ProductsBasket.size(); i++) {

            ProductBasket ProductBasket = ProductsBasket.get(i);
            View itemOrder = LayoutInflater.from(this).inflate(R.layout.item_basket,llItems,false);
            TextView tvName = itemOrder.findViewById(R.id.tvName);
            TextView tvPrice = itemOrder.findViewById(R.id.tvPrice);
            TextView tvCount = itemOrder.findViewById(R.id.tvCount);
            View bthMinus = itemOrder.findViewById(R.id.bthMinus);
            View bthPlus = itemOrder.findViewById(R.id.bthPlus);
            View bthItemClear = itemOrder.findViewById(R.id.bthItemClear);

            tvName.setText(ProductBasket.product.name);
            tvPrice.setText(ProductBasket.product.price + " Р");
            tvCount.setText(ProductBasket.count + " штук");
            bthMinus.setOnClickListener(v -> {
                ProductBasket.count--;
                onBasketUpdate(ProductBasket);
            });

            bthPlus.setOnClickListener(v -> {
                ProductBasket.count++;
                onBasketUpdate(ProductBasket);
            });

            bthItemClear.setOnClickListener(v -> {
                ProductBasket.count = 0;
                onBasketUpdate(ProductBasket);
            });

            AllSum += ProductBasket.product.price * ProductBasket.count;
            llItems.addView(itemOrder);

        }

        tvAllSum.setText(AllSum + " Р");

    }

}