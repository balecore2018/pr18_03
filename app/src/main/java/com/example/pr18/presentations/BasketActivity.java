package com.example.pr18.presentations;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.network.datas.baskets.BasketDelete;
import com.example.network.datas.baskets.BasketGet;
import com.example.network.datas.baskets.BasketUpdate;
import com.example.network.datas.orders.OrderCreate;
import com.example.network.domains.callbacks.MyResponseCallback;
import com.example.network.domains.common.Settings;
import com.example.network.domains.models.BasketParams;
import com.example.network.domains.models.Order;
import com.example.network.domains.models.ProductBasket;
import com.example.pr18.R;
import com.example.pr18.domains.PermissionManager;
import com.example.pr18.infrastructure.OrderService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;

public class BasketActivity extends AppCompatActivity {

    public static String TOKEN = Settings.DEMO_TOKEN;
    private static final String TAG = "BASKET";
    private static final int BASKET_UPDATE_INTERVAL = 30 * 1000;

    private final ArrayList<ProductBasket> productsBasket = new ArrayList<>();
    private final Gson gson = new GsonBuilder().create();
    private final Handler basketUpdateHandler = new Handler(Looper.getMainLooper());
    private final Runnable basketUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            onBasketGet();
            basketUpdateHandler.postDelayed(this, BASKET_UPDATE_INTERVAL);
        }
    };

    private LinearLayout llItems;
    private TextView tvAllSum;
    private View bthBasketDelete;
    private View bthOrderCreate;
    private Context context;
    private boolean isBasketLoading = false;

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
            BasketDelete requestBasketDelete = new BasketDelete(
                    TOKEN,
                    new MyResponseCallback() {
                        @Override
                        public void onCompile(String result) {
                            Log.d("BASKET DELETE", result);
                            onBasketGet();
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("BASKET DELETE", error);
                        }
                    }
            );
            requestBasketDelete.execute();
        });

        bthOrderCreate.setOnClickListener(v -> {
            OrderCreate requestOrderCreate = new OrderCreate(
                    TOKEN,
                    new MyResponseCallback() {
                        @Override
                        public void onCompile(String result) {
                            Log.d("ORDER CREATE", result);
                            Order order = gson.fromJson(result, Order.class);
                            onBasketGet();
                            Toast.makeText(
                                    context,
                                    getString(R.string.order_created_message),
                                    Toast.LENGTH_SHORT
                            ).show();

                            if (order != null && order.id > 0) {
                                Intent orderServiceIntent = new Intent(context, OrderService.class);
                                orderServiceIntent.putExtra("id", order.id);
                                startService(orderServiceIntent);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("ORDER CREATE", error);
                        }
                    }
            );
            requestOrderCreate.execute();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBasketAutoUpdate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBasketAutoUpdate();
    }

    private void startBasketAutoUpdate() {
        stopBasketAutoUpdate();
        basketUpdateRunnable.run();
    }

    private void stopBasketAutoUpdate() {
        basketUpdateHandler.removeCallbacks(basketUpdateRunnable);
    }

    public void onBasketGet() {
        if (isBasketLoading) {
            return;
        }

        isBasketLoading = true;
        BasketGet requestBasketGet = new BasketGet(
                TOKEN,
                new MyResponseCallback() {
                    @Override
                    public void onCompile(String result) {
                        isBasketLoading = false;
                        Log.d("BASKET GET", result);
                        productsBasket.clear();

                        String data = result == null ? "" : result.trim();
                        if (data.startsWith("[")) {
                            ProductBasket[] items = gson.fromJson(data, ProductBasket[].class);
                            if (items != null) {
                                productsBasket.addAll(Arrays.asList(items));
                            }
                        } else if (data.startsWith("{")) {
                            Order basket = gson.fromJson(data, Order.class);
                            if (basket != null && basket.products != null) {
                                productsBasket.addAll(basket.products);
                            }
                        }

                        createItemBasket();
                    }

                    @Override
                    public void onError(String error) {
                        isBasketLoading = false;
                        Log.e("BASKET GET", error);
                    }
                }
        );
        requestBasketGet.execute();
    }

    public void onBasketUpdate(ProductBasket productBasket) {
        if (productBasket == null || productBasket.product == null) {
            Log.e(TAG, "Product basket item is empty");
            return;
        }

        BasketParams data = new BasketParams(productBasket.count, productBasket.product.id);
        BasketUpdate requestBasketUpdate = new BasketUpdate(
                data,
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
        requestBasketUpdate.execute();
    }

    private void createItemBasket() {
        llItems.removeAllViews();
        int allSum = 0;

        for (ProductBasket productBasket : productsBasket) {
            View itemOrder = LayoutInflater.from(this).inflate(R.layout.item_basket, llItems, false);
            TextView tvName = itemOrder.findViewById(R.id.tvName);
            TextView tvPrice = itemOrder.findViewById(R.id.tvPrice);
            TextView tvCount = itemOrder.findViewById(R.id.tvCount);
            View bthMinus = itemOrder.findViewById(R.id.bthMinus);
            View bthPlus = itemOrder.findViewById(R.id.bthPlus);
            View bthItemClear = itemOrder.findViewById(R.id.bthItemClear);

            String name = productBasket.product == null ? "" : productBasket.product.name;
            int price = productBasket.product == null ? 0 : productBasket.product.price;

            tvName.setText(name);
            tvPrice.setText(getString(R.string.basket_price_format, price));
            tvCount.setText(getString(R.string.basket_count_format, productBasket.count));

            bthMinus.setOnClickListener(v -> {
                if (productBasket.count > 0) {
                    productBasket.count--;
                    onBasketUpdate(productBasket);
                }
            });

            bthPlus.setOnClickListener(v -> {
                productBasket.count++;
                onBasketUpdate(productBasket);
            });

            bthItemClear.setOnClickListener(v -> {
                productBasket.count = 0;
                onBasketUpdate(productBasket);
            });

            allSum += price * productBasket.count;
            llItems.addView(itemOrder);
        }

        tvAllSum.setText(getString(R.string.basket_total_format, allSum));
    }
}
