package com.example.network.datas.baskets;

import com.example.network.domains.apis.MyAsyncTask;
import com.example.network.domains.callbacks.MyResponseCallback;
import com.example.network.domains.common.Settings;

import org.jsoup.Jsoup;

import java.io.IOException;

import org.jsoup.Connection;

public class BasketGet extends MyAsyncTask {

    String token;

    public BasketGet(String token, MyResponseCallback callback) {

        super(callback);
        this.token = token;

    }

    @Override
    protected String doInBackground(Void... voids) {

        try {

            Connection.Response response = Jsoup.connect(Settings.URL + "/api/basket/get")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .method(Connection.Method.GET)
                    .header("Content-type", "application/json")
                    .header("token", token)
                    .execute();

            return response.statusCode() == 200 ? response.body() : "Error: " + response.body();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }

    }

}
