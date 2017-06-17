package com.rightdirection.calendarwidget;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.rightdirection.calendarwidget.POJOs.CalendarEvent;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CalendarWidgetProvider extends AppWidgetProvider {

    private final static String ACTION_ON_CLICK = "com.rightdirection.calendarwidget.itemonclick";
    final static String EVENT = "event";
    private final String TAG = this.getClass().getSimpleName();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        //CharSequence widgetText = CalendarWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Создаем RemoteViews объекты
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.calendar_widget);

        // Установим цвет заднего фона всего виджета
        remoteViews.setInt(R.id.calendar, "setBackgroundColor", CalendarWidgetConfigureActivity.loadBackgroundColorBackgroundColorPref(context, appWidgetId));

        setCalendarDataTexts(remoteViews);
        setCalendarDataSheetClick(remoteViews, context, appWidgetId);
        setEventsList(remoteViews, context, appWidgetId);
        setEventsListClick(remoteViews, context);

        // Передаем сообщение менеджеру виджетов, что необходимо обновить виджеты
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.events);
    }

    private static void setEventsList(RemoteViews remoteViews, Context context, int appWidgetId){
        // Вызываем сервис для заполнения списка событий
        Intent intent = new Intent(context, CalendarWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // Добавим data к намерению, чтобы сделать его уникальным. Иначе второй и последующие добавленные виджеты будут использовать тоже самое намерение.
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        remoteViews.setRemoteAdapter(R.id.events, intent);
        remoteViews.setEmptyView(R.id.events, R.id.empty_view);
    }

    private static void setEventsListClick(RemoteViews remoteViews, Context context){
        Intent listClickIntent = new Intent(context, CalendarWidgetProvider.class);
        listClickIntent.setAction(ACTION_ON_CLICK);
        PendingIntent listClickPIntent = PendingIntent.getBroadcast(context, 0, listClickIntent, 0);
        remoteViews.setPendingIntentTemplate(R.id.events, listClickPIntent);
    }

    private static void setCalendarDataTexts(RemoteViews remoteViews) {
        Date currentDate = new Date(System.currentTimeMillis());
        // Устанавливаем тексты даты календаря
        String data = new SimpleDateFormat("d", Locale.getDefault()).format(currentDate);
        remoteViews.setTextViewText(R.id.calendar_data, data);

        DateFormatSymbols rusDateFormatSymbols = new DateFormatSymbols();
        if (Locale.getDefault().getLanguage().equals(new Locale("ru").getLanguage())) {
            rusDateFormatSymbols.setMonths(new String[]{"янв", "февр", "март", "апр", "май", "июнь", "июль", "авг", "сент", "окт", "нояб", "дек"});
        }else{
            rusDateFormatSymbols.setMonths(new String[]{"jan", "feb", "mar", "apr", "may", "june", "july", "aug", "sept", "oct", "nov", "dec"});
        }
        String month = new SimpleDateFormat("MMMM", rusDateFormatSymbols).format(currentDate);
        remoteViews.setTextViewText(R.id.calendar_month, month.toUpperCase());
    }

    private static void setCalendarDataSheetClick(RemoteViews remoteViews, Context context, int appWidgetId){
        // Привяжем событие обновления виджета при клике на дате календаря
        Intent updateIntent = new Intent(context, CalendarWidgetProvider.class);
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId });
        PendingIntent updatePIntent = PendingIntent.getBroadcast(context, appWidgetId, updateIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.calendar_sheet, updatePIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        // Обрабатываем нажатие на элемент списка
        if (intent.getAction().equalsIgnoreCase(ACTION_ON_CLICK)) {
            CalendarEvent event = intent.getParcelableExtra(EVENT);
            if (event != null) {
                // Откроем событие
                Intent eventIntent = new Intent(Intent.ACTION_VIEW);
                eventIntent.setData(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.getId()));
                eventIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getStartTime());
                eventIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.getEndTime());
                eventIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    context.startActivity(eventIntent);
                } catch (Exception e) {
                    if (e instanceof ActivityNotFoundException) {
                        Toast.makeText(context, R.string.calendar_activity_not_found, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // На телефоне может быть установлено несколько активных виджетов, поэтому обновим их все
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Когда пользоатель удаляет виджет, удаим настройки, связанные с ним
        for (int appWidgetId : appWidgetIds) {
            CalendarWidgetConfigureActivity.deletePrefs(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Добавить функциональность при создании первого виджета
    }

    @Override
    public void onDisabled(Context context) {
        // Добавить функциональность после отображения последнего виджета
    }
}

