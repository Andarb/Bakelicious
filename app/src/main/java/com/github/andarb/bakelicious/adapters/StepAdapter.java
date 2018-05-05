package com.github.andarb.bakelicious.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.github.andarb.bakelicious.R;
import com.github.andarb.bakelicious.StepListFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Binds views for a number of recipe steps.
 */
public class StepAdapter extends RecyclerView.Adapter<StepAdapter.StepViewHolder> {

    private final Context mContext;
    private StepListFragment.OnStepSelectedListener mCallback;

    public StepAdapter(Context context, StepListFragment.OnStepSelectedListener callback) {
        mContext = context;
        mCallback = callback;
    }


    class StepViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.step_name_text_view)
        TextView stepNameTV;

        public StepViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            // Callback to InstructionsFragmentActivity passing in the step clicked
            mCallback.onStepSelected(position);
        }
    }

    @Override
    public StepAdapter.StepViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.step_list_item, parent, false);

        return new StepAdapter.StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StepAdapter.StepViewHolder holder, int position) {
        // TODO bind views
        holder.stepNameTV.setText(position + ". Test step instruction.");

    }

    @Override
    public int getItemCount() {
        // TODO get appropriate size
        return 10;
    }
}