package com.example.network.datas.orders;

import com.example.network.domains.apis.MyAsyncTask;
import com.example.network.domains.callbacks.MyResponseCallback;
import com.example.network.domains.common.Settings;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.sql.Connection;

public class OrderCreate extends MyAsyncTask {

    String token;

    public OrderCreate(String token, MyResponseCallback callback) {

        super(callback);
        this.token = token;

    }

    @Override
    protected String doInBackground(Void... voids) {

        try {

            Connection.Response response = Jsoup.connect(Settings.URL + "api/order/create")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .method(Connection.Method.POST)
                    .header("Content-type", "application/json")
                    .header("token", token)
                    .execute();

            return response.statusCode() == 200 ? response.body() : "Error: " + response.body();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }

    }

}
