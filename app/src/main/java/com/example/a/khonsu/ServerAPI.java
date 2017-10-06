package com.example.a.khonsu;

import com.example.a.khonsu.model.ServerObject;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ServerAPI {

    @GET("/~tring/sample/khonsu.json")
    Call<ServerObject> getAnswer();
}
