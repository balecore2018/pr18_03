package com.example.network.datas.users;

import com.example.network.domains.apis.MyAsyncTask;
import com.example.network.domains.callbacks.MyResponseCallback;
import com.example.network.domains.common.Settings;
import com.example.network.domains.models.User;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import com.google.gson.GsonBuilder;

public class UserSend extends MyAsyncTask {
    String mail;

    public UserSend(String mail, MyResponseCallback callback) {
        super(callback);
        this.mail = mail;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            Connection.Response response = Jsoup.connect(Settings.URL + "/api/user/send?email=" + mail)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .method(Connection.Method.GET)
                    .execute();
            return response.statusCode() == 200 ?
                    response.body() :
                    "Error: " + response.body();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
}
