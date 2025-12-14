package com.example.gamecatalog.database;

import android.util.Log;
import androidx.lifecycle.LiveData;
import com.example.gamecatalog.Entites.Game;
import com.example.gamecatalog.Retrofit.RetrofiClient;
import androidx.lifecycle.MediatorLiveData;
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
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(REPO_TAG, "Ошибка загрузки: " + response.code());
                    // Сигнализируем о завершении, даже если ошибка
                    if (onDataReady != null) {
                        onDataReady.run();
                    }
                    return;
                }

                // Новые данные с сервера
                List<Game> newListFromServer = response.body();

                new Thread(() -> {
                    // Получаем текущие данные из базы
                    List<Game> oldListFromDb = gameDao.getAllSync();

                    // Переносим локальные поля
                    for (Game newGame : newListFromServer) {
                        for (Game oldGame : oldListFromDb) {
                            if (newGame.id == oldGame.id) {
                                newGame.isFavorite = oldGame.isFavorite;
                                newGame.comment = oldGame.comment;
                                newGame.rating = oldGame.rating;
                                break;
                            }
                        }
                    }

                    // Вставляем "слитый" список.
                    gameDao.insertAll(newListFromServer);

                    //В самом конце фоновой задачи сигналим о готовности
                    if (onDataReady != null) {
                        onDataReady.run();
                    }
                }).start();
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                Log.e(REPO_TAG, "Сетевая ошибка: " + t.getMessage());
                // Сигналим о завершении при ошибке сети
                if (onDataReady != null) {
                    onDataReady.run();
                }
            }
        });
    }
    public LiveData<List<Game>> getAllSortedByDateDesc() { return gameDao.getAllSortedByDateDesc(); }
    public LiveData<List<Game>> getAllSortedByNameAsc() { return gameDao.getAllSortedByNameAsc(); }
    public LiveData<List<Game>> getAllSortedByNameDesc() { return gameDao.getAllSortedByNameDesc(); }
    public LiveData<List<Game>> getAllSortedByDateAsc() { return gameDao.getAllSortedByDateAsc(); }
    public LiveData<List<Game>> getAllSortedByPublisher() { return gameDao.getAllSortedByPublisher(); }

}
