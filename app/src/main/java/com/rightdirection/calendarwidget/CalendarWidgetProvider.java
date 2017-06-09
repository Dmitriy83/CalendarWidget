package com.rightdirection.calendarwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarWidgetProvider extends AppWidgetProvider {

    final static String ACTION_ON_CLICK = "com.rightdirection.calendarwidget.itemonclick";
    final static String ITEM_POSITION = "item_position";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        //CharSequence widgetText = CalendarWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Создаем RemoteViews объекты
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.calendar_widget);

        setCalendarDataTexts(remoteViews);
        setCalendarDataSheetClick(remoteViews, context, appWidgetId);
        setEventsList(remoteViews, context, appWidgetId);
        setEventsListClick(remoteViews, context);

        // Передаем сообщение менеджеру виджетов, что необходимо обновить виджеты
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.events);
    }

    static void setEventsList(RemoteViews remoteViews, Context context, int appWidgetId){
        // Вызываем сервис для заполнения списка событий
        Intent intent = new Intent(context, CalendarWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // Добавим data к намерению, чтобы сделать его уникальным. Иначе второй и последующие добавленные виджеты будут использовать тоже самое намерение.
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        remoteViews.setRemoteAdapter(R.id.events, intent);
        remoteViews.setEmptyView(R.id.events, R.id.empty_view);
    }

    static void setEventsListClick(RemoteViews remoteViews, Context context){
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
            int itemPos = intent.getIntExtra(ITEM_POSITION, -1);
            if (itemPos != -1) {
                Toast.makeText(context, "Clicked on item " + itemPos,
                        Toast.LENGTH_SHORT).show();
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
            CalendarWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
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

