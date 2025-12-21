package com.example.gamecatalog.database;

import android.util.Log;
import androidx.lifecycle.LiveData;
import com.example.gamecatalog.Entites.Game;
import com.example.gamecatalog.Retrofit.RetrofiClient;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameRepository {
    private final GameDao gameDao;
    private static final String REPO_TAG = "GameRepository";

    public GameRepository(GameDao gameDao) {
        this.gameDao = gameDao;
    }

    public LiveData<List<Game>> getAll() {
        return gameDao.getAll();
    }
    public LiveData<Game> getGameById(int id) {
        return gameDao.getGameById(id);
    }
    public LiveData<List<Game>> getFavorites() {
        return gameDao.getFavorites();
    }
    public LiveData<List<Game>> search(String query) {
        return gameDao.searchByTitle(query);
    }
    public LiveData<List<String>> getUniqueGenres() {
        return gameDao.getUniqueGenres();
    }


    // --- ПРОСТЫЕ МЕТОДЫ ЗАПИСИ ---
    public void setFavorite(int gameId, boolean isFavorite) {
        new Thread(() -> gameDao.setFavorite(gameId, isFavorite)).start();
    }
    public void updateGame(Game game) {
        // Запускаем обновление в фоновом потоке
        new Thread(() -> {
            gameDao.update(game);
        }).start();
    }

    public void refreshData(Runnable onDataReady) {
        RetrofiClient.api().getGameList().enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Game> newListFromServer = response.body();

                    new Thread(() -> {
                        // 1. Получаем ВСЕ игры из базы (и API-шные, и пользовательские)
                        List<Game> allOldGames = gameDao.getAllSync();

                        for (Game newGame : newListFromServer) {
                            // По умолчанию считаем, что это игра из API
                            newGame.isUserCreated = false;

                            for (Game oldGame : allOldGames) {
                                if (newGame.id == oldGame.id) {
                                    // 2. ПРОВЕРКА: Если в базе под этим ID лежит пользовательская игра
                                    if (oldGame.isUserCreated) {
                                        // ОЙ! Конфликт ID.
                                        // Чтобы не потерять игру пользователя, меняем ID у игры из API
                                        // (или наоборот, но проще проигнорировать замену)
                                        // В данном случае мы можем просто пропустить обновление этой игры из API
                                        // или задать ей временный отрицательный ID, чтобы не было REPLACE
                                        Log.w(REPO_TAG, "Конфликт ID! Игра из API " + newGame.id + " совпала с пользовательской.");
                                    } else {
                                        // Это обычная игра из API, переносим локальные поля
                                        newGame.isFavorite = oldGame.isFavorite;
                                        newGame.comment = oldGame.comment;
                                        newGame.rating = oldGame.rating;
                                    }
                                    break;
                                }
                            }
                        }

                        // 3. Вставляем.
                        // ВАЖНО: insertAll заменит старые игры API новыми,
                        // но НЕ ТРОНЕТ пользовательские игры, у которых ID другие.
                        gameDao.insertAll(newListFromServer);

                        if (onDataReady != null) onDataReady.run();
                    }).start();
                }
            }
            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                if (onDataReady != null) onDataReady.run();
            }
        });
    }
    public void insertGame(Game game) {
        new Thread(() -> {
            gameDao.insert(game);
        }).start();}

    // Удалить игру из базы
    public void deleteGame(Game game) {
        new Thread(() -> {
            gameDao.delete(game);
        }).start();
    }
    public LiveData<List<Game>> getUserCreatedGames() {
        return gameDao.getUserCreatedGames();
    }
    public LiveData<List<Game>> getAllSortedByDateDesc() { return gameDao.getAllSortedByDateDesc(); }
    public LiveData<List<Game>> getAllSortedByNameAsc() { return gameDao.getAllSortedByNameAsc(); }
    public LiveData<List<Game>> getAllSortedByNameDesc() { return gameDao.getAllSortedByNameDesc(); }
    public LiveData<List<Game>> getAllSortedByDateAsc() { return gameDao.getAllSortedByDateAsc(); }
    public LiveData<List<Game>> getAllSortedByPublisher() { return gameDao.getAllSortedByPublisher(); }

}
