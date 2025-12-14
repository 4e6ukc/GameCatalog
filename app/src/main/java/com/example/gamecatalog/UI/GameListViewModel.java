// Файл GameListViewModel.java
package com.example.gamecatalog.UI;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData; // Важный импорт
import androidx.lifecycle.MutableLiveData;
import com.example.gamecatalog.Entites.Game;
import com.example.gamecatalog.database.AppDatabase;
import com.example.gamecatalog.database.GameRepository;
import java.util.List;

public class GameListViewModel extends AndroidViewModel {
    private final GameRepository repository;
    private final MutableLiveData<Boolean> isDataReady = new MutableLiveData<>(false);

    private final MediatorLiveData<List<Game>> displayedGames = new MediatorLiveData<>();
    private LiveData<List<Game>> currentSource = null;

    public GameListViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        repository = new GameRepository(db.GameDao());

        repository.refreshData(() -> isDataReady.postValue(true));

        // Устанавливаем источник по умолчанию
        setSortMode(MainActivity.SortMode.NONE);
    }

    // Метод для смены сортировки
    public void setSortMode(MainActivity.SortMode sortMode) {
        if (currentSource != null) {
            displayedGames.removeSource(currentSource); // Отписываемся от старого
        }

        switch (sortMode) {
            case RELEASE_DATE_NEWEST: currentSource = repository.getAllSortedByDateDesc(); break;
            case NAME_ASC: currentSource = repository.getAllSortedByNameAsc(); break;
            case NAME_DESC: currentSource = repository.getAllSortedByNameDesc(); break;
            case RELEASE_DATE_OLDEST: currentSource = repository.getAllSortedByDateAsc(); break;
            case PUBLISHER: currentSource = repository.getAllSortedByPublisher(); break;
            case NONE: // <-- Добавляем обработку
            default:   // <-- И делаем ее поведением по умолчанию
                currentSource = repository.getAll(); // Используем оригинальный метод
                break;
        }

        // Подписываемся на новый источник
        displayedGames.addSource(currentSource, games -> displayedGames.setValue(games));
    }


    // --- Остальные методы ---
    public LiveData<Boolean> getIsDataReady() { return isDataReady; }
    public LiveData<List<Game>> getDisplayedGames() { return displayedGames; } // Теперь отдаем Mediator
    public void setFavorite(int id, boolean fav) { repository.setFavorite(id, fav); }
    public LiveData<Game> getGameById(int id) { return repository.getGameById(id); }
    public LiveData<List<Game>> getFavorites() { return repository.getFavorites(); }
    public LiveData<List<String>> getUniqueGenres() { return repository.getUniqueGenres(); }


    public void updateGame(Game game) {
        repository.updateGame(game);
    }
}
