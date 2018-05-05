package com.github.andarb.bakelicious.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.andarb.bakelicious.InstructionsFragmentActivity;
import com.github.andarb.bakelicious.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Binds views for a collection of recipes.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private final Context mContext;

    public RecipeAdapter(Context context) {
        mContext = context;
    }


    class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.recipe_name_text_view)
        TextView recipeNameTV;

        public RecipeViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            Intent instructionsIntent = new Intent(mContext, InstructionsFragmentActivity.class);
            instructionsIntent.putExtra(InstructionsFragmentActivity.RECIPE_EXTRA, position);
            // TODO pass extras that you need

            mContext.startActivity(instructionsIntent);
        }
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.recipe_list_item, parent, false);

        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        // TODO bind views
        holder.recipeNameTV.setText("Test Recipe Name");

    }

    @Override
    public int getItemCount() {
        // TODO get appropriate size
        return 5;
    }
}
