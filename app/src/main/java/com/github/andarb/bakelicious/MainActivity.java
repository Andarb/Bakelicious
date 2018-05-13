package com.github.andarb.bakelicious;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.github.andarb.bakelicious.adapters.RecipeAdapter;
import com.github.andarb.bakelicious.data.Recipe;
import com.github.andarb.bakelicious.utils.RetrofitClient;

import java.util.List;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Downloads and displays a list of recipes.
 * Recipes can be clicked on to get further details and instructions.
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.recipes_recycler_view)
    RecyclerView mRecyclerView;
    @BindBool(R.bool.isTablet)
    boolean mIsTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Start the download of recipes
        downloadRecipes();

        // Check if the app is running on a tablet or a phone.
        // And, set the appropriate number of columns in GridView.
        int nrOfGridColumns;
        if(mIsTablet) {
            nrOfGridColumns = 3;
        } else {
            nrOfGridColumns = 1;
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, nrOfGridColumns));
        mRecyclerView.setHasFixedSize(true);
    }

    /* Download and parse a list of recipes using Retrofit */
    public void downloadRecipes() {
        Call<List<Recipe>> getCall = RetrofitClient.getRecipes();

        getCall.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call,
                                   Response<List<Recipe>> response) {
                if (response.isSuccessful()) {

                    //TODO replace error Toasts with a textview
                    List<Recipe> recipes = response.body();
                    if (recipes == null) {
                        Toast.makeText(MainActivity.this, R.string.error_empty_recipe_list,
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    RecipeAdapter recipeAdapter = new RecipeAdapter(MainActivity.this, recipes);
                    mRecyclerView.setAdapter(recipeAdapter);
                } else {
                    Toast.makeText(MainActivity.this,
                            R.string.error_server_status + response.code(), Toast.LENGTH_LONG)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                Toast.makeText(MainActivity.this, R.string.error_internet,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
