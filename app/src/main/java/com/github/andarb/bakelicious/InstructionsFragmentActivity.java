package com.github.andarb.bakelicious;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Manages StepDetailsFragment and StepListFragment.
 * Implements Master/Detail flow navigation.
 */
public class InstructionsFragmentActivity extends FragmentActivity
        implements StepListFragment.OnStepSelectedListener {
    // Clicked recipe in MainActivity
    public final static String RECIPE_EXTRA = "recipe";

    @BindBool(R.bool.isTablet)
    boolean mIsTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instructions);
        ButterKnife.bind(this);

        //TODO get recipe clicked from MainActivity
        int recipe = getIntent().getIntExtra(RECIPE_EXTRA, 0);

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

            StepListFragment stepListFragment = (StepListFragment)
                    getSupportFragmentManager().findFragmentById(R.id.step_list_fragment);
            // stepListFragment.update(newdata);
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
