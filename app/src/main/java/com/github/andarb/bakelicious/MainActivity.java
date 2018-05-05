package com.github.andarb.bakelicious;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.github.andarb.bakelicious.adapters.RecipeAdapter;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.ButterKnife;

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

        // TODO setup proper adapter values
        RecipeAdapter recipeAdapter = new RecipeAdapter(this);
        mRecyclerView.setAdapter(recipeAdapter);
    }
}
