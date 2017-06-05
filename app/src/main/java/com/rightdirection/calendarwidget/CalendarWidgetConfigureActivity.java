package com.rightdirection.calendarwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * Экран настроек для виджета {@link CalendarWidgetProvider CalendarWidgetProvider}.
 */
public class CalendarWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "com.rightdirection.calendarwidget.CalendarWidgetProvider";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    EditText mAppWidgetText;

    public CalendarWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Считываем преффикс для конкретного виджета из настроек приложения.
    // Если преффикс не был сохранен, получаем преффикс по умолчанию из ресурсов
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Присваиваем значению результата - CANCELED, на случай, если пользователь нажмет кнопку Нахад.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.calendar_widget_configure);
        mAppWidgetText = (EditText) findViewById(R.id.calendar_data);
        findViewById(R.id.add_button).setOnClickListener(mOnAddClickListener);

        // Находим виджет id из намерения.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // Если эта активность была запущена с помощью намерения без установленного виджет ID, завершаем работу с ошибкой.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        mAppWidgetText.setText(loadTitlePref(CalendarWidgetConfigureActivity.this, mAppWidgetId));
    }

    View.OnClickListener mOnAddClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = CalendarWidgetConfigureActivity.this;

            // Сохраняем преффикс в настройках
            String widgetText = mAppWidgetText.getText().toString();
            saveTitlePref(context, mAppWidgetId, widgetText);

            // В зону ответственности активности настроек входит обновление виджета на экране
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            CalendarWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Обязательно возвращаем виджет id
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };
}

