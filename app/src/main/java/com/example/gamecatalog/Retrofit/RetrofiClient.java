package com.example.gamecatalog.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;


public class RetrofiClient {
    private static final String BASE_URL="https://www.freetogame.com/api/";
    private static Retrofit retrofit=null;

    public static Retrofit getRetrofit() {
        if(retrofit==null){

            // Интерсептор, чтобы смотреть запросы/ответы (очень полезно!)///
            HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
            logger.setLevel(HttpLoggingInterceptor.Level.BODY);
            // Клиент OkHttp
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build();

            retrofit=new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    // Быстрый доступ к API
    public static ServiceApi api() {
        return getRetrofit().create(ServiceApi.class);
    }

}


