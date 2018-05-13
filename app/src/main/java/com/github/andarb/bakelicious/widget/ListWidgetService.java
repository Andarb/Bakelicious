package com.github.andarb.bakelicious.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.github.andarb.bakelicious.R;

public class ListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

/**
 * This class is used to display a list of ingredients contained wholly in one list item.
 * Ideally, instead of this ListView, a simple TextView would suffice. However, since ScrollView is
 * not supported by widgets, using ListView grants us vertical scrolling even for a single list
 * item.
 */
class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private int mAppWidgetId;

    ListRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
    }

    // As we have the whole list of ingredients in 1 String, count will always be 1
    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public RemoteViews getViewAt(int i) {

        RemoteViews views = new RemoteViews(mContext.getPackageName(),
                R.layout.ingredient_widget_item);

        SharedPreferences sharedPref = mContext.getSharedPreferences(
                mContext.getString(R.string.preferences_file_key), Context.MODE_PRIVATE);
        String ingredients = sharedPref.getString(
                mContext.getString(R.string.preferences_ingredients_key),
                mContext.getString(R.string.error_empty_ingredient_list));

        views.setTextViewText(R.id.widget_ingredient_text_view, ingredients);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
