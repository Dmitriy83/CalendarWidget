package com.rightdirection.calendarwidget;

import android.*;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.onegravity.colorpicker.ColorPickerDialog;
import com.onegravity.colorpicker.ColorPickerListener;
import com.onegravity.colorpicker.SetColorPickerListenerEvent;

import java.util.HashMap;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Экран настроек для виджета {@link CalendarWidgetProvider CalendarWidgetProvider}.
 */
public class CalendarWidgetConfigureActivity extends Activity implements EasyPermissions.PermissionCallbacks {

    private static final String PREFS_NAME = "com.rightdirection.calendarwidget.CalendarWidgetProvider";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    public static final String PREF_KEY_TEXT_COLOR = "text_color";
    public static final String PREF_KEY_BACKGROUND_COLOR = "background_color";
    public static final String PREF_KEY_TEXT_SIZE = "text_size";
    public static final String PREF_KEY_TODAY_TEXT_COLOR = "today_text_color";
    public static final String PREF_KEY_NUMBER_OF_EVENTS_DISPLAYED = "number_of_events_displayed";
    private static final int REQUEST_READ_CALENDAR = 297;
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Context mContext;
    private final String TAG = this.getClass().getSimpleName();
    private static final HashMap<String,Integer> defaultValues = getDefaultValues();
    private static HashMap<String,Integer> getDefaultValues(){
        HashMap<String,Integer> defaultValues = new HashMap<>();
        defaultValues.put(PREF_KEY_TEXT_COLOR, Color.WHITE);
        defaultValues.put(PREF_KEY_BACKGROUND_COLOR, Color.TRANSPARENT);
        defaultValues.put(PREF_KEY_TEXT_SIZE, 15);
        defaultValues.put(PREF_KEY_TODAY_TEXT_COLOR, Color.YELLOW);
        defaultValues.put(PREF_KEY_NUMBER_OF_EVENTS_DISPLAYED, 10);
        return  defaultValues;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Находим виджет id из намерения.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // Если эта активность была запущена с помощью намерения без установленного виджет ID, завершаем работу с ошибкой.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        // Присваиваем значению результата - CANCELED, на случай, если пользователь нажмет кнопку Нахад.
        setResult(RESULT_CANCELED);

        mContext = this;

        // Наполняем экран из layout
        setContentView(R.layout.calendar_widget_configure);

        setAddUpgradeButtonParamsAndListener();
        setTextSizeParamsAndListener();
        setTextColorParamsAndListener();
        setTodayTextColorParamsAndListener();
        setBackgroundColorParamsAndListener();
        setNumberOfEventsDisplayedParamsAndListener();

        // Проверим/запросим разрешения
        requestCalendarPermissions();
    }

