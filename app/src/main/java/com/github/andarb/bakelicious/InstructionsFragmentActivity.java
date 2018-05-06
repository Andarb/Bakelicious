package com.github.andarb.bakelicious;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.github.andarb.bakelicious.data.Recipe;

import butterknife.BindBool;
import butterknife.ButterKnife;

/**
 * Manages StepDetailsFragment and StepListFragment.
 * Implements Master/Detail flow navigation.
 */
public class InstructionsFragmentActivity extends FragmentActivity
        implements StepListFragment.OnStepSelectedListener {

    public final static String RECIPE_EXTRA = "recipe";

    @BindBool(R.bool.isTablet)
    boolean mIsTablet;

    private Recipe mRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instructions);
        ButterKnife.bind(this);

        // Get the clicked (in MainActivity) recipe
        mRecipe = getIntent().getParcelableExtra(RECIPE_EXTRA);

        setTitle(mRecipe.getName());

        // If we are on a phone, then we will need to add the first fragment
        if (!mIsTablet) {
            // There is no need to add the fragment again, if activity is being restored
            if (savedInstanceState != null) return;

            // Create and add a fragment with a list of steps
            StepListFragment stepListFragment = new StepListFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, stepListFragment).commit();
        }
    }

    // When a recipe step in StepListFragment is clicked, open or replace StepDetailsFragment
    @Override
    public void onStepSelected(int position) {
        if (mIsTablet) {
            //TODO update StepDetailsFragment with new information. remove toast
            Toast.makeText(this, "tablet test position:" + position, Toast.LENGTH_SHORT).show();

            StepDetailsFragment stepDetailsFragment = (StepDetailsFragment)
                    getSupportFragmentManager().findFragmentById(R.id.step_details_fragment);
            // stepDetailsFragment.update(newdata);
        } else {
            //TODO replace StepListFragment with StepDetailsFragment. remove toast
            Toast.makeText(this, "phone test position:" + position, Toast.LENGTH_SHORT).show();

            StepDetailsFragment stepDetailsFragment = new StepDetailsFragment();
            // stepDetailsFragment.setArguments(newdata);
            FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();

            fragTransaction.replace(R.id.fragment_container, stepDetailsFragment).
                    addToBackStack(null).commit();
        }
    }
}
