package com.rightdirection.calendarwidget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.rightdirection.calendarwidget.POJOs.CalendarEvent;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class CalendarWidgetProvider extends AppWidgetProvider {

    private final static String ACTION_ITEM_ON_CLICK = "com.rightdirection.calendarwidget.itemonclick";
    private final static String ACTION_DATE_SHEET_ON_CLICK = "com.rightdirection.calendarwidget.datesheetonclick";
    private final static String ACTION_SETTINGS_ON_CLICK = "com.rightdirection.calendarwidget.settingsonclick";
    final static String EVENT = "event";
    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // На телефоне может быть установлено несколько активных виджетов, поэтому обновим их все
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, appWidgetManager.getAppWidgetInfo(appWidgetId).provider.getClassName());
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

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        updateAppWidget(context, appWidgetManager, appWidgetId, appWidgetManager.getAppWidgetInfo(appWidgetId).provider.getClassName());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        // Обрабатываем нажатие на элемент списка
        if (intent.getAction().equalsIgnoreCase(ACTION_ITEM_ON_CLICK)) {
            calendarItemOnClick(context, intent);
        }
        // Обрабатываем нажатие на дату календаря - отобразим на короткое время кнопку настоек
        else if (intent.getAction().equalsIgnoreCase(ACTION_DATE_SHEET_ON_CLICK)) {
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return;
            dateSheetOnClick(context, widgetId);
        }
        // Обрабатываем нажатие на кнопку Настройки
        else if (intent.getAction().equalsIgnoreCase(ACTION_SETTINGS_ON_CLICK)) {
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return;
            Intent confIntent = new Intent(context, CalendarWidgetConfigureActivity.class);
            confIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            confIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            context.startActivity(confIntent);
        }
    }

    private void dateSheetOnClick(Context context, final int widgetId) {
        final AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);

        // Отключим видимость списка
        final RemoteViews remoteViews = getRemoteViews(context,
                widgetManager.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT));
        remoteViews.setViewVisibility(R.id.events, GONE);
        // Покажем кнопку настройки
        remoteViews.setViewVisibility(R.id.rvSettings, VISIBLE);

        // Обновим отображение виджета
        widgetManager.updateAppWidget(widgetId, remoteViews);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            // Событие после прошествия времени задержки
            public void run() {
                // Включим видимость списка, уберем кнопку Настройка
                remoteViews.setViewVisibility(R.id.events, VISIBLE);
                remoteViews.setViewVisibility(R.id.rvSettings, GONE);
                widgetManager.updateAppWidget(widgetId, remoteViews);
                widgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.events);
            }
        }, 1100);
    }

    private void calendarItemOnClick(Context context, Intent intent) {
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

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String providerClassName) {

        //CharSequence widgetText = CalendarWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Создаем RemoteViews объекты
        // Получим минимальную высоту виджета
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        RemoteViews remoteViews = getRemoteViews(context, minHeight);
        //RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.calendar_widget);

        // Установим цвет заднего фона всего виджета
        remoteViews.setInt(R.id.calendar, "setBackgroundColor", CalendarWidgetConfigureActivity.loadPrefValue(context, appWidgetId, CalendarWidgetConfigureActivity.PREF_KEY_BACKGROUND_COLOR));

        setCalendarDataTexts(remoteViews);
        setEventsList(remoteViews, context, appWidgetId);
        try {
            setCalendarDataSheetClick(remoteViews, context, appWidgetId, providerClassName);
            setEventsListClick(remoteViews, context, providerClassName);
            setSettingsButtonClick(remoteViews, context, appWidgetId, providerClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Передаем сообщение менеджеру виджетов, что необходимо обновить виджеты
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.events);
    }

    /**
     * Determine appropriate view based on row or column provided.
     */
    private static RemoteViews getRemoteViews(Context context, int minHeight) {
        // Определим количество ячеек в высоту на основании размера виджета в dp.
        int rows = getCellsForSize(minHeight);
        // Измени полотно в зависимости от количества ячеек в высоту
        switch (rows) {
            case 1:  {
                //Toast.makeText(context, "1 в высоту.", Toast.LENGTH_SHORT).show();
                return new RemoteViews(context.getPackageName(), R.layout.calendar_widget);
            }
            default: {
                //Toast.makeText(context, "Больше 1 в высоту.", Toast.LENGTH_SHORT).show();
                return new RemoteViews(context.getPackageName(), R.layout.calendar_widget_expanded);
            }
        }
    }

    /**
     * Returns number of cells needed for given size of the widget.
     *
     * @param size Widget size in dp.
     * @return Size in number of cells.
     */
    private static int getCellsForSize(int size) {
        int n = 2;
        while (70 * n - 30 < size) {
            ++n;
        }
        return n - 1;
    }

    private static void setEventsList(RemoteViews remoteViews, Context context, int appWidgetId) {
        // Вызываем сервис для заполнения списка событий
        Intent intent = new Intent(context, CalendarWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // Добавим data к намерению, чтобы сделать его уникальным. Иначе второй и последующие добавленные виджеты будут использовать тоже самое намерение.
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        remoteViews.setRemoteAdapter(R.id.events, intent);
        remoteViews.setEmptyView(R.id.events, R.id.empty_view);
    }

    private static void setEventsListClick(RemoteViews remoteViews, Context context, String providerClassName) throws ClassNotFoundException {
        Intent listClickIntent = new Intent(context, getClassByName(providerClassName));
        listClickIntent.setAction(ACTION_ITEM_ON_CLICK);
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
        @SuppressLint("SimpleDateFormat") String month = new SimpleDateFormat("MMMM", rusDateFormatSymbols).format(currentDate);
        remoteViews.setTextViewText(R.id.calendar_month, month.toUpperCase());

        // Установим значения текстовых полей для развернутого виджета
        String fullData = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(currentDate);
        remoteViews.setTextViewText(R.id.calendar_full_data, fullData);
        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(currentDate);
        remoteViews.setTextViewText(R.id.calendar_day_of_week, dayOfWeek);
    }

    private static void setCalendarDataSheetClick(RemoteViews remoteViews, Context context, int appWidgetId, String providerClassName) throws ClassNotFoundException {
        // Привяжем событие обновления виджета при клике на дате календаря
        Intent updateIntent = new Intent(context, getClassByName(providerClassName));
        updateIntent.setAction(ACTION_DATE_SHEET_ON_CLICK);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent updatePIntent = PendingIntent.getBroadcast(context, appWidgetId, updateIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.calendar_sheet, updatePIntent);
    }

    private static Class<?> getClassByName(String providerClassName) throws ClassNotFoundException {
        return Class.forName(providerClassName);
    }

    private static void setSettingsButtonClick(RemoteViews remoteViews, Context context, int appWidgetId, String providerClassName) throws ClassNotFoundException {
        Intent btnSettingsClickIntent = new Intent(context, getClassByName(providerClassName));
        btnSettingsClickIntent.setAction(ACTION_SETTINGS_ON_CLICK);
        btnSettingsClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent btnSettingsClickPIntent = PendingIntent.getBroadcast(context, appWidgetId, btnSettingsClickIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.btnSettings, btnSettingsClickPIntent);
    }
}

