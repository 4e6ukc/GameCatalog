package com.example.gamecatalog.Retrofit;

import com.example.gamecatalog.Entites.Game;
import com.example.gamecatalog.Entites.GameDetails;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ServiceApi {
    @GET("games")
    Call<List<Game>> getGameList();
    @GET("game")
    Call<GameDetails> getGame(@Query("id") int id);
}
