package com.example.gamecatalog.Entites;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "games")
public class Game {
    @PrimaryKey(autoGenerate = true)
   public int id;
    public  String title;
    public String thumbnail;
    public String short_description;
    @SerializedName("genre")
    public String genre;
    public String publisher;
    public boolean isUserCreated = false;
    public String release_date;
    // Локальные данные пользователя
    @ColumnInfo(defaultValue = "0")
    public boolean isFavorite = false;

    @ColumnInfo(defaultValue = "")
    public String comment = "";

    @ColumnInfo(defaultValue = "0")
    public float rating = 0;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getThumbnail() {
        return thumbnail;
    }



    public float getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getGenre() {
        return genre;
    }

    public String getPublisher() {
        return publisher;
    }


    public String getRelease_date() {
        return release_date;
    }

    public String getShortDescription() {
        return short_description;
    }
}

