package com.example.gamecatalog.Retrofit;

import com.example.gamecatalog.Entites.Game;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ServiceApi {
    @GET("games")
    Call<List<Game>> getGameList();
}
