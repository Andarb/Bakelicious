package com.github.andarb.bakelicious.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.andarb.bakelicious.R;
import com.github.andarb.bakelicious.StepListFragment;
import com.github.andarb.bakelicious.data.Recipe;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Binds views for a number of recipe steps.
 */
public class StepAdapter extends RecyclerView.Adapter<StepAdapter.StepViewHolder> {

    // Helps separate step numbers from descriptions
    private final static String DELIMITER = ". ";

    private final Context mContext;
    private StepListFragment.OnStepSelectedListener mCallback;
    private Recipe mRecipe;
    private int mSelectedPosition = RecyclerView.NO_POSITION;

    public StepAdapter(Context context, StepListFragment.OnStepSelectedListener callback,
                       Recipe recipe, boolean isTablet) {
        mContext = context;
        mCallback = callback;
        mRecipe = recipe;
        if (isTablet) mSelectedPosition = 0;
    }


    class StepViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.step_name_text_view)
        TextView stepNameTV;
        @BindView(R.id.step_introduction_image_view)
        ImageView stepIntroductionIV;
        @BindView(R.id.step_list_item)
        LinearLayout stepListItem;

        public StepViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            // Updates (via Selector) backgrounds of the old and new list item
            notifyItemChanged(mSelectedPosition);
            mSelectedPosition = position;
            notifyItemChanged(mSelectedPosition);

            // Callback to InstructionsFragmentActivity passing in the step clicked
            mCallback.onStepSelected(position);
        }
    }

    @NonNull
    @Override
    public StepAdapter.StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.step_list_item, parent, false);

        return new StepAdapter.StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepAdapter.StepViewHolder holder, int position) {
        // Check if current list item is selected, and activate background Selector if it is
        holder.stepListItem.setSelected(mSelectedPosition == position);

        // Check if description is available, and add an image instead of position number zero
        String description = mRecipe.getSteps().get(position).getShortDescription().trim();
        if (description.isEmpty()) description = mContext.getString(R.string.missing_description);
        if (position != 0) {
            holder.stepIntroductionIV.setVisibility(View.GONE);
            description = position + DELIMITER + description;
        } else {
            holder.stepIntroductionIV.setVisibility(View.VISIBLE);
        }

        holder.stepNameTV.setText(description);
    }

    @Override
    public int getItemCount() {
        return mRecipe.getSteps().size();
    }

    /* Set the new selected position */
    public void setClickedStep(int position) {
        mSelectedPosition = position;
    }
}