package com.example.gamecatalog.UI;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gamecatalog.R;
import com.example.gamecatalog.Entites.Game;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private GameAdapter adapter;
    private ProgressBar progress;
    private TextView textEmpty;
    private ChipGroup chipGroupGenres;
    private GameListViewModel viewModel;

    private List<Game> fullListFromVm = new ArrayList<>();

    private String currentQuery = "";
    private String currentGenreFilter = null;
    enum SortMode {NONE, NAME_ASC, NAME_DESC, RELEASE_DATE_NEWEST, RELEASE_DATE_OLDEST, PUBLISHER, USER_CREATED}
    private SortMode sortMode = SortMode.NONE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setupRecyclerView();
        setupViewModel();
    }

    private void initViews() {
        progress = findViewById(R.id.progressBar);
        textEmpty = findViewById(R.id.textEmpty);
        recycler = findViewById(R.id.recyclerGames);
        chipGroupGenres = findViewById(R.id.chipGroupStyles);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViewById(R.id.GameBtNFavorites).setOnClickListener(v ->
                startActivity(new Intent(this, FavoritesActivity.class)));
        findViewById(R.id.GameBtNUsers).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserItemActivity.class);
            // Мы НЕ передаем сюда GAME_ID, поэтому UserItemActivity поймет,
            // что нужно работать в режиме создания новой записи.
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GameAdapter();
        recycler.setAdapter(adapter);

        adapter.setOnItemClickListener((game, imageView) -> {
            Intent intent;
            if (game.isUserCreated) {
                // Если игра создана пользователем — открываем экран редактирования
                intent = new Intent(this, UserItemActivity.class);
                intent.putExtra("GAME_ID", game.getId()); // Передаем ID для загрузки данных
            } else {
                // Если игра из API — открываем стандартное детальное окно
                intent = new Intent(this, DetailActivity.class);
                intent.putExtra("Game", game.getId());
            }
            startActivity(intent);
        });

        adapter.setOnFavoriteClickListener(game ->
                viewModel.setFavorite(game.getId(), !game.isFavorite));
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(GameListViewModel.class);
        progress.setVisibility(View.VISIBLE); // Показываем прогресс-бар в самом начале


        viewModel.getIsDataReady().observe(this, isReady -> {
            if (isReady) {
                // Любые последующие изменения (фильтры, добавление в избранное) будут обновляться как обычно.
                viewModel.getDisplayedGames().observe(MainActivity.this, games -> {
                    progress.setVisibility(View.GONE); // Прячем прогресс-бар
                    fullListFromVm.clear();
                    if (games != null) {
                        fullListFromVm.addAll(games);
                    }
                    applyAllLocalFilters(); // Применяем фильтры и отображаем список
                });

                // Также подписываемся на жанры
                viewModel.getUniqueGenres().observe(MainActivity.this, genres -> {
                    if (genres != null) {
                        buildGenreChips(genres);
                    }
                });
            }
        });
    }

    private void buildGenreChips(List<String> uniqueGenres) {
        chipGroupGenres.removeAllViews();
        Chip chipAll = createChip("Все");
        chipGroupGenres.addView(chipAll);
        chipAll.setOnClickListener(v -> {
            currentGenreFilter = null;
            updateChipSelection();
            applyAllLocalFilters();
        });

        for (String genre : uniqueGenres) {
            Chip chip = createChip(genre);
            chipGroupGenres.addView(chip);
            chip.setOnClickListener(v -> {
                currentGenreFilter = genre;
                updateChipSelection();
                applyAllLocalFilters();
            });
        }
        updateChipSelection();
    }

    private void updateChipSelection() {
        for (int i = 0; i < chipGroupGenres.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupGenres.getChildAt(i);
            String chipText = chip.getText().toString();
            if (currentGenreFilter == null && "Все".equals(chipText)) {
                chip.setChecked(true);
            } else {
                chip.setChecked(chipText.equals(currentGenreFilter));
            }
        }
    }

    private void applyAllLocalFilters() {
        // fullListFromVm уже приходит отсортированным из LiveData
        List<Game> filteredList = new ArrayList<>(fullListFromVm);

        //  Применяем фильтр по жанру (если он выбран)
        if (currentGenreFilter != null) {
            filteredList.removeIf(game -> !currentGenreFilter.equals(game.getGenre()));
        }

        //  Применяем фильтр по поисковому запросу (если он есть)
        if (!TextUtils.isEmpty(currentQuery)) {
            String lowerCaseQuery = currentQuery.toLowerCase().trim();
            filteredList.removeIf(game -> game.getTitle() == null || !game.getTitle().toLowerCase().contains(lowerCaseQuery));
        }

        // Отдаем в адаптер отфильтрованный (но уже ранее отсортированный) список
        adapter.setItems(filteredList);

        //  Проверяем, не пуст ли итоговый список, и показываем/скрываем заглушку
        if (filteredList.isEmpty()) {
            recycler.setVisibility(View.GONE);
            textEmpty.setVisibility(View.VISIBLE);
        } else {
            recycler.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.GONE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Поиск по названию");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                applyAllLocalFilters();
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort) {
            showSortDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        // Возвращаем опцию "Без сортировки"
        final String[] options = new String[]{
                "Без сортировки",
                "По названию (А-Я)",
                "По названию (Я-А)",
                "Сначала новые",
                "Сначала старые",
                "По издателю",
                "Мои игры (Пользовательские)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Сортировка")
                .setSingleChoiceItems(options, sortMode.ordinal(), (dialog, which) -> sortMode = SortMode.values()[which])
                .setPositiveButton("Применить", (dialog, which) -> {
                    viewModel.setSortMode(sortMode); // Сообщаем ViewModel о новом режиме
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private Chip createChip(String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setId(View.generateViewId());
        chip.setTextColor(getResources().getColor(R.color.chip_text_color, getTheme()));
        chip.setChipBackgroundColorResource(R.color.chip_background_state);
        chip.setChipStrokeWidth(0);
        chip.setCheckedIconVisible(false);
        return chip;
    }
}
