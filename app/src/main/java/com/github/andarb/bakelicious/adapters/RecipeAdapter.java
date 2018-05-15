package com.github.andarb.bakelicious.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.andarb.bakelicious.InstructionsFragmentActivity;
import com.github.andarb.bakelicious.R;
import com.github.andarb.bakelicious.data.Recipe;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Binds views for a collection of recipes.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private final Context mContext;
    private List<Recipe> mRecipes;

    public RecipeAdapter(Context context, List<Recipe> recipes) {
        mContext = context;
        mRecipes = recipes;
    }


    class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.recipe_photo_image_view)
        ImageView recipePhotoIV;
        @BindView(R.id.recipe_name_text_view)
        TextView recipeNameTV;
        @BindView(R.id.recipe_servings_text_view)
        TextView recipeServingsTV;

        public RecipeViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            Intent instructionsIntent = new Intent(mContext, InstructionsFragmentActivity.class);
            instructionsIntent.putExtra(InstructionsFragmentActivity.RECIPE_EXTRA,
                    mRecipes.get(position));

            mContext.startActivity(instructionsIntent);
        }
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.recipe_list_item, parent, false);

        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        // Retrieve recipe values
        String imageUrl = mRecipes.get(position).getImage();
        String recipeName = mRecipes.get(position).getName();
        String recipeServings = String.valueOf(mRecipes.get(position).getServings());

        // Check if they aren't empty, and set
        if (!imageUrl.trim().isEmpty()) Picasso.get().load(imageUrl).into(holder.recipePhotoIV);

        if (!recipeName.trim().isEmpty()) {
            holder.recipeNameTV.setText(recipeName);
        } else {
            holder.recipeNameTV.setText(R.string.missing_recipe_name);
        }

        if (!recipeServings.trim().isEmpty()) {
            holder.recipeServingsTV.setText(recipeServings);
        } else {
            holder.recipeServingsTV.setText(R.string.missing_servings);
        }
    }

    @Override
    public int getItemCount() {
        return mRecipes.size();
    }
}