    private void requestCalendarPermissions(){
        String[] perms = {android.Manifest.permission.READ_CALENDAR};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            // Получим разрешения
            EasyPermissions.requestPermissions(this, getString(R.string.read_calendar_rationale),
                    REQUEST_READ_CALENDAR, perms);
        }
    }

    private void setBackgroundColorParamsAndListener() {
        ImageView imgBackgroundColor = findViewById(R.id.img_background_color);
        imgBackgroundColor.setBackgroundColor(loadPrefValue(this, mAppWidgetId, PREF_KEY_BACKGROUND_COLOR));
        imgBackgroundColor.invalidate();
        RelativeLayout rlBackgroundColor = findViewById(R.id.rl_background_color);
        rlBackgroundColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int dialogId = new ColorPickerDialog(mContext,
                        loadPrefValue(mContext, mAppWidgetId, PREF_KEY_BACKGROUND_COLOR), true).show();
                SetColorPickerListenerEvent.setListener(dialogId, new ColorPickerListener() {
                    @Override
                    public void onDialogClosing() {
                        // Ничего не делаем
                    }
                    @Override
                    public void onColorChanged(int color) {
                        ImageView imgBackgroundColor = findViewById(R.id.img_background_color);
                        imgBackgroundColor.setBackgroundColor(color);
                        imgBackgroundColor.invalidate();
                    }
                });
            }
        });
    }

    private void setTextColorParamsAndListener() {
        ImageView imgTextColor = findViewById(R.id.img_text_color);
        imgTextColor.setBackgroundColor(loadPrefValue(mContext, mAppWidgetId, PREF_KEY_TEXT_COLOR));
        imgTextColor.invalidate();
        RelativeLayout rlTextColor = findViewById(R.id.rl_text_color);
        rlTextColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int dialogId = new ColorPickerDialog(mContext,
                        loadPrefValue(mContext, mAppWidgetId, PREF_KEY_TEXT_COLOR), true).show();
                SetColorPickerListenerEvent.setListener(dialogId, new ColorPickerListener() {
                    @Override
                    public void onDialogClosing() {
                        // Ничего не делаем
                    }
                    @Override
                    public void onColorChanged(int color) {
                        ImageView imgTextColor = findViewById(R.id.img_text_color);
                        imgTextColor.setBackgroundColor(color);
                        imgTextColor.invalidate();
                    }
                });
            }
        });
    }

    private void setTodayTextColorParamsAndListener() {
        ImageView imgTodayTextColor = findViewById(R.id.img_today_text_color);
        imgTodayTextColor.setBackgroundColor(loadPrefValue(mContext, mAppWidgetId, PREF_KEY_TODAY_TEXT_COLOR));
        imgTodayTextColor.invalidate();
        RelativeLayout rlTodayTextColor = findViewById(R.id.rl_today_text_color);
        rlTodayTextColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int dialogId = new ColorPickerDialog(mContext,
                        loadPrefValue(mContext, mAppWidgetId, PREF_KEY_TODAY_TEXT_COLOR), true).show();
                SetColorPickerListenerEvent.setListener(dialogId, new ColorPickerListener() {
                    @Override
                    public void onDialogClosing() {
                        // Ничего не делаем
                    }
                    @Override
                    public void onColorChanged(int color) {
                        ImageView imgTodayTextColor = findViewById(R.id.img_today_text_color);
                        imgTodayTextColor.setBackgroundColor(color);
                        imgTodayTextColor.invalidate();
                    }
                });
            }
        });
    }

    private void setTextSizeParamsAndListener() {
        EditText etTextSize = findViewById(R.id.et_text_size);
        etTextSize.setText(String.valueOf(loadPrefValue(this, mAppWidgetId, PREF_KEY_TEXT_SIZE)));
    }

    private void setNumberOfEventsDisplayedParamsAndListener() {
        EditText etNumberOfEvents = findViewById(R.id.et_number_of_events_displayed);
        etNumberOfEvents.setText(String.valueOf(loadPrefValue(this, mAppWidgetId, PREF_KEY_NUMBER_OF_EVENTS_DISPLAYED)));
    }

    private void setAddUpgradeButtonParamsAndListener() {
        Button addUpgradeButton = findViewById(R.id.add_upgrade_button);
        if (isItFirstConfiguration()){
            addUpgradeButton.setText(getString(R.string.add_widget));
        }
        addUpgradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUpgradeButtonOnClick();

            }
        });
    }

    private void addUpgradeButtonOnClick() {
        // Сохраняем количество отображаемых событий в настройках
        EditText etNumberOfEvents = findViewById(R.id.et_number_of_events_displayed);
        int numberOfEvents = defaultValues.get(PREF_KEY_NUMBER_OF_EVENTS_DISPLAYED);
        try {
            numberOfEvents = Integer.parseInt(etNumberOfEvents.getText().toString());
        }catch(Exception e){
            Log.e(TAG, e.toString());
        }
        savePrefValue(mAppWidgetId, PREF_KEY_NUMBER_OF_EVENTS_DISPLAYED, numberOfEvents);

        // Сохраняем размер текста в настройках
        EditText etTextSize = findViewById(R.id.et_text_size);
        int textSize = defaultValues.get(PREF_KEY_TEXT_SIZE);
        try {
            textSize = Integer.parseInt(etTextSize.getText().toString());
        }catch(Exception e){
            Log.e(TAG, e.toString());
        }
        savePrefValue(mAppWidgetId, PREF_KEY_TEXT_SIZE, textSize);

        // Сохраняем цвет текста в настройках
        ImageView imgTextColor = findViewById(R.id.img_text_color);
        ColorDrawable textColorDrawable = (ColorDrawable) imgTextColor.getBackground();
        if (textColorDrawable != null) savePrefValue(mAppWidgetId, PREF_KEY_TEXT_COLOR, textColorDrawable.getColor());

        // Сохраняем цвет текста события сегодняшнего дня в настройках
        ImageView imgTodayTextColor = findViewById(R.id.img_today_text_color);
        ColorDrawable todayTextColorDrawable = (ColorDrawable) imgTodayTextColor.getBackground();
        if (todayTextColorDrawable != null) savePrefValue(mAppWidgetId, PREF_KEY_TODAY_TEXT_COLOR, todayTextColorDrawable.getColor());

        // Сохраняем цвет фона в настройках
        ImageView imgBackgroundColor = findViewById(R.id.img_background_color);
        ColorDrawable backgroundColorDrawable = (ColorDrawable) imgBackgroundColor.getBackground();
        if (backgroundColorDrawable != null) savePrefValue(mAppWidgetId, PREF_KEY_BACKGROUND_COLOR, backgroundColorDrawable.getColor());

        // В зону ответственности активности настроек входит обновление виджета на экране
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        String providerClassName = appWidgetManager.getAppWidgetInfo(mAppWidgetId).provider.getClassName();
        if (providerClassName.equals(CalendarWidgetProvider.class.getName())) {
            CalendarWidgetProvider.updateAppWidget(mContext, appWidgetManager, mAppWidgetId, providerClassName);
        } else{
            CalendarWidgetProvider4x4.updateAppWidget(mContext, appWidgetManager, mAppWidgetId, providerClassName);
        }

        // Обязательно возвращаем виджет id
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    static void deletePrefs(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId + PREF_KEY_TEXT_COLOR);
        prefs.apply();
    }

    private boolean isItFirstConfiguration(){
        final int wrongTextColor = -100;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int textColor = prefs.getInt(PREF_PREFIX_KEY + mAppWidgetId + PREF_KEY_TEXT_COLOR, wrongTextColor);
        return (textColor == wrongTextColor);
    }

    private void savePrefValue(int appWidgetId, String prefName, int value) {
        SharedPreferences.Editor prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId + prefName, value);
        prefs.apply();
    }

    public static int loadPrefValue(Context context, int appWidgetId, String prefName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int defaultValue = defaultValues.get(prefName);
        return prefs.getInt(PREF_PREFIX_KEY + appWidgetId + prefName, defaultValue);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        // Ничего не делаем
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // Выводим сообщение, что события календаря не будут отображаться
        if (requestCode == REQUEST_READ_CALENDAR){
            if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                // Пользователь отклонил разрешения с опцией "БОЛЬШЕ НЕ СПРАШИВАТЬ."
                Toast.makeText(this, R.string.on_permissions_denied_permanently_msg, Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, R.string.on_permissions_denied_msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Передадим обработку результов в EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}

