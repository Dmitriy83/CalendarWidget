package com.rightdirection.calendarwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
    public static final String PREF_KEY_EVENT_TEXT_SIZE = "text_size";
    public static final String PREF_KEY_TODAY_TEXT_COLOR = "today_text_color";
    public static final String PREF_KEY_NUMBER_OF_EVENTS_DISPLAYED = "number_of_events_displayed";
    public static final String PREF_KEY_IS_SHOW_CALENDAR_PAGE = "is_show_calendar_page";

    public static final String PREF_KEY_MONTH_TEXT_SIZE = "month_text_size";
    public static final String PREF_KEY_MONTH_TEXT_COLOR = "month_text_color";
    public static final String PREF_KEY_DATE_TEXT_SIZE = "date_text_size";
    public static final String PREF_KEY_DATE_TEXT_COLOR = "date_text_color";
    public static final String PREF_KEY_DATE_BACKGROUND_COLOR = "date_background_color";
    public static final String PREF_KEY_MONTH_TEXT_SIZE_4X4 = "month_text_size_4x4";
    public static final String PREF_KEY_MONTH_TEXT_COLOR_4X4 = "month_text_color_4x4";
    public static final String PREF_KEY_DATE_TEXT_SIZE_4X4 = "date_text_size_4x4";
    public static final String PREF_KEY_DATE_TEXT_COLOR_4X4 = "date_text_color_4x4";
    public static final String PREF_KEY_DATE_BACKGROUND_COLOR_4X4 = "date_background_color_4x4";

    private static final int REQUEST_READ_CALENDAR = 297;
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Context mContext;
    private final String TAG = this.getClass().getSimpleName();
    private static final HashMap<String,Integer> defaultValues = getDefaultValues();
    private static HashMap<String,Integer> getDefaultValues(){
        HashMap<String,Integer> defaultValues = new HashMap<>();
        defaultValues.put(PREF_KEY_TEXT_COLOR, Color.WHITE);
        defaultValues.put(PREF_KEY_BACKGROUND_COLOR, Color.TRANSPARENT);
        defaultValues.put(PREF_KEY_EVENT_TEXT_SIZE, 15);
        defaultValues.put(PREF_KEY_TODAY_TEXT_COLOR, Color.YELLOW);
        defaultValues.put(PREF_KEY_NUMBER_OF_EVENTS_DISPLAYED, 10);
        defaultValues.put(PREF_KEY_IS_SHOW_CALENDAR_PAGE, 1);
        addAdditionalDefaultValues(defaultValues);

        return  defaultValues;
    }

    public static void addAdditionalDefaultValues(HashMap<String,Integer> defaultValues){
        defaultValues.put(PREF_KEY_MONTH_TEXT_SIZE, 19);
        defaultValues.put(PREF_KEY_MONTH_TEXT_COLOR, Color.parseColor("#ffffff"));
        defaultValues.put(PREF_KEY_DATE_TEXT_SIZE, 32);
        defaultValues.put(PREF_KEY_DATE_TEXT_COLOR, Color.parseColor("#ffffff"));
        defaultValues.put(PREF_KEY_DATE_BACKGROUND_COLOR, Color.parseColor("#addae9"));

        defaultValues.put(PREF_KEY_MONTH_TEXT_SIZE_4X4, 22);
        defaultValues.put(PREF_KEY_MONTH_TEXT_COLOR_4X4, Color.parseColor("#ffffff"));
        defaultValues.put(PREF_KEY_DATE_TEXT_SIZE_4X4, 16);
        defaultValues.put(PREF_KEY_DATE_TEXT_COLOR_4X4, Color.parseColor("#ffffff"));
        defaultValues.put(PREF_KEY_DATE_BACKGROUND_COLOR_4X4, Color.parseColor("#e66551"));
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
        setEventTextSizeParamsAndListener();
        setTextColorParamsAndListener();
        setTodayTextColorParamsAndListener();
        setBackgroundColorParamsAndListener();
        setNumberOfEventsDisplayedParamsAndListener();
        setIsShowCalendarSheetParamsAndListener();
        setMonthTextSizeParamsAndListener();
        setMonthTextColorParamsAndListener();
        setDateTextSizeParamsAndListener();
        setDateTextColorParamsAndListener();
        setDateBackgroundColorParamsAndListener();

        // Проверим/запросим разрешения
        requestCalendarPermissions();
    }

    public static String getPrefKeyMonthTextSize(Context context, int appWidgetId){
        if (CalendarWidgetProvider.isCalendarWidgetExpanded(context, appWidgetId)) {
            return PREF_KEY_MONTH_TEXT_SIZE_4X4;
        }else {
            return PREF_KEY_MONTH_TEXT_SIZE;
        }
    }

    public static String getPrefKeyMonthTextColor(Context context, int appWidgetId){
        if (CalendarWidgetProvider.isCalendarWidgetExpanded(context, appWidgetId)) {
            return PREF_KEY_MONTH_TEXT_COLOR_4X4;
        }else {
            return PREF_KEY_MONTH_TEXT_COLOR;
        }
    }

    public static String getPrefKeyDateTextSize(Context context, int appWidgetId){
        if (CalendarWidgetProvider.isCalendarWidgetExpanded(context, appWidgetId)) {
            return PREF_KEY_DATE_TEXT_SIZE_4X4;
        }else {
            return PREF_KEY_DATE_TEXT_SIZE;
        }
    }

    public static String getPrefKeyDateTextColor(Context context, int appWidgetId){
        if (CalendarWidgetProvider.isCalendarWidgetExpanded(context, appWidgetId)) {
            return PREF_KEY_DATE_TEXT_COLOR_4X4;
        }else {
            return PREF_KEY_DATE_TEXT_COLOR;
        }
    }

    public static String getPrefKeyDateBackgroundColor(Context context, int appWidgetId){
        if (CalendarWidgetProvider.isCalendarWidgetExpanded(context, appWidgetId)) {
            return PREF_KEY_DATE_BACKGROUND_COLOR_4X4;
        }else {
            return PREF_KEY_DATE_BACKGROUND_COLOR;
        }
    }

    private void setMonthTextSizeParamsAndListener() {
        EditText etMonthTextSize = findViewById(R.id.et_month_text_size);
        etMonthTextSize.setText(String.valueOf(loadPrefValue(this, mAppWidgetId, getPrefKeyMonthTextSize(mContext, mAppWidgetId))));
    }

    private void setMonthTextColorParamsAndListener() {
        ImageView imgMonthTextColor = findViewById(R.id.img_month_text_color);
        imgMonthTextColor.setBackgroundColor(loadPrefValue(mContext, mAppWidgetId, getPrefKeyMonthTextColor(mContext, mAppWidgetId)));
        imgMonthTextColor.invalidate();
        RelativeLayout rlMonthTextColor = findViewById(R.id.rl_month_text_color);
        rlMonthTextColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int dialogId = new ColorPickerDialog(mContext,
                        loadPrefValue(mContext, mAppWidgetId, getPrefKeyMonthTextSize(mContext, mAppWidgetId)), true).show();
                SetColorPickerListenerEvent.setListener(dialogId, new ColorPickerListener() {
                    @Override
                    public void onDialogClosing() {
                        // Ничего не делаем
                    }
                    @Override
                    public void onColorChanged(int color) {
                        ImageView imgMonthTextColor = findViewById(R.id.img_month_text_color);
                        imgMonthTextColor.setBackgroundColor(color);
                        imgMonthTextColor.invalidate();
                    }
                });
            }
        });
    }

    private void setDateTextSizeParamsAndListener() {
        EditText etDateTextSize = findViewById(R.id.et_date_text_size);
        etDateTextSize.setText(String.valueOf(loadPrefValue(this, mAppWidgetId, getPrefKeyDateTextSize(mContext, mAppWidgetId))));
    }

    private void setDateTextColorParamsAndListener() {
        ImageView imgDateTextColor = findViewById(R.id.img_date_text_color);
        imgDateTextColor.setBackgroundColor(loadPrefValue(mContext, mAppWidgetId, getPrefKeyDateTextColor(mContext, mAppWidgetId)));
        imgDateTextColor.invalidate();
        RelativeLayout rlDateTextColor = findViewById(R.id.rl_date_text_color);
        rlDateTextColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int dialogId = new ColorPickerDialog(mContext,
                        loadPrefValue(mContext, mAppWidgetId, getPrefKeyDateTextSize(mContext, mAppWidgetId)), true).show();
                SetColorPickerListenerEvent.setListener(dialogId, new ColorPickerListener() {
                    @Override
                    public void onDialogClosing() {
                        // Ничего не делаем
                    }
                    @Override
                    public void onColorChanged(int color) {
                        ImageView imgDateTextColor = findViewById(R.id.img_date_text_color);
                        imgDateTextColor.setBackgroundColor(color);
                        imgDateTextColor.invalidate();
                    }
                });
            }
        });
    }

    private void setDateBackgroundColorParamsAndListener() {
        ImageView imgDateBackgroundColor = findViewById(R.id.img_date_text_background_color);
        imgDateBackgroundColor.setBackgroundColor(loadPrefValue(mContext, mAppWidgetId, getPrefKeyDateBackgroundColor(mContext, mAppWidgetId)));
        imgDateBackgroundColor.invalidate();
        RelativeLayout rlDateBackgroundColor = findViewById(R.id.rl_date_text_background_color);
        rlDateBackgroundColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int dialogId = new ColorPickerDialog(mContext,
                        loadPrefValue(mContext, mAppWidgetId, getPrefKeyDateBackgroundColor(mContext, mAppWidgetId)), true).show();
                SetColorPickerListenerEvent.setListener(dialogId, new ColorPickerListener() {
                    @Override
                    public void onDialogClosing() {
                        // Ничего не делаем
                    }
                    @Override
                    public void onColorChanged(int color) {
                        ImageView imgDateBackgroundColor = findViewById(R.id.img_date_text_background_color);
                        imgDateBackgroundColor.setBackgroundColor(color);
                        imgDateBackgroundColor.invalidate();
                    }
                });
            }
        });
    }

    private void setIsShowCalendarSheetParamsAndListener() {
        CheckBox cbxIsShowCalendarSheet = findViewById(R.id.cbx_is_show_calendar_sheet);
        cbxIsShowCalendarSheet.setChecked(loadPrefValue(this, mAppWidgetId, PREF_KEY_IS_SHOW_CALENDAR_PAGE) != 0);
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

    private void setEventTextSizeParamsAndListener() {
        EditText etTextSize = findViewById(R.id.et_events_text_size);
        etTextSize.setText(String.valueOf(loadPrefValue(this, mAppWidgetId, PREF_KEY_EVENT_TEXT_SIZE)));
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
        EditText etEventTextSize = findViewById(R.id.et_events_text_size);
        int eventTextSize = defaultValues.get(PREF_KEY_EVENT_TEXT_SIZE);
        try {
            eventTextSize = Integer.parseInt(etEventTextSize.getText().toString());
        }catch(Exception e){
            Log.e(TAG, e.toString());
        }
        savePrefValue(mAppWidgetId, PREF_KEY_EVENT_TEXT_SIZE, eventTextSize);

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

        // Сохраняем настройку отображения странички календаря
        CheckBox cbxIsShowCalendarSheet = findViewById(R.id.cbx_is_show_calendar_sheet);
        savePrefValue(mAppWidgetId, PREF_KEY_IS_SHOW_CALENDAR_PAGE, cbxIsShowCalendarSheet.isChecked() ? 1 : 0);

        // Сохраняем размер текста месяца в настройках
        int monthTextSize = defaultValues.get(getPrefKeyMonthTextSize(mContext, mAppWidgetId));
        try {
            EditText etMonthTextSize = findViewById(R.id.et_month_text_size);
            monthTextSize = Integer.parseInt(etMonthTextSize.getText().toString());
        }catch(Exception e){
            Log.e(TAG, e.toString());
        }
        savePrefValue(mAppWidgetId, getPrefKeyMonthTextSize(mContext, mAppWidgetId), monthTextSize);

        // Сохраняем цвет текста в настройках
        ImageView imgMonthTextColor = findViewById(R.id.img_month_text_color);
        ColorDrawable monthTextColorDrawable = (ColorDrawable) imgMonthTextColor.getBackground();
        if (monthTextColorDrawable != null) savePrefValue(mAppWidgetId, getPrefKeyMonthTextColor(mContext, mAppWidgetId), monthTextColorDrawable.getColor());

        // Сохраняем размер текста даты в настройках
        int dateTextSize = defaultValues.get(getPrefKeyDateTextSize(mContext, mAppWidgetId));
        try {
            EditText etDateTextSize = findViewById(R.id.et_date_text_size);
            dateTextSize = Integer.parseInt(etDateTextSize.getText().toString());
        }catch(Exception e){
            Log.e(TAG, e.toString());
        }
        savePrefValue(mAppWidgetId, getPrefKeyDateTextSize(mContext, mAppWidgetId), dateTextSize);

        // Сохраняем цвет текста даты в настройках
        ImageView imgDateTextColor = findViewById(R.id.img_date_text_color);
        ColorDrawable dateTextColorDrawable = (ColorDrawable) imgDateTextColor.getBackground();
        if (dateTextColorDrawable != null) savePrefValue(mAppWidgetId, getPrefKeyDateTextColor(mContext, mAppWidgetId), dateTextColorDrawable.getColor());

        // Сохраняем цвет фона даты в настройках
        ImageView imgDateBackgroundColor = findViewById(R.id.img_date_text_background_color);
        ColorDrawable dateBackgroundColorDrawable = (ColorDrawable) imgDateBackgroundColor.getBackground();
        if (dateBackgroundColorDrawable != null) savePrefValue(mAppWidgetId, getPrefKeyDateBackgroundColor(mContext, mAppWidgetId), dateBackgroundColorDrawable.getColor());

        // В зону ответственности активности настроек входит обновление виджета на экране
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        String providerClassName;
        if (appWidgetManager != null && appWidgetManager.getAppWidgetInfo(mAppWidgetId) != null && appWidgetManager.getAppWidgetInfo(mAppWidgetId).provider != null){
            providerClassName = appWidgetManager.getAppWidgetInfo(mAppWidgetId).provider.getClassName();
        }else{
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Передадим обработку результов в EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}

