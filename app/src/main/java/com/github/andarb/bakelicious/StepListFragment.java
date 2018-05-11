package com.github.andarb.bakelicious;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.andarb.bakelicious.adapters.StepAdapter;
import com.github.andarb.bakelicious.data.Ingredient;
import com.github.andarb.bakelicious.data.Recipe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A list of recipe steps is displayed here.
 * Each step can be clicked on to display further details
 * for that specific step (in StepDetailsFragment).
 */
public class StepListFragment extends Fragment {

    // InstructionsFragmentActivity must implement this interface in order for
    // StepListFragment and StepDetailsFragment to communicate when a recipe step is clicked.
    public interface OnStepSelectedListener {
        public void onStepSelected(int position);
    }
    OnStepSelectedListener mCallback;

    @BindView(R.id.steps_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.ingredients_text_view)
    TextView mIngredientsTV;

    private Unbinder mButterknifeUnbinder;


    // Required empty public constructor
    public StepListFragment() {
    }

    // Create a new instance of the fragment, and pass it a recipe
    public static StepListFragment newInstance(Recipe recipe) {
        StepListFragment f = new StepListFragment();

        Bundle args = new Bundle();
        args.putParcelable(InstructionsFragmentActivity.RECIPE_EXTRA, recipe);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context context = getActivity();
        View view = inflater.inflate(R.layout.step_list, container, false);
        mButterknifeUnbinder = ButterKnife.bind(this, view);

        // Retrieve recipe from our activity
        Recipe recipe = getArguments().getParcelable(InstructionsFragmentActivity.RECIPE_EXTRA);

        // Parse and set a list of ingredients
        String ingredients = parseIngredients(recipe.getIngredients());
        mIngredientsTV.setText(ingredients);

        // Set up the adapter and recyclerview to display recipe steps
        StepAdapter stepAdapter = new StepAdapter(context, mCallback, recipe);

        mRecyclerView.setLayoutManager(new LinearLayoutManager
                (context, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(stepAdapter);

        return view;
    }

    private String parseIngredients(List<Ingredient> ingredients) {
        StringBuilder sB = new StringBuilder();
        for (int i = 0; i < ingredients.size(); i++) {
            float quantity = ingredients.get(i).getQuantity();
            String measure = ingredients.get(i).getMeasure();
            String ingredient = ingredients.get(i).getIngredient();

            sB.append(quantity);
            sB.append(" ");
            sB.append(measure);
            sB.append(" ");
            sB.append(ingredient);
            sB.append("\n");
        }

        return sB.toString().toLowerCase();
    }

    // This ensures InstructionsFragmentActivity has implemented OnStepSelectedListener interface
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnStepSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnImageClickListener");
        }
    }

    // Required unbind when using Butterknife with Fragments
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mButterknifeUnbinder.unbind();
    }
}
