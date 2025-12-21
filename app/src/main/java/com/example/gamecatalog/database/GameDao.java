package com.example.gamecatalog.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
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

    // Новый метод по умолчанию (сортировка по дате релиза)
    @Query("SELECT * FROM games ORDER BY release_date DESC")
    LiveData<List<Game>> getAllSortedByDateDesc();

    // Сортировка по названию (А-Я)
    @Query("SELECT * FROM games ORDER BY title ASC")
    LiveData<List<Game>> getAllSortedByNameAsc();

    // Сортировка по названию (Я-А)
    @Query("SELECT * FROM games ORDER BY title DESC")
    LiveData<List<Game>> getAllSortedByNameDesc();

    // Сортировка по дате релиза (старые)
    @Query("SELECT * FROM games ORDER BY release_date ASC")
    LiveData<List<Game>> getAllSortedByDateAsc();

    // Сортировка по издателю
    @Query("SELECT * FROM games ORDER BY publisher ASC")
    LiveData<List<Game>> getAllSortedByPublisher();

    // Получить список уникальных жанров для фильтров
    @Query("SELECT DISTINCT TRIM(genre) FROM games WHERE genre IS NOT NULL AND genre != '' ORDER BY TRIM(genre) ASC")
    LiveData<List<String>> getUniqueGenres();

    // Вставить список игр, заменяя существующие (для "умного" обновления)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Game> games);

    @Query("SELECT * FROM games WHERE isUserCreated = 1 ORDER BY id DESC")
    LiveData<List<Game>> getUserCreatedGames();
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Game game);
    // Обновить одну запись (когда пользователь сохраняет рейтинг/комментарий)
    @Update
    void update(Game game);
    @Delete
    void delete(Game game);

    // Просто и быстро изменить статус "избранное" по ID
    @Query("UPDATE games SET isFavorite = :isFav WHERE id = :gameId")
    void setFavorite(int gameId, boolean isFav);

}

