package com.github.andarb.bakelicious;


import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.github.andarb.bakelicious.widget.IngredientProvider;

import java.text.DecimalFormat;
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

    // Used to parse a list of ingredients
    private final static String NEW_LINE = "\n";
    private final static String SPACE = " ";
    private final static String HYPHEN = "- ";

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


    /* Required empty public constructor */
    public StepListFragment() {
    }

    /* Create a new instance of the fragment, and pass it a recipe */
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

        // Parse a list of ingredients. Then, set it to be displayed, and save a copy for the widget
        String ingredients = parseIngredients(recipe.getIngredients());
        mIngredientsTV.setText(ingredients);
        saveIngredients(recipe.getName(), ingredients);

        // Set up the adapter and recyclerview to display recipe steps
        StepAdapter stepAdapter = new StepAdapter(context, mCallback, recipe);

        mRecyclerView.setLayoutManager(new LinearLayoutManager
                (context, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(stepAdapter);

        return view;
    }

    /* Create a list of ingredients out of individual values */
    private String parseIngredients(List<Ingredient> ingredients) {
        StringBuilder sB = new StringBuilder();
        DecimalFormat dF = new DecimalFormat("#.#"); // remove trailing zeros

        for (int i = 0; i < ingredients.size(); i++) {
            String quantity = String.valueOf(dF.format(ingredients.get(i).getQuantity()));
            String measure = ingredients.get(i).getMeasure().trim();
            String ingredient = ingredients.get(i).getIngredient().trim();

            sB.append(HYPHEN);

            // Check if any values are empty
            if (quantity.isEmpty()) {
                quantity = getString(R.string.missing_quantity);
            }
            sB.append(quantity);


            if (measure.isEmpty()) {
                sB.append(getString(R.string.missing_measure));
            } else if (!measure.equals("UNIT")) { // If it's a unit, skip it
                // Don't add space if the measure is grams or kilos
                if (!measure.equals("G") && !measure.equals("K")) sB.append(SPACE);
                sB.append(measure);
            }
            sB.append(SPACE);

            if (ingredient.isEmpty()) {
                ingredient = getString(R.string.missing_ingredient);
            }
            sB.append(ingredient);
            sB.append(NEW_LINE);
        }

        return sB.toString().toLowerCase().trim();
    }

    /* Save recipe name and its ingredients, and try to update the widget */
    private void saveIngredients(String recipe, String ingredients) {
        Activity activity = getActivity();
        SharedPreferences sharedPref = activity.getSharedPreferences(
                activity.getString(R.string.preferences_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.preferences_recipe_key), recipe);
        editor.putString(getString(R.string.preferences_ingredients_key), ingredients);
        editor.commit();

        Intent intent = new Intent(activity, IngredientProvider.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int appWidgetIds[] = AppWidgetManager.getInstance(activity.getApplication())
                .getAppWidgetIds(new ComponentName(activity.getApplication(), IngredientProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        activity.sendBroadcast(intent);
    }

    /* This ensures InstructionsFragmentActivity has implemented OnStepSelectedListener interface */
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mButterknifeUnbinder.unbind(); // Required unbind when using Butterknife with Fragments
    }
}
