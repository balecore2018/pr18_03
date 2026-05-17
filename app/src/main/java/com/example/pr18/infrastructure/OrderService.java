package com.example.pr18.infrastructure;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.network.datas.orders.OrderGet;
import com.example.network.domains.callbacks.MyResponseCallback;
import com.example.network.domains.models.Order;
import com.example.pr18.R;
import com.example.pr18.domains.NotifyManager;
import com.example.pr18.presentations.BasketActivity;
import com.google.gson.GsonBuilder;

public class OrderService extends Service {

    private static final String TAG = "ORDER SERVICE";
    private static final int INTERVAL = 30 * 1000;

    private Integer id;
    private final Handler handler = new Handler();
    private NotifyManager notifyManager;

    private final Runnable checkStatusRunnable = new Runnable() {
        @Override
        public void run() {
            if (id == null || id <= 0) {
                Log.e(TAG, "Order id is empty");
                stopSelf();
                return;
            }

            OrderGet responseOrderGet = new OrderGet(
                    id,
                    BasketActivity.TOKEN,
                    new MyResponseCallback() {
                        @Override
                        public void onCompile(String result) {
                            Order orderData = new GsonBuilder().create().fromJson(result, Order.class);
                            if (orderData == null) {
                                handler.postDelayed(checkStatusRunnable, INTERVAL);
                                return;
                            }

                            if (orderData.status == 1) {
                                int allSum = 0;
                                int productsCount = orderData.products == null ? 0 : orderData.products.size();

                                for (int i = 0; i < productsCount; i++) {
                                    if (orderData.products.get(i).product != null) {
                                        allSum += orderData.products.get(i).product.price
                                                * orderData.products.get(i).count;
                                    }
                                }

                                notifyManager.SendNotify(getString(
                                        R.string.order_delivered_message,
                                        allSum,
                                        productsCount
                                ));
                                stopSelf();
                            } else {
                                handler.postDelayed(checkStatusRunnable, INTERVAL);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, error);
                            handler.postDelayed(checkStatusRunnable, INTERVAL);
                        }
                    }
            );
            responseOrderGet.execute();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        notifyManager = new NotifyManager(this);
        Log.d(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(checkStatusRunnable);

        if (intent != null && intent.hasExtra("id")) {
            id = intent.getIntExtra("id", -1);
        }

        if (id == null || id <= 0) {
            Log.e(TAG, "Order id is empty");
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        handler.post(checkStatusRunnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(checkStatusRunnable);
        Log.d(TAG, "Service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
