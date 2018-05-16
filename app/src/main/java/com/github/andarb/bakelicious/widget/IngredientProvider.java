package com.github.andarb.bakelicious.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.RemoteViews;

import com.github.andarb.bakelicious.MainActivity;
import com.github.andarb.bakelicious.R;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

/**
 * Setup a widget to display a list of a recipe name and a list of ingredients.
 * A recipe name can be clicked, which opens that recipe in our app.
 */
public class IngredientProvider extends AppWidgetProvider {

    public static final String WIDGET_EXTRA = "widget_launch";

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
                Intent serviceIntent = new Intent(context, ListWidgetService.class);
                serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
                serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

                views.setRemoteAdapter(R.id.widget_ingredient_list_view, serviceIntent);
            }
            views.setTextViewText(R.id.widget_recipe_text_view, recipe);

            // When recipe name is clicked, MainActivity is launched. Extra is passed to let the
            // activity know it was launched by the widget.
            Intent widgetIntent = new Intent(context, MainActivity.class);
            // We don't need a stack history, as the recipe clicked in the widget will always be
            // the last one opened in the app
            widgetIntent.addFlags(FLAG_ACTIVITY_CLEAR_TASK);
            widgetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            widgetIntent.putExtra(WIDGET_EXTRA, true);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, widgetIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_recipe_text_view, pendingIntent);

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }
}
