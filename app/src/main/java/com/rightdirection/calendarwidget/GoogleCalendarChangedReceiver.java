package com.rightdirection.calendarwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import static com.rightdirection.calendarwidget.CalendarWidgetProvider.ACTION_GOOGLE_CALENDAR_CHANGED;

// Отдельный "получатель" нужен по той причине, что CalendarWidgetProvider имеет очень короткий "жизненный срок"
class GoogleCalendarChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent updateIntent = new Intent(context.getApplicationContext(), CalendarWidgetProvider.class);
        updateIntent.setAction(ACTION_GOOGLE_CALENDAR_CHANGED);
        context.getApplicationContext().sendBroadcast(updateIntent);
    }
}
