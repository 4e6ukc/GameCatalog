package com.example.gamecatalog.UI;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gamecatalog.Entites.Game;
import com.example.gamecatalog.database.AppDatabase;
import com.example.gamecatalog.database.GameRepository;
import java.util.List;

public class GameListViewModel extends AndroidViewModel {
    private final GameRepository repository;
    private final MutableLiveData<Boolean> isDataReady = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsDataReady() {
        return isDataReady;
    }
    public GameListViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        repository = new GameRepository(db.GameDao());

        // При первом запуске обновляем кэш игр и выставляем флаг готовности, когда все закончится
        repository.refreshData(() -> {
            // Этот код выполнится, когда refreshData завершит свою работу
            isDataReady.postValue(true);
        });
    }

    // Методы просто пробрасывают вызовы в репозиторий
    public LiveData<List<Game>> getDisplayedGames() {
        return repository.getAll();
    }


    // Остальные методы без изменений
    public void setFavorite(int id, boolean fav) { repository.setFavorite(id, fav); }
    public void updateGame(Game game) { repository.updateGame(game); }
    public LiveData<Game> getGameById(int id) { return repository.getGameById(id); }
    public LiveData<List<Game>> getFavorites() { return repository.getFavorites(); }
    public LiveData<List<Game>> search(String query) { return repository.search(query); }


    public LiveData<List<String>> getUniqueGenres() {
        return repository.getUniqueGenres();
    }
}
