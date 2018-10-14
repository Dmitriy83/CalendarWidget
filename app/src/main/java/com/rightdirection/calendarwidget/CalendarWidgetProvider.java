package com.rightdirection.calendarwidget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
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

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class CalendarWidgetProvider extends AppWidgetProvider {

    private final static String ACTION_ITEM_ON_CLICK = "com.rightdirection.calendarwidget.itemonclick";
    private final static String ACTION_DATE_SHEET_ON_CLICK = "com.rightdirection.calendarwidget.datesheetonclick";
    private final static String ACTION_SETTINGS_ON_CLICK = "com.rightdirection.calendarwidget.settingsonclick";
    private final static String PROVIDER_CHANGED = "android.intent.action.PROVIDER_CHANGED";
    public final static String ACTION_GOOGLE_CALENDAR_CHANGED = "com.rightdirection.calendarwidget.googlecalendarchanged";
    final static String EVENT = "event";
    private final String TAG = this.getClass().getSimpleName();
    private static GoogleCalendarChangedReceiver mGoogleCalendarChangedReceiver;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // На телефоне может быть установлено несколько активных виджетов, поэтому обновим их все
        for (int appWidgetId : appWidgetIds) {
            String className;
            if (appWidgetManager != null && appWidgetManager.getAppWidgetInfo(appWidgetId) != null && appWidgetManager.getAppWidgetInfo(appWidgetId).provider != null){
                className = appWidgetManager.getAppWidgetInfo(appWidgetId).provider.getClassName();
            }else{
                continue;
            }
            updateAppWidget(context, appWidgetManager, appWidgetId, className);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Когда пользоатель удаляет виджет, удалим настройки, связанные с ним
        for (int appWidgetId : appWidgetIds) {
            CalendarWidgetConfigureActivity.deletePrefs(context, appWidgetId);
        }
    }

    /**
     Вызывается при создании первого виджета
     * @param context - контекст провайдера
     */
    @Override
    public void onEnabled(Context context) {
        // Зарегистрируем приемник намериний об изменении событий календаря
        registerGoogleCalendarChangedReceiver(context);
    }

    /**
     Вызывается после удаления последнего виджета
     * @param context - контекст провайдера
     */
    @Override
    public void onDisabled(Context context) {
        unregisterGoogleCalendarChangedReceiver(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        String className;
        if (appWidgetManager != null && appWidgetManager.getAppWidgetInfo(appWidgetId) != null && appWidgetManager.getAppWidgetInfo(appWidgetId).provider != null){
            className = appWidgetManager.getAppWidgetInfo(appWidgetId).provider.getClassName();
        }else{
            return;
        }
        updateAppWidget(context, appWidgetManager, appWidgetId, className);
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
        else if (intent.getAction().equalsIgnoreCase(ACTION_GOOGLE_CALENDAR_CHANGED) || intent.getAction().equalsIgnoreCase(PROVIDER_CHANGED)){
            // Обновим виджеты при изменении события календаря
            updateAllWidgets(context, new ComponentName(context, CalendarWidgetProvider.class));
            updateAllWidgets(context, new ComponentName(context, CalendarWidgetProvider4x4.class));
        }
    }

    private void registerGoogleCalendarChangedReceiver(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            unregisterGoogleCalendarChangedReceiver(context);
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_PROVIDER_CHANGED);
            filter.addDataScheme("content");
            filter.addDataAuthority("com.android.calendar", null);
            mGoogleCalendarChangedReceiver = new GoogleCalendarChangedReceiver();
            context.getApplicationContext().registerReceiver(mGoogleCalendarChangedReceiver, filter);
        }
    }

    private void unregisterGoogleCalendarChangedReceiver(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mGoogleCalendarChangedReceiver != null) {
            context.getApplicationContext().unregisterReceiver(mGoogleCalendarChangedReceiver);
        }
    }

    private void updateAllWidgets(Context context, ComponentName componentName){
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        for (int widgetId : widgetManager.getAppWidgetIds(componentName)) {
            widgetManager.updateAppWidget(widgetId, getRemoteViews(context,  widgetId));
            widgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.events);
        }
    }

    private void dateSheetOnClick(Context context, final int widgetId) {
        final AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);

        // Отключим видимость списка
        final RemoteViews remoteViews = getRemoteViews(context,  widgetId);
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
        // Зарегистрируем приемник перед открытием, чтобы после изменения события календаря обновился виджет.
        //  Долго этот приемник не просуществует (пока сборщик мусора не удалит класс из памяти)
        registerGoogleCalendarChangedReceiver(context);
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
        RemoteViews remoteViews = getRemoteViews(context, appWidgetId);
        //RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.calendar_widget);

        // Установим цвет заднего фона всего виджета
        remoteViews.setInt(R.id.calendar, "setBackgroundColor", CalendarWidgetConfigureActivity.loadPrefValue(context, appWidgetId, CalendarWidgetConfigureActivity.PREF_KEY_BACKGROUND_COLOR));

        // Скроем страничку календаря при необходимости
        if (CalendarWidgetConfigureActivity.loadPrefValue(context, appWidgetId, CalendarWidgetConfigureActivity.PREF_KEY_IS_SHOW_CALENDAR_PAGE) == 0) {
            remoteViews.setViewVisibility(R.id.calendar_month, GONE);
            remoteViews.setViewVisibility(R.id.calendar_date, GONE);
            remoteViews.setViewVisibility(R.id.calendar_full_data, GONE);
            remoteViews.setViewVisibility(R.id.calendar_day_of_week, GONE);
            remoteViews.setViewVisibility(R.id.calendar_sheet_settings, VISIBLE);
        } else{
            remoteViews.setViewVisibility(R.id.calendar_month, VISIBLE);
            remoteViews.setViewVisibility(R.id.calendar_date, VISIBLE);
            remoteViews.setViewVisibility(R.id.calendar_full_data, VISIBLE);
            remoteViews.setViewVisibility(R.id.calendar_day_of_week, VISIBLE);
            remoteViews.setViewVisibility(R.id.calendar_sheet_settings, GONE);
        }

        setCalendarDataTexts(remoteViews, context, appWidgetId);

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
    private static RemoteViews getRemoteViews(Context context, final int widgetId) {
        // Определим количество ячеек в высоту на основании размера виджета в dp.
        //int rows = getCellsForSize(minHeight);
        // Измени полотно в зависимости от количества ячеек в высоту
        /*switch (rows) {
            case 1:  {
                return new RemoteViews(context.getPackageName(), R.layout.calendar_widget);
            }
            default: {
                return new RemoteViews(context.getPackageName(), R.layout.calendar_widget_expanded);
            }
        }*/
        if (isCalendarWidgetExpanded(context, widgetId)) {
            return new RemoteViews(context.getPackageName(), R.layout.calendar_widget_expanded);
        } else{
            return new RemoteViews(context.getPackageName(), R.layout.calendar_widget);
        }
    }

    static boolean isCalendarWidgetExpanded(Context context, int widgetId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        String providerClassName = "";
        if (appWidgetManager != null && appWidgetManager.getAppWidgetInfo(widgetId) != null && appWidgetManager.getAppWidgetInfo(widgetId).provider != null){
            providerClassName = appWidgetManager.getAppWidgetInfo(widgetId).provider.getClassName();
        }
        return providerClassName.equals(CalendarWidgetProvider4x4.class.getName());
    }

    /**
     * Returns number of cells needed for given size of the widget.
     *
     * @param size Widget size in dp.
     * @return Size in number of cells.
     */
    @SuppressWarnings("unused")
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

    private static void setCalendarDataTexts(RemoteViews remoteViews, Context context, int appWidgetId) {
        Date currentDate = new Date(System.currentTimeMillis());
        // Устанавливаем тексты даты календаря
        String data = new SimpleDateFormat("d", Locale.getDefault()).format(currentDate);
        remoteViews.setTextViewText(R.id.calendar_date, data);

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

        // Установим размеры текстов и цвета
        int monthId;
        int dateId;
        if (CalendarWidgetProvider.isCalendarWidgetExpanded(context, appWidgetId)) {
            monthId = R.id.calendar_full_data;
            dateId = R.id.calendar_day_of_week;
        }else {
            monthId = R.id.calendar_month;
            dateId = R.id.calendar_date;
        }
        remoteViews.setTextViewTextSize(monthId, COMPLEX_UNIT_SP,
                CalendarWidgetConfigureActivity.loadPrefValue(context, appWidgetId, CalendarWidgetConfigureActivity.getPrefKeyMonthTextSize(context, appWidgetId)));
        remoteViews.setTextColor(monthId,
                CalendarWidgetConfigureActivity.loadPrefValue(context, appWidgetId, CalendarWidgetConfigureActivity.getPrefKeyMonthTextColor(context, appWidgetId)));
        remoteViews.setTextViewTextSize(dateId, COMPLEX_UNIT_SP,
                CalendarWidgetConfigureActivity.loadPrefValue(context, appWidgetId, CalendarWidgetConfigureActivity.getPrefKeyDateTextSize(context, appWidgetId)));
        remoteViews.setTextColor(dateId,
                CalendarWidgetConfigureActivity.loadPrefValue(context, appWidgetId, CalendarWidgetConfigureActivity.getPrefKeyDateTextColor(context, appWidgetId)));
        remoteViews.setInt(dateId, "setBackgroundColor",
                CalendarWidgetConfigureActivity.loadPrefValue(context, appWidgetId, CalendarWidgetConfigureActivity.getPrefKeyDateBackgroundColor(context, appWidgetId)));
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

