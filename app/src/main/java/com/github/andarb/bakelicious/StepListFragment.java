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

import com.github.andarb.bakelicious.adapters.StepAdapter;
import com.github.andarb.bakelicious.data.Recipe;

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

    private Unbinder mButterknifeUnbinder;


    // Required empty public constructor
    public StepListFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context context = getActivity();
        View view = inflater.inflate(R.layout.step_list, container, false);
        mButterknifeUnbinder = ButterKnife.bind(this, view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager
                (context, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);

        // TODO setup proper adapter values
        Recipe recipe = getActivity().getIntent().getParcelableExtra(InstructionsFragmentActivity.RECIPE_EXTRA);
        StepAdapter stepAdapter = new StepAdapter(context, mCallback, recipe);
        mRecyclerView.setAdapter(stepAdapter);

        return view;
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
