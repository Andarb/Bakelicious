package com.github.andarb.bakelicious.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.RemoteViews;

import com.github.andarb.bakelicious.R;

public class IngredientProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // Iterate over all widgets
        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];

            // Retrieve recipe name from shared preferences file
            SharedPreferences sharedPref = context.getSharedPreferences(
                    context.getString(R.string.preferences_file_key), Context.MODE_PRIVATE);
            String defaultValue = context.getString(R.string.preferences_recipe_default_value);
            String recipe = sharedPref.getString(context.getString(R.string.preferences_recipe_key),
                    defaultValue);

            // Set ListView adapter for ingredients if they're available, and set the recipe name
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ingredient_widget);
            if (!recipe.equals(defaultValue)) {
                // Intent for ListWidgetService that provides the ingredients view
                Intent intent = new Intent(context, ListWidgetService.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
                intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

                views.setRemoteAdapter(R.id.widget_ingredient_list_view, intent);
            }
            views.setTextViewText(R.id.widget_recipe_text_view, recipe);

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }
}
