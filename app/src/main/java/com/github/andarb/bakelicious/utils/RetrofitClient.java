package com.github.andarb.bakelicious.utils;

import com.github.andarb.bakelicious.data.Recipe;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Retrofit library is (in our case) used to download the recipes JSON file from
 * the internet, and to convert it to POJO objects.
 */
public final class RetrofitClient {

    // Url for the recipes JSON file
    private static final String BASE_URL =
            "https://d17h27t6h515a5.cloudfront.net/topher/";
    private static final String FILE_URL = "2017/May/59121517_baking/baking.json";

    // Retrofit recipe interface
    private interface RecipeApi {
        @GET(FILE_URL)
        Call<List<Recipe>> getRecipeList();
    }

    /* Setup retrofit, and return a retrofit `Call`, which can be used to start downloading */
    public static Call<List<Recipe>> getRecipes() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RecipeApi apiService = retrofit.create(RecipeApi.class);

        return apiService.getRecipeList();
    }
}
