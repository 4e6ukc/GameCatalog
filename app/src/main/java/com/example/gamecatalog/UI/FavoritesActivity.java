package  com.example.gamecatalog.UI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.example.gamecatalog.R;

public class FavoritesActivity extends AppCompatActivity {

    private GameAdapter adapter;
    private GameListViewModel viewModel;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        RecyclerView recycler = findViewById(R.id.recyclerFavorites);
        progress = findViewById(R.id.progressFavorites);

        adapter = new GameAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(GameListViewModel.class);


        //  Перед запросом данных показываем индикатор загрузки.
        progress.setVisibility(View.VISIBLE);
        recycler.setVisibility(View.INVISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            viewModel.getFavorites().observe(this, games -> {
                // Этот код выполнится, как только LiveData отдаст данные.

                // Первым делом прячем индикатор загрузки.
                progress.setVisibility(View.GONE);
                // И делаем RecyclerView снова видимым.
                recycler.setVisibility(View.VISIBLE);

                // Обновляем адаптер.
                if (games != null) {
                    adapter.setItems(games);
                }

            });
        }, 400);


        // Обработчик клика для перехода на детальный экран
        adapter.setOnItemClickListener((game, imageView) -> {
            Intent intent = new Intent(FavoritesActivity.this, DetailActivity.class);
            // Передаем ID игры с правильным ключом "Game"
            intent.putExtra("Game", game.getId());
            startActivity(intent);
        });

        // Удаляем из избранного
        adapter.setOnFavoriteClickListener(game ->
                viewModel.setFavorite(game.id, false)
        );
    }
}
