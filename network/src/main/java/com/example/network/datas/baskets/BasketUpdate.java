package com.example.network.datas.baskets;

import com.example.network.domains.apis.MyAsyncTask;
import com.example.network.domains.callbacks.MyResponseCallback;
import com.example.network.domains.common.Settings;
import com.example.network.domains.models.BasketParams;
import com.google.gson.GsonBuilder;

import org.jsoup.Jsoup;

import java.io.IOException;

import org.jsoup.Connection;

public class BasketUpdate extends MyAsyncTask {

    String token;
    BasketParams basketRequest;


    public BasketUpdate(BasketParams basketRequest, String token, MyResponseCallback callback) {

        super(callback);
        this.token = token;
        this.basketRequest = basketRequest;

    }

    @Override
    protected String doInBackground(Void... voids) {

        String rawData = new GsonBuilder().create().toJson(basketRequest);

        try {

            Connection.Response response = Jsoup.connect(Settings.URL + "/api/basket/update")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .method(Connection.Method.PUT)
                    .header("Content-type", "application/json")
                    .header("token", token)
                    .requestBody(rawData)
                    .execute();

            return response.statusCode() == 200 ? response.body() : "Error: " + response.body();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }

    }

}
