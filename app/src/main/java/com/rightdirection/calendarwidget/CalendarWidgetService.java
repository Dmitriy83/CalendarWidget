package com.rightdirection.calendarwidget;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    private static final int COUNT_OF_DISPLAYING_EVENTS = 10;
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
        showTestEvents();
        //showEvents();
    }

    private void showTestEvents() {
        mData.clear();
        for (int i = 1; i <= 15; i++){
            mData.add("Date " + i + " Event " + i);
        }
    }

    private void showEvents(){
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        long startTimeInMillis = Calendar.getInstance().getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.MONTH, 3);
        long entTimeInMillis = endTime.getTimeInMillis();

        String[] projection = new String[]{CalendarContract.Instances.DTSTART, CalendarContract.Instances.TITLE};
        final int EVENT_START_TIME_INDEX = 0;
        final int EVENT_TITLE_INDEX = 1;
        String selection = CalendarContract.Instances.BEGIN + " >= " + startTimeInMillis + " and "
                + CalendarContract.Instances.BEGIN + " <= " + entTimeInMillis + " and "
                + CalendarContract.Instances.VISIBLE + " = 1";
        String sortOrder = CalendarContract.Instances.BEGIN;

        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(eventsUriBuilder, startTimeInMillis);
        ContentUris.appendId(eventsUriBuilder, entTimeInMillis);
        Uri eventsUri = eventsUriBuilder.build();
        Cursor cursor = mContext.getContentResolver().query(eventsUri, projection, selection, null, sortOrder);

        mData.clear();
        if (cursor == null) return;
        int i = 1;
        while (cursor.moveToNext() && i <= COUNT_OF_DISPLAYING_EVENTS) {
            mData.add(getDateString(cursor.getLong(EVENT_START_TIME_INDEX)) + " " + String.valueOf(cursor.getString(EVENT_TITLE_INDEX)));
            Log.d("showEvents", getDateString(cursor.getLong(EVENT_START_TIME_INDEX)));
            i++;
        }
        cursor.close();
    }

    private String getDateString(long timeInMillis) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMillis);
        date = getStartOfDay(date);

        Calendar today = getStartOfDay(Calendar.getInstance());
        if (today.equals(date)){
            return mContext.getString(R.string.today);
        }

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);
        tomorrow = getStartOfDay(tomorrow);
        if (tomorrow.equals(date)){
            return mContext.getString(R.string.tomorrow);
        }

        return new SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(date.getTime());
    }

    private Calendar getStartOfDay(Calendar time){
        time.set(Calendar.HOUR_OF_DAY, 0);
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        return time;
    }

    private static Calendar toCalendar(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
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

