package com.example.gamecatalog.UI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.gamecatalog.Entites.Game;
import com.example.gamecatalog.R;

public class UserItemActivity extends AppCompatActivity {
    private String selectedImageUri = null;


    private GameListViewModel viewModel;
    private Game localGame; // Текущая игра (если редактируем)
    private boolean isEditMode = false;

    private ImageButton imageGameUsers, buttonFavoriteUsers, buttonDeleteUsers;
    private EditText textNameUsers, textGenreUsers, textDateUsers, textPublisherUsers, textDescriptionUsers, editCommentUsers;
    private RatingBar ratingBarUsers;
    private Button buttonSaveUsers;
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {// ПЫТАЕМСЯ ПОЛУЧИТЬ ПОСТОЯННЫЕ ПРАВА
                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    } catch (Exception e) {
                        // На некоторых устройствах/версиях может не сработать,
                        // просто игнорируем, временный доступ все равно будет
                        e.printStackTrace();
                    }

                    selectedImageUri = uri.toString();
                    // Отображаем выбранное фото сразу
                    Glide.with(this)
                            .load(uri)
                            .placeholder(R.drawable.icon_add_image)
                            .into(imageGameUsers);
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_user);

        viewModel = new ViewModelProvider(this).get(GameListViewModel.class);
        initViews();

        // Проверяем, передали ли нам ID (режим редактирования)
        int gameId = getIntent().getIntExtra("GAME_ID", -1);

        if (gameId != -1) {
            isEditMode = true;
            loadGameData(gameId);
            buttonDeleteUsers.setVisibility(View.VISIBLE); // Показываем удаление только при редактировании
        } else {
            isEditMode = false;
            buttonDeleteUsers.setVisibility(View.GONE); // При создании удалять нечего
        }

        setupClickListeners();
    }

    private void initViews() {
        imageGameUsers = findViewById(R.id.imageGameUsers);
        buttonFavoriteUsers = findViewById(R.id.buttonFavoriteUsers);
        buttonDeleteUsers = findViewById(R.id.buttonDeleteUsers);

        textNameUsers = findViewById(R.id.textNameUsers);
        textGenreUsers = findViewById(R.id.textGenreUsers);
        textDateUsers = findViewById(R.id.textDateUsers);
        textPublisherUsers = findViewById(R.id.textPublisherUsers);
        textDescriptionUsers = findViewById(R.id.textDescriptionUsers);
        editCommentUsers = findViewById(R.id.editCommentUsers);

        ratingBarUsers = findViewById(R.id.ratingBarUsers);
        buttonSaveUsers = findViewById(R.id.buttonSaveUsers);
    }

    private void loadGameData(int id) {
        viewModel.getGameById(id).observe(this, game -> {
            if (game != null) {
                localGame = game;
                populateUi(game);
            }
        });
    }

    private void populateUi(Game game) {
        textNameUsers.setText(game.title);
        textGenreUsers.setText(game.genre);
        textDateUsers.setText(game.release_date);
        textPublisherUsers.setText(game.publisher);
        textDescriptionUsers.setText(game.short_description);
        editCommentUsers.setText(game.comment);
        ratingBarUsers.setRating(game.rating);
        updateFavoriteIcon(game.isFavorite);
        if (game.thumbnail != null) {
            selectedImageUri = game.thumbnail;
            Glide.with(this)
                    .load(game.thumbnail)
                    .placeholder(R.drawable.icon_add_image)
                    .into(imageGameUsers);
        }
    }

    private void setupClickListeners() {
        // Кнопка сохранения (Создать или Обновить)
        buttonSaveUsers.setOnClickListener(v -> saveGame());

        // Выбор картинки
        imageGameUsers.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        // Кнопка удаления (только в режиме редактирования)
        buttonDeleteUsers.setOnClickListener(v -> {
            if (localGame != null) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Удаление игры")
                        .setMessage("Вы уверены, что хотите удалить эту игру из вашей библиотеки?")
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            viewModel.deleteGame(localGame);
                            Toast.makeText(this, "Игра удалена", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .setNegativeButton("Отмена", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        // Кнопка избранного (ЕДИНСТВЕННЫЙ И ПРАВИЛЬНЫЙ СЛУШАТЕЛЬ)
        buttonFavoriteUsers.setOnClickListener(v -> {
            // Если мы создаем игру, а объекта еще нет в памяти — создаем его
            if (localGame == null) {
                localGame = new Game();
                localGame.isUserCreated = true;
                // Сразу даем ID, чтобы метод setFavorite в БД не промахнулся
                localGame.id = (int) (System.currentTimeMillis() / 1000);
            }

            // Переключаем статус
            localGame.isFavorite = !localGame.isFavorite;
            updateFavoriteIcon(localGame.isFavorite);

            // Если мы редактируем уже существующую в базе игру — обновляем статус в БД мгновенно
            if (isEditMode) {
                viewModel.setFavorite(localGame.id, localGame.isFavorite);
            }
            // Если это создание новой, статус сохранится в методе saveGame()
            // вместе со всеми остальными полями.
        });
    }


    private void saveGame() {
        String title = textNameUsers.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Название обязательно!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEditMode && localGame == null) {
            localGame = new Game();
            localGame.id = (int) (System.currentTimeMillis() / 1000);
            localGame.isUserCreated = true;
        } else if (!isEditMode) {
            // Если объект уже был создан нажатием на звездочку,
            // просто убеждаемся, что ID и флаг на месте
            localGame.id = (int) (System.currentTimeMillis() / 1000);
            localGame.isUserCreated = true;
        }
        localGame.thumbnail = selectedImageUri;
        // Заполняем данными из полей
        localGame.title = title;
        localGame.genre = textGenreUsers.getText().toString();
        localGame.release_date = textDateUsers.getText().toString();
        localGame.publisher = textPublisherUsers.getText().toString();
        localGame.short_description = textDescriptionUsers.getText().toString();
        localGame.comment = editCommentUsers.getText().toString();
        localGame.rating = ratingBarUsers.getRating();

        // Ставим заглушку на картинку, если пусто
        if (localGame.thumbnail == null) {
            localGame.thumbnail = "android.resource://" + getPackageName() + "/" + R.drawable.icon_add_image;
        }

        if (isEditMode) {
            viewModel.updateGame(localGame);
            Toast.makeText(this, "Обновлено", Toast.LENGTH_SHORT).show();
        } else {
            viewModel.insertGame(localGame);
            Toast.makeText(this, "Сохранено в базу", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void updateFavoriteIcon(boolean isFav) {
        if (isFav) {
            buttonFavoriteUsers.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            buttonFavoriteUsers.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }
}
