package com.github.andarb.bakelicious;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

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

        // There is no need to add the fragments again, if activity is being restored
        if (savedInstanceState != null) return;

        // Get the clicked (in MainActivity) recipe
        mRecipe = getIntent().getParcelableExtra(RECIPE_EXTRA);

        // Set action bar title to be the name of the recipe
        setTitle(mRecipe.getName());

        // Create and add a fragment with a list of recipe steps
        StepListFragment stepListFragment = StepListFragment.newInstance(mRecipe);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.list_fragment_container, stepListFragment).commit();

        // If on a tablet, add a second fragment for the recipe step details
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

            FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
            fragTransaction.replace(R.id.list_fragment_container, stepDetailsFragment).
                    addToBackStack(null).commit();
        }
    }
}
