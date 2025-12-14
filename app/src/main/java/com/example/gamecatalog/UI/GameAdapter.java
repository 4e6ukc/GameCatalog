package com.example.gamecatalog.UI;

import com.bumptech.glide.Glide;
import com.example.gamecatalog.R;
import com.example.gamecatalog.Entites.Game;

import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.Holder> {

    private List<Game> items = new ArrayList<>();

    // Интерфейсы для кликов
    public interface OnItemClickListener {
        void onItemClick(Game game, ImageView img);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Game game);
    }

    private OnItemClickListener clickListener;
    private OnFavoriteClickListener favListener;

    public void setItems(List<Game> list) {
        this.items = list;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        clickListener = l;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener l) {
        favListener = l;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int v) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        // Получаем объект Game
        Game game = items.get(pos);

        // Заполняем поля из объекта Game в соответствии с новым макетом
        h.textName.setText(game.title);
        h.textGenre.setText(game.genre); // Заполняем жанр
        h.textReleaseDate.setText(game.release_date); // Заполняем дату релиза
        h.textPublisher.setText(game.publisher); // Заполняем издателя
        h.textShortDescription.setText(game.short_description); // Заполняем краткое описание

        // Загрузка изображения
        Glide.with(h.itemView.getContext())
                .load(game.thumbnail)
                .into(h.image);

        // Установка иконки избранного
        h.favorite.setImageResource(
                game.isFavorite
                        ? android.R.drawable.btn_star_big_on
                        : android.R.drawable.btn_star_big_off
        );

        // Анимация появления остается без изменений
        h.itemView.setAlpha(0f);
        h.itemView.setTranslationY(40);
        h.itemView.animate().alpha(1f).translationY(0).setDuration(200).start();

        // Клик по карточке
        h.itemView.setOnClickListener(v -> {
            // Проверяем, что clickListener существует (хорошая практика)
            if (clickListener != null) {
                // Делегируем клик в MainActivity. Так правильнее.
                clickListener.onItemClick(game, h.image);
            }
        });

        // Клик по избранному
        h.favorite.setOnClickListener(v -> {
            if (favListener != null)
                favListener.onFavoriteClick(game);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Храним ссылки на элементы карточки
    static class Holder extends RecyclerView.ViewHolder {
        // Объявляем все View из нового макета
        ImageView image, favorite;
        TextView textName, textGenre, textReleaseDate, textPublisher, textShortDescription;

        public Holder(@NonNull View v) {
            super(v);
            // Находим все View по их ID
            image = v.findViewById(R.id.imageGame);
            favorite = v.findViewById(R.id.iconFavorite);
            textName = v.findViewById(R.id.textName);
            textGenre = v.findViewById(R.id.textGenre);
            textReleaseDate = v.findViewById(R.id.textReleaseDate);
            textPublisher = v.findViewById(R.id.textPublisher);
            textShortDescription = v.findViewById(R.id.textShortDescription);
        }
    }
}
