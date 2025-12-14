package com.example.gamecatalog.Entites;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GameDetails {
    // Все поля, как в классе Game, но с аннотациями для надежности
    public int id;
    public String title;
    public String thumbnail;
    @SerializedName("description") // Используем аннотацию, если имена полей отличаются
    public String description; // Полное описание

    @SerializedName("game_url")
    public String gameUrl;
    public String genre;
    public String publisher;
    public String developer;
    @SerializedName("release_date")
    public String releaseDate;

    @SerializedName("minimum_system_requirements")
    public SystemRequirements systemRequirements; // Вложенный объект

    public List<Screenshot> screenshots; // Список скриншотов
}