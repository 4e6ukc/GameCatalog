package com.example.gamecatalog.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gamecatalog.Entites.Game;

import java.util.List;
@Dao
public interface GameDao {
    // Получить все игры (для главного экрана)
    @Query("SELECT * FROM games")
    LiveData<List<Game>> getAll();

    // Синхронно получить все игры (для "умного" обновления в репозитории)
    @Query("SELECT * FROM games")
    List<Game> getAllSync();

    // Получить только избранные игры (для экрана FavoritesActivity)
    @Query("SELECT * FROM games WHERE isFavorite = 1")
    LiveData<List<Game>> getFavorites();

    // Получить одну игру по ID (для детального экрана)
    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    LiveData<Game> getGameById(int id);

    // Поиск по названию
    @Query("SELECT * FROM games WHERE title LIKE '%' || :query || '%'")
    LiveData<List<Game>> searchByTitle(String query);

    // Получить список уникальных жанров для фильтров
    @Query("SELECT DISTINCT TRIM(genre) FROM games WHERE genre IS NOT NULL AND genre != '' ORDER BY TRIM(genre) ASC")
    LiveData<List<String>> getUniqueGenres();

    // Вставить список игр, заменяя существующие (для "умного" обновления)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Game> games);

    // Обновить одну запись (когда пользователь сохраняет рейтинг/комментарий)
    @Update
    void update(Game game);

    // Просто и быстро изменить статус "избранное" по ID
    @Query("UPDATE games SET isFavorite = :isFav WHERE id = :gameId")
    void setFavorite(int gameId, boolean isFav);

}

