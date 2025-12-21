package com.example.gamecatalog.UI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.bumptech.glide.Glide;
import com.example.gamecatalog.R;
import com.example.gamecatalog.Entites.Game;
import com.example.gamecatalog.Entites.GameDetails;
import com.example.gamecatalog.Retrofit.RetrofiClient;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    // Локальная сущность для сохранения рейтинга, комментов и избранного
    private Game localGameData;
    private GameListViewModel viewModel;
    private ProgressBar progressBar;
    private View mainContent;
    private int gameId;
    private ViewPager2 galleryViewPager;
    private TabLayout tabIndicator;
    private GalleryAdapter galleryAdapter;

    private TextView textName, textGenreDate, textPublisher, textDeveloper, textUrlGame, textDescription, textRequirements;
    private RatingBar ratingBar;
    private EditText editComment;
    private Button buttonSave;
    private ImageButton buttonFavorite;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        gameId = getIntent().getIntExtra("Game", -1);
        if (gameId == -1) {
            Toast.makeText(this, "Ошибка: ID игры не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Инициализируем ViewModel
        viewModel = new ViewModelProvider(this).get(GameListViewModel.class);

        // Инициализируем все View-элементы
        initViews();

        // Настраиваем галерею
        setupGallery();

        // Устанавливаем слушатели на кнопки
        setupClickListeners();

        // Загружаем данные из сети
        fetchGameDetails(gameId);

        // Параллельно загружаем локальные данные (рейтинг, избранное)
        loadLocalGameData(gameId);
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBarDetail);
        mainContent = findViewById(R.id.mainContent);
        galleryViewPager = findViewById(R.id.galleryViewPager);

        tabIndicator = findViewById(R.id.tabIndicator);
        textName = findViewById(R.id.textName);
        textGenreDate = findViewById(R.id.textGenreDate);
        textPublisher = findViewById(R.id.textPublisher);
        textDeveloper = findViewById(R.id.textDeveloper);
        textUrlGame = findViewById(R.id.textUrlGame);
        textDescription = findViewById(R.id.textDescription);
        textRequirements = findViewById(R.id.textRequirements);
        ratingBar = findViewById(R.id.ratingBar);
        editComment = findViewById(R.id.editComment);
        buttonFavorite = findViewById(R.id.buttonFavorite);
        buttonSave = findViewById(R.id.buttonSave);

    }

    // Загрузка данных с сервера
    private void fetchGameDetails(int gameId) {
        // Перед запросом: показываем прогресс, скрываем контент
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (mainContent != null) mainContent.setVisibility(View.GONE);

        RetrofiClient.api().getGame(gameId).enqueue(new Callback<GameDetails>() {
            @Override
            public void onResponse(Call<GameDetails> call, Response<GameDetails> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateUiWithDetails(response.body());

                    // ДАННЫЕ ПРИШЛИ: Прячем прогресс, показываем контент
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (mainContent != null) mainContent.setVisibility(View.VISIBLE);

                } else {
                    // Если 404 или ошибка сервера
                    Toast.makeText(DetailActivity.this, "Ошибка сервера: " + response.code(), Toast.LENGTH_LONG).show();
                    // progressBar остается VISIBLE, mainContent остается GONE
                }
            }

            @Override
            public void onFailure(Call<GameDetails> call, Throwable t) {
                Log.e("DetailActivity", "Ошибка сети", t);
                Toast.makeText(DetailActivity.this, "Проверьте подключение к сети", Toast.LENGTH_LONG).show();
                // progressBar остается VISIBLE
            }
        });
    }

    // Загрузка локальных данных (рейтинг, избранное)
    private void loadLocalGameData(int gameId) {
        viewModel.getGameById(gameId).observe(this, gameFromDb -> {
            // Если игра найдена в базе, используем ее
            if (gameFromDb != null) {
                localGameData = gameFromDb;
            } else {
                // ЕСЛИ ИГРЫ НЕТ В БАЗЕ, СОЗДАЕМ ПУСТОЙ ОБЪЕКТ!
                // Это гарантирует, что localGameData никогда не будет null.
                localGameData = new Game();
                localGameData.id = gameId;
                localGameData.isFavorite = false;
                localGameData.rating = 0;
                localGameData.comment = "";
            }
            // Обновляем UI локальными данными
            populateUiWithLocalData();
        });
    }

    // Заполняет View данными из сети
    private void populateUiWithDetails(GameDetails details) {

        textName.setText(details.title);
        textGenreDate.setText(String.format("%s • %s", details.genre, details.releaseDate));
        textPublisher.setText("Издатель: " + details.publisher);
        textDeveloper.setText("Разработчик: " + details.developer);
        textUrlGame.setText("Сайт игры: " + details.gameUrl);
        textDescription.setText(details.description);

        // Заполняем системные требования
        if (details.systemRequirements != null) {
            String reqs = "Минимальные системные требования:\n" +
                    "ОС: " + details.systemRequirements.os + "\n" +
                    "Процессор: " + details.systemRequirements.processor + "\n" +
                    "Память: " + details.systemRequirements.memory + "\n" +
                    "Графика: " + details.systemRequirements.graphics + "\n" +
                    "Место на диске: " + details.systemRequirements.storage;
            textRequirements.setText(reqs);
        } else {
            textRequirements.setText("Минимальные системные требования: не указаны");
        }

        // --- ЗАПОЛНЯЕМ ГАЛЕРЕЮ ---
        List<String> imageUrls = new ArrayList<>();

        // Первым делом добавляем главную обложку
        if (details.thumbnail != null) {
            imageUrls.add(details.thumbnail);
        }

        // Затем добавляем все скриншоты
        if (details.screenshots != null) {
            for (com.example.gamecatalog.Entites.Screenshot s : details.screenshots) {
                imageUrls.add(s.image);
            }
        }

        // Передаем список URL в адаптер
        galleryAdapter.setItems(imageUrls);

        // Показываем или скрываем точки-индикаторы в зависимости от количества картинок
        tabIndicator.setVisibility(imageUrls.size() > 1 ? View.VISIBLE : View.GONE);
    }

    // Заполняет View локальными данными
    private void populateUiWithLocalData() {
        if (localGameData == null) return;
        ratingBar.setRating(localGameData.getRating());
        editComment.setText(localGameData.getComment());
        updateFavButton();
    }


    // Устанавливает слушатели на кнопки
    private void setupClickListeners() {
        buttonFavorite.setOnClickListener(v -> {
            // Проверяем, что localGameData уже инициализирован
            if (localGameData != null) {
                localGameData.isFavorite = !localGameData.isFavorite;
                viewModel.setFavorite(localGameData.getId(), localGameData.isFavorite);
                // UI обновится автоматически через LiveData, но для мгновенной реакции дублируем
                updateFavButton();
            } else {
                Toast.makeText(this, "Данные еще загружаются...", Toast.LENGTH_SHORT).show();
            }
        });

        buttonSave.setOnClickListener(v -> {
            if (localGameData != null) {
                localGameData.rating = ratingBar.getRating();
                localGameData.comment = editComment.getText().toString();
                viewModel.updateGame(localGameData); // Просто обновляем игру в базе
                Toast.makeText(this, "Сохранено!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // Обновляет вид кнопки "Избранное"
    private void updateFavButton() {
        if (localGameData != null) {
            if (localGameData.isFavorite) {
                buttonFavorite.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                buttonFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            }
        }
    }
    private void setupGallery() {
        galleryAdapter = new GalleryAdapter();
        galleryViewPager.setAdapter(galleryAdapter);        // Связываем ViewPager2 с TabLayout для отображения точек-индикаторов
        new com.google.android.material.tabs.TabLayoutMediator(tabIndicator, galleryViewPager,
                (tab, position) -> {

                }
        ).attach();
    }
}
