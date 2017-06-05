package com.rightdirection.calendarwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Сервис для отображения сервиса календаря. Его вызывает ListAdapter.
 */
public class CalendarWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CalendarWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

/**
 * Фабрика для заполнения событий календаря
 */
class CalendarWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private ArrayList<String> mData;
    private Context mContext;
    private int mWidgetID;

    public CalendarWidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mWidgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        mData = new ArrayList<>();
    }

    @Override
    public void onDataSetChanged() {
        // Заполним список тестовыми данными
        mData.clear();
        mData.add(new SimpleDateFormat("HH:mm:ss", new Locale("RU")).format(new Date(System.currentTimeMillis())));
        mData.add(String.valueOf(hashCode()));
        mData.add(String.valueOf(mWidgetID));
        for (int i = 3; i < 15; i++) {
            mData.add("Item " + i);
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rView = new RemoteViews(mContext.getPackageName(), R.layout.event);
        rView.setTextViewText(R.id.tvEventText, mData.get(position));

        // Добавим обработчик нажатия на элемент списка
        Intent clickIntent = new Intent();
        clickIntent.putExtra(CalendarWidgetProvider.ITEM_POSITION, position);
        rView.setOnClickFillInIntent(R.id.tvEventText, clickIntent);

        return rView;
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
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}

