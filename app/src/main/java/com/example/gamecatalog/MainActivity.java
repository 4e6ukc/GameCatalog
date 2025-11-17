package com.example.gamecatalog;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamecatalog.Entites.Game;
import com.example.gamecatalog.Retrofit.RetrofiClient;
import com.example.gamecatalog.Retrofit.ServiceApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG="Z";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ServiceApi service= RetrofiClient.getRetrofit().create(ServiceApi.class);
        try {
           Call<List<Game>> call= service.getGameList();
            call.enqueue(new Callback<List<Game>>() {
                @Override
                public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Game> gameList = response.body();

                        // вывести в лог названия первых нескольких игр
                        for (int i = 0; i < Math.min(gameList.size(), 5); i++) {
                            Log.i(TAG, "Информация: " + gameList.get(i).getTitle());
                        }
                    } else {
                        Log.w(TAG, "Ответ не удался. Код: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<List<Game>> call, Throwable throwable) {
                    Log.e(TAG,"Error: ", throwable);

                }
            });

        }
        catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
}