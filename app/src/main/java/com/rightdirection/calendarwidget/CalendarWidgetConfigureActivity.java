package com.rightdirection.calendarwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.onegravity.colorpicker.ColorPickerDialog;
import com.onegravity.colorpicker.ColorPickerListener;
import com.onegravity.colorpicker.SetColorPickerListenerEvent;

/**
 * Экран настроек для виджета {@link CalendarWidgetProvider CalendarWidgetProvider}.
 */
public class CalendarWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "com.rightdirection.calendarwidget.CalendarWidgetProvider";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    private static final String PREF_KEY_TEXT_COLOR = "text_color";
    private static final String PREF_KEY_BACKGROUND_COLOR = "background_color";
    private static final String PREF_KEY_TEXT_SIZE = "text_size";
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Context mContext;
    private final String TAG = this.getClass().getSimpleName();
    public static final int DEFAULT_TEXT_COLOR = Color.WHITE;
    private static final int DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT;
    private static final int DEFAULT_TEXT_SIZE = 15;

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
        setBackgroundColorParamsAndListener();
    }

    private void setBackgroundColorParamsAndListener() {
        ImageView imgBackgroundColor = (ImageView) findViewById(R.id.img_background_color);
        imgBackgroundColor.setBackgroundColor(loadBackgroundColorBackgroundColorPref(this, mAppWidgetId));
        imgBackgroundColor.invalidate();
        RelativeLayout rlBackgroundColor = (RelativeLayout) findViewById(R.id.rl_background_color);
        rlBackgroundColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int dialogId = new ColorPickerDialog(mContext,
                        loadBackgroundColorBackgroundColorPref(mContext, mAppWidgetId), true).show();
                SetColorPickerListenerEvent.setListener(dialogId, new ColorPickerListener() {
                    @Override
                    public void onDialogClosing() {
                        // Ничего не делаем
                    }
                    @Override
                    public void onColorChanged(int color) {
                        ImageView imgBackgroundColor = (ImageView) findViewById(R.id.img_background_color);
                        imgBackgroundColor.setBackgroundColor(color);
                        imgBackgroundColor.invalidate();
                    }
                });
            }
        });
    }

    private void setTextColorParamsAndListener() {
        ImageView imgTextColor = (ImageView) findViewById(R.id.img_text_color);
        imgTextColor.setBackgroundColor(loadTextColorBackgroundColorPref(this, mAppWidgetId));
        imgTextColor.invalidate();
        RelativeLayout rlTextColor = (RelativeLayout) findViewById(R.id.rl_text_color);
        rlTextColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int dialogId = new ColorPickerDialog(mContext,
                        loadTextColorBackgroundColorPref(mContext, mAppWidgetId), true).show();
                SetColorPickerListenerEvent.setListener(dialogId, new ColorPickerListener() {
                    @Override
                    public void onDialogClosing() {
                        // Ничего не делаем
                    }
                    @Override
                    public void onColorChanged(int color) {
                        ImageView imgTextColor = (ImageView) findViewById(R.id.img_text_color);
                        imgTextColor.setBackgroundColor(color);
                        imgTextColor.invalidate();
                    }
                });
            }
        });
    }

    private void setTextSizeParamsAndListener() {
        EditText etTextSize = (EditText) findViewById(R.id.et_text_size);
        etTextSize.setText(String.valueOf(loadTextSizePref(this, mAppWidgetId)));
    }

    private void setAddUpgradeButtonParamsAndListener() {
        Button addUpgradeButton = (Button)findViewById(R.id.add_upgrade_button);
        if (isItFirstConfiguration()){
            addUpgradeButton.setText(getString(R.string.add_widget));
        }
        addUpgradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Сохраняем размер текста в настройках
                EditText etTextSize = (EditText) findViewById(R.id.et_text_size);
                int textSize = DEFAULT_TEXT_SIZE;
                try {
                    textSize = Integer.parseInt(etTextSize.getText().toString());
                }catch(Exception e){
                    Log.e(TAG, e.toString());
                }
                saveTextSizePref(mAppWidgetId, textSize);

                // Сохраняем цвет текста в настройках
                ImageView imgTextColor = (ImageView) findViewById(R.id.img_text_color);
                ColorDrawable textColorDrawable = (ColorDrawable) imgTextColor.getBackground();
                if (textColorDrawable != null) saveTextColorPref(mAppWidgetId, textColorDrawable.getColor());

                // Сохраняем цвет фона в настройках
                ImageView imgBackgroundColor = (ImageView) findViewById(R.id.img_background_color);
                ColorDrawable backgroundColorDrawable = (ColorDrawable) imgBackgroundColor.getBackground();
                if (backgroundColorDrawable != null) saveBackgroundColorPref(mAppWidgetId, backgroundColorDrawable.getColor());

                // В зону ответственности активности настроек входит обновление виджета на экране
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
                CalendarWidgetProvider.updateAppWidget(mContext, appWidgetManager, mAppWidgetId);

                // Обязательно возвращаем виджет id
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
    }

    private void saveTextSizePref(int appWidgetId, int textSize) {
        SharedPreferences.Editor prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId + PREF_KEY_TEXT_SIZE, textSize);
        prefs.apply();
    }

    public static int loadTextSizePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(PREF_PREFIX_KEY + appWidgetId + PREF_KEY_TEXT_SIZE, DEFAULT_TEXT_SIZE);
    }

    private void saveTextColorPref(int appWidgetId, int color) {
        SharedPreferences.Editor prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId + PREF_KEY_TEXT_COLOR, color);
        prefs.apply();
    }

    public static int loadTextColorBackgroundColorPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(PREF_PREFIX_KEY + appWidgetId + PREF_KEY_TEXT_COLOR, DEFAULT_TEXT_COLOR);
    }

    private void saveBackgroundColorPref(int appWidgetId, int color) {
        SharedPreferences.Editor prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId + PREF_KEY_BACKGROUND_COLOR, color);
        prefs.apply();
    }

    public static int loadBackgroundColorBackgroundColorPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(PREF_PREFIX_KEY + appWidgetId + PREF_KEY_BACKGROUND_COLOR, DEFAULT_BACKGROUND_COLOR);
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
}

