package com.rightdirection.calendarwidget;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.rightdirection.calendarwidget.POJOs.CalendarEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

/**
 * Сервис для отображения списка календаря. Его вызывает ListAdapter.
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
    //private final String TAG = this.getClass().getSimpleName();
    private ArrayList<CalendarEvent> mData;
    private final Context mContext;
    private final int mWidgetID;
    private int mTextColor;
    private int mTextSize;
    private int mNumberOfEventsDisplayed;
    private int mTodayTextColor;

    CalendarWidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mWidgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        mData = new ArrayList<>();
    }

    private static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    @Override
    public void onDataSetChanged() {
        mTextColor = CalendarWidgetConfigureActivity.loadPrefValue(mContext, mWidgetID, CalendarWidgetConfigureActivity.PREF_KEY_TEXT_COLOR);
        mTextSize = CalendarWidgetConfigureActivity.loadPrefValue(mContext, mWidgetID, CalendarWidgetConfigureActivity.PREF_KEY_EVENT_TEXT_SIZE);
        mTodayTextColor = CalendarWidgetConfigureActivity.loadPrefValue(mContext, mWidgetID, CalendarWidgetConfigureActivity.PREF_KEY_TODAY_TEXT_COLOR);
        mNumberOfEventsDisplayed = CalendarWidgetConfigureActivity.loadPrefValue(mContext, mWidgetID, CalendarWidgetConfigureActivity.PREF_KEY_NUMBER_OF_EVENTS_DISPLAYED);

        if (!isEmulator()){
            updateEvents();
        } else{
            updateTestEvents();
        }
    }

    private void updateTestEvents() {
        mData.clear();
        ArrayList<CalendarEvent> testEvents = getTestEventsList();
        mData.addAll(testEvents);
    }

    private ArrayList<CalendarEvent> getTestEventsList() {
        ArrayList<CalendarEvent> testEvents = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        if (Locale.getDefault().getLanguage().equals(new Locale("ru").getLanguage())) {
            testEvents.add(new CalendarEvent(calendar.getTimeInMillis(), calendar.getTimeInMillis(), "Позвони родителям", 1));
            calendar.add(Calendar.DATE,1);
            calendar.add(Calendar.HOUR,3);
            testEvents.add(new CalendarEvent(calendar.getTimeInMillis(), calendar.getTimeInMillis(), "День рождения Маргарита", 1));
            calendar.add(Calendar.DATE,5);
            calendar.add(Calendar.HOUR,3);
            testEvents.add(new CalendarEvent(calendar.getTimeInMillis(), calendar.getTimeInMillis(), "Презентация", 1));
            calendar.add(Calendar.DATE,7);
            calendar.add(Calendar.HOUR,3);
            testEvents.add(new CalendarEvent(calendar.getTimeInMillis(), calendar.getTimeInMillis(), "Долг 10000", 1));
            calendar.add(Calendar.DATE,15);
            calendar.add(Calendar.HOUR,3);
            testEvents.add(new CalendarEvent(calendar.getTimeInMillis(), calendar.getTimeInMillis(), "День рождения Дмитрий Александрович", 1));
            calendar.add(Calendar.DATE,2);
            testEvents.add(new CalendarEvent(calendar.getTimeInMillis(), calendar.getTimeInMillis(), "День рождения Кирилл", 1));
            calendar.add(Calendar.DATE,12);
            testEvents.add(new CalendarEvent(calendar.getTimeInMillis(), calendar.getTimeInMillis(), "День рождения Отец", 1));
            calendar.add(Calendar.DATE,20);
            testEvents.add(new CalendarEvent(calendar.getTimeInMillis(), calendar.getTimeInMillis(), "День рождения Соня", 1));
        } else{
            testEvents.add(new CalendarEvent(calendar.getTimeInMillis(), calendar.getTimeInMillis(), "Call parents", 1));
            calendar.add(Calendar.DATE,1);
            calendar.add(Calendar.HOUR, 3);
            testEvents.add(new CalendarEvent(calendar.getTimeInMillis(), calendar.getTimeInMillis(), "Birthday Margo", 1));
            calendar.add(Calendar.DATE,5);
            calendar.add(Calendar.HOUR,3);
            testEvents.add(new CalendarEvent(calendar.getTimeInMillis(), calendar.getTimeInMillis(), "Presentation", 1));
            calendar.add(Calendar.DATE,7);
            calendar.add(Calendar.HOUR,3);
            testEvents.add(new CalendarEvent(calendar.getTimeInMillis(), calendar.getTimeInMillis(), "Debt 10000", 1));
            calendar.add(Calendar.DATE,15);
            calendar.add(Calendar.HOUR,3);
            testEvents.add(new CalendarEvent(calendar.getTimeInMillis(), calendar.getTimeInMillis(), "Birthday Dmitri", 1));
            calendar.add(Calendar.DATE,2);
            testEvents.add(new CalendarEvent(calendar.getTimeInMillis(), calendar.getTimeInMillis(), "Birthday Jim", 1));
            calendar.add(Calendar.DATE,12);
            testEvents.add(new CalendarEvent(calendar.getTimeInMillis(), calendar.getTimeInMillis(), "Birthday Dad", 1));
            calendar.add(Calendar.DATE,20);
            testEvents.add(new CalendarEvent(calendar.getTimeInMillis(), calendar.getTimeInMillis(), "Birthday Sofia", 1));
        }

        return testEvents;
    }

    private void updateEvents() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        long startTimeInMillis = Calendar.getInstance().getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.MONTH, 12); // Считываем события за год вперед
        long entTimeInMillis = endTime.getTimeInMillis();

        String[] projection = new String[]{CalendarContract.Instances.BEGIN, CalendarContract.Instances.END,
                CalendarContract.Instances.TITLE, CalendarContract.Instances.EVENT_ID};
        final int EVENT_START_TIME_INDEX = 0;
        final int EVENT_END_TIME_INDEX = 1;
        final int EVENT_TITLE_INDEX = 2;
        final int EVENT_ID_INDEX = 3;
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
        while (cursor.moveToNext() && i <= mNumberOfEventsDisplayed) {
            mData.add(new CalendarEvent(cursor.getLong(EVENT_START_TIME_INDEX), cursor.getLong(EVENT_END_TIME_INDEX),  cursor.getString(EVENT_TITLE_INDEX), cursor.getLong(EVENT_ID_INDEX)));
            i++;
        }
        cursor.close();
    }

    private String getDateString(long timeInMillis) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMillis);

        Calendar dateStartOfDay = Calendar.getInstance();
        dateStartOfDay.setTimeInMillis(timeInMillis);
        getStartOfDay(dateStartOfDay);

        Calendar today = getStartOfDay(Calendar.getInstance());
        if (today.equals(dateStartOfDay)){
            return mContext.getString(R.string.today) + new SimpleDateFormat(" HH:mm", Locale.getDefault()).format(date.getTime());
        }

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);
        tomorrow = getStartOfDay(tomorrow);
        if (tomorrow.equals(dateStartOfDay)){
            return mContext.getString(R.string.tomorrow) + new SimpleDateFormat(" HH:mm", Locale.getDefault()).format(date.getTime());
        }

        return new SimpleDateFormat("d MMMM HH:mm", Locale.getDefault()).format(date.getTime());
    }

    private Calendar getStartOfDay(Calendar time){
        time.set(Calendar.HOUR_OF_DAY, 0);
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        return time;
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
        CalendarEvent event = mData.get(position);
        if (event == null) return null;

        RemoteViews rView = new RemoteViews(mContext.getPackageName(), R.layout.event);

        rView.setTextViewText(R.id.tvEventText, getDateString(event.getStartTime()) + " " + event.getTitle());
        if (isToday(event.getStartTime())) {
            rView.setTextColor(R.id.tvEventText, mTodayTextColor);
        }else{
            rView.setTextColor(R.id.tvEventText, mTextColor);
        }

        rView.setTextViewTextSize(R.id.tvEventText, COMPLEX_UNIT_SP, mTextSize);

        // Добавим обработчик нажатия на элемент списка
        Intent clickIntent = new Intent();
        clickIntent.putExtra(CalendarWidgetProvider.EVENT, event);
        rView.setOnClickFillInIntent(R.id.tvEventText, clickIntent);

        return rView;
    }

    private boolean isToday(long time) {
        Calendar startOfDay = Calendar.getInstance();
        startOfDay.setTimeInMillis(time);
        getStartOfDay(startOfDay);

        Calendar today = getStartOfDay(Calendar.getInstance());
        return today.equals(startOfDay);
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

