package com.andarb.bakelicious;


import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andarb.bakelicious.adapters.StepAdapter;
import com.andarb.bakelicious.data.Ingredient;
import com.andarb.bakelicious.data.Recipe;
import com.andarb.bakelicious.widget.IngredientProvider;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindBool;
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
    private final static String HYPHEN = "-" + SPACE;

    // Action for the broadcast receiver
    public static final String STEP_SELECTED_ACTION =
            "com.andarb.bakelicious.RECIPE_STEP_SELECTED";

    // InstructionsFragmentActivity must implement this interface in order for
    // StepListFragment and StepDetailsFragment to communicate when a recipe step is clicked.
    public interface OnStepSelectedListener {
        void onStepSelected(int position);
    }

    private OnStepSelectedListener mCallback;


    @BindBool(R.bool.isTablet)
    boolean mIsTablet;
    @BindView(R.id.steps_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.ingredients_text_view)
    TextView mIngredientsTV;

    private Unbinder mButterknifeUnbinder;
    private StepReceiver mStepReceiver;
    private StepAdapter mStepAdapter;


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

        // Register a broadcast receiver
        if (mIsTablet) {
            mStepReceiver = new StepReceiver();
            IntentFilter filter = new IntentFilter(STEP_SELECTED_ACTION);
            getActivity().registerReceiver(mStepReceiver, filter);
        }

        // Retrieve recipe from our activity
        Recipe recipe = getArguments().getParcelable(InstructionsFragmentActivity.RECIPE_EXTRA);

        // Parse a list of ingredients. Then, set it to be displayed, and save a copy for the widget
        String ingredients = parseIngredients(recipe.getIngredients());
        mIngredientsTV.setText(ingredients);
        saveIngredients(recipe.getName(), ingredients);

        // Set up the adapter and recyclerview to display recipe steps
        mStepAdapter = new StepAdapter(context, mCallback, recipe, mIsTablet);

        mRecyclerView.setLayoutManager(new LinearLayoutManager
                (context, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mStepAdapter);

        return view;
    }

    /* Create a list of ingredients out of individual values */
    private String parseIngredients(List<Ingredient> ingredients) {
        StringBuilder sB = new StringBuilder();
        DecimalFormat dF = new DecimalFormat("#.#"); // remove trailing zeros

        for (int i = 0; i < ingredients.size(); i++) {
            String quantity = String.valueOf(dF.format(ingredients.get(i).getQuantity()));
            String measure = ingredients.get(i).getMeasure();
            String ingredient = ingredients.get(i).getIngredient();

            sB.append(HYPHEN);

            // Check if any values are empty
            if (TextUtils.isEmpty(quantity)) {
                quantity = getString(R.string.missing_quantity);
            }
            sB.append(quantity);


            if (TextUtils.isEmpty(measure)) {
                sB.append(getString(R.string.missing_measure));
            } else if (!measure.equals("UNIT")) { // If it's a unit, skip it
                // Don't add space if the measure is grams or kilos
                if (!measure.equals("G") && !measure.equals("K")) sB.append(SPACE);
                sB.append(measure);
            }
            sB.append(SPACE);

            if (TextUtils.isEmpty(ingredient)) {
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

        // Broadcast receiver no longer needed
        if (mIsTablet) getActivity().unregisterReceiver(mStepReceiver);

        mButterknifeUnbinder.unbind(); // Required unbind when using Butterknife with Fragments
    }


    /**
     * Listens for Previous/Next step button clicks in StepDetailsFragment.
     * When received, scroll to the new step in the list.
     * And update the adapter to highlight the new selected step.
     */
    private class StepReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(STEP_SELECTED_ACTION)) {
                int position = intent.getIntExtra(InstructionsFragmentActivity.STEP_EXTRA,
                        RecyclerView.NO_POSITION);

                mRecyclerView.scrollToPosition(position);
                mStepAdapter.setClickedStep(position);
                mStepAdapter.notifyDataSetChanged();
            }
        }
    }
}
