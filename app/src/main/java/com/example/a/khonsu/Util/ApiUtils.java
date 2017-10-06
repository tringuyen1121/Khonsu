package com.example.a.khonsu.util;

import com.example.a.khonsu.ServerAPI;

class ApiUtils {

    private static final String BASE_URL = "http://users.metropolia.fi";

    static ServerAPI getServerAPI() {
        return RetrofitClient.getClient(BASE_URL).create(ServerAPI.class);
    }
}
