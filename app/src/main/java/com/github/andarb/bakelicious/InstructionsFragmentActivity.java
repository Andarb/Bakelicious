package com.github.andarb.bakelicious;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.github.andarb.bakelicious.data.Recipe;

import butterknife.BindBool;
import butterknife.ButterKnife;

/**
 * Manages StepDetailsFragment and StepListFragment.
 * Implements Master/Detail flow navigation.
 */
public class InstructionsFragmentActivity extends FragmentActivity
        implements StepListFragment.OnStepSelectedListener {

    public final static String RECIPE_EXTRA = "recipe_object";
    public final static String STEP_EXTRA = "recipe_step_number";

    @BindBool(R.bool.isTablet)
    boolean mIsTablet;

    private Recipe mRecipe;
    private StepDetailsFragment mStepDetailsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instructions);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            // FragmentManager will recreate fragments automatically, so we just need to make sure
            // we restore Activity members that are needed for fragment operation, and then return;
            mRecipe = savedInstanceState.getParcelable(RECIPE_EXTRA);
            if (mIsTablet) {
                mStepDetailsFragment = (StepDetailsFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.details_fragment_container);
            }

            return;
        } else {
            // Get the clicked recipe (from MainActivity)
            mRecipe = getIntent().getParcelableExtra(RECIPE_EXTRA);
        }

        // Set action bar title to be the name of the recipe
        setTitle(mRecipe.getName());

        // Launch a list of recipe steps on both a phone or a tablet
        StepListFragment stepListFragment = StepListFragment.newInstance(mRecipe);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.list_fragment_container, stepListFragment)
                .commit();

        // If on a tablet, also add a second fragment for the step details
        if (mIsTablet) {
            mStepDetailsFragment = StepDetailsFragment.newInstance(mRecipe, 0);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.details_fragment_container, mStepDetailsFragment).commit();
        }
    }

    // When a recipe step in StepListFragment is clicked, update or replace StepDetailsFragment
    @Override
    public void onStepSelected(int position) {
        if (mIsTablet) {
            mStepDetailsFragment.updateDetails(position);
        } else {
            StepDetailsFragment stepDetailsFragment = StepDetailsFragment.newInstance(mRecipe, position);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.list_fragment_container, stepDetailsFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    // Save recipe details, so we don't need to do another network request for them
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(RECIPE_EXTRA, mRecipe);
        super.onSaveInstanceState(outState);
    }
}
