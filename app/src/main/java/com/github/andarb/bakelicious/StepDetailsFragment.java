package com.github.andarb.bakelicious;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.andarb.bakelicious.data.Recipe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * When a recipe step (list item) in StepListFragment is clicked,
 * further information pertaining to that step is displayed here
 */
public class StepDetailsFragment extends Fragment {

    @BindView(R.id.step_description_text_view)
    TextView stepDescriptionTV;

    private Recipe mRecipe;
    private Unbinder mButterknifeUnbinder;

    // Required empty public constructor
    public StepDetailsFragment() {
    }

    // Create a new instance of the fragment, and pass it a recipe and a recipe step
    public static StepDetailsFragment newInstance(Recipe recipe, int step) {
        StepDetailsFragment f = new StepDetailsFragment();

        Bundle args = new Bundle();
        args.putParcelable(InstructionsFragmentActivity.RECIPE_EXTRA, recipe);
        args.putInt(InstructionsFragmentActivity.STEP_EXTRA, step);
        f.setArguments(args);

        return f;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.step_details, container, false);
        mButterknifeUnbinder = ButterKnife.bind(this, view);

        // Retrieve recipe from our activity, and populate details
        mRecipe = getArguments().getParcelable(InstructionsFragmentActivity.RECIPE_EXTRA);
        int stepNr = getArguments().getInt(InstructionsFragmentActivity.STEP_EXTRA, 0);
        updateDetails(stepNr);

        return view;
    }

    public void updateDetails(int step) {
        //TODO update exoplayer here
        stepDescriptionTV.setText(mRecipe.getSteps().get(step).getDescription());
    }

    // Required unbind when using Butterknife with Fragments
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mButterknifeUnbinder.unbind();
    }
}
