package com.github.andarb.bakelicious;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.andarb.bakelicious.adapters.RecipeAdapter;
import com.github.andarb.bakelicious.data.Recipe;
import com.github.andarb.bakelicious.utils.RetrofitClient;
import com.github.andarb.bakelicious.widget.IngredientProvider;

import java.util.List;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
    @BindView(R.id.recipes_progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.error_layout)
    View mErrorLayout;
    @BindView(R.id.error_message_text_view)
    TextView mErrorTV;
    @BindBool(R.bool.isTablet)
    boolean mIsTablet;

    private boolean mLaunchedByWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Check if this activity was launched from our widget
        mLaunchedByWidget = getIntent().getBooleanExtra(IngredientProvider.WIDGET_EXTRA, false);

        // Start the download of recipes
        downloadRecipes();

        // Check if the app is running on a tablet or a phone.
        // And, set the appropriate number of columns in GridView.
        int nrOfGridColumns;
        if (mIsTablet) {
            nrOfGridColumns = 3;
        } else {
            nrOfGridColumns = 1;
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, nrOfGridColumns));
        mRecyclerView.setHasFixedSize(true);
    }

    /* Download and parse a list of recipes using Retrofit */
    public void downloadRecipes() {
        mProgressBar.setVisibility(View.VISIBLE);
        Call<List<Recipe>> getCall = RetrofitClient.getRecipes();

        getCall.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call,
                                   Response<List<Recipe>> response) {
                if (response.isSuccessful()) {
                    hideError();

                    List<Recipe> recipes = response.body();
                    if (recipes == null) {
                        showError(getString(R.string.missing_recipe_list));
                        return;
                    }

                    // Try to open the recipe clicked in the widget
                    if (mLaunchedByWidget) openWidgetRecipe(recipes);

                    RecipeAdapter recipeAdapter = new RecipeAdapter(MainActivity.this, recipes);
                    mRecyclerView.setAdapter(recipeAdapter);
                } else {
                    showError(getString(R.string.error_server_status) + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                showError(getString(R.string.error_internet));
            }
        });
    }

    /* Retrieve the last saved recipe name (if any), and open the appropriate recipe */
    private void openWidgetRecipe(List<Recipe> recipes) {
        SharedPreferences sharedPref =
                getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE);

        String defaultValue = getString(R.string.preferences_recipe_default_value);
        String recipeKey = getString(R.string.preferences_recipe_key);
        String recipeName = sharedPref.getString(recipeKey, defaultValue);

        // If widget/SharedPrefs are empty, remain in MainActivity so a recipe can be chosen
        if (!recipeKey.equals(defaultValue)) {
            for (int i = 0; i < recipes.size(); i++) {
                if (recipes.get(i).getName().equals(recipeName)) {
                    Intent intent = new Intent(this, InstructionsFragmentActivity.class);
                    intent.putExtra(InstructionsFragmentActivity.RECIPE_EXTRA,
                            recipes.get(i));

                    startActivity(intent);
                }
            }
        }
    }

    /* Hides loading bar and displays an error message */
    private void showError(String error) {
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        mErrorTV.setText(error);
        mErrorLayout.setVisibility(View.VISIBLE);
    }

    /* Hides the error layout and the loading animation, and displays the downloaded results */
    private void hideError() {
        mProgressBar.setVisibility(View.GONE);
        mErrorLayout.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /* Used to retry downloading recipes when network issues occur */
    @OnClick(R.id.error_retry_button)
    public void onRetryClicked() {
        downloadRecipes();
    }
}
