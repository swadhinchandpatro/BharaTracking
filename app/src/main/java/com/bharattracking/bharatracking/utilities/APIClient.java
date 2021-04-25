package com.bharattracking.bharatracking.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Url;

public class APIClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(Utils.READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .connectTimeout(Utils.CONNECTION_TIMEOUT,TimeUnit.MILLISECONDS)
                .writeTimeout(Utils.WRITE_TIMEOUT,TimeUnit.MILLISECONDS)
                .addInterceptor(interceptor)
                .build();

        Gson gson = new GsonBuilder().setLenient().create();

        retrofit = new Retrofit.Builder()
                .baseUrl("http://168.235.102.152")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        return retrofit;
    }

    //TODO network check and caching
    public static boolean hasActiveConnection() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) (new URL("http://clients3.google.com/generate_204").openConnection());
        return (connection.getResponseCode() == 204 && connection.getContentLength() == 0);
    }
}
