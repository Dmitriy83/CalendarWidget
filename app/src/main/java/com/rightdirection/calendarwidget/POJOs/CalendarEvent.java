package com.rightdirection.calendarwidget.POJOs;

import android.os.Parcel;
import android.os.Parcelable;

public class CalendarEvent implements Parcelable{
    private final long startTime;
    private final long endTime;
    private final String title;
    private final long id;

    public CalendarEvent(long startTime, long endTime, String title, long id) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.title = title;
        this.id = id;
    }

    private CalendarEvent(Parcel in) {
        startTime = in.readLong();
        endTime = in.readLong();
        title = in.readString();
        id = in.readLong();
    }

    @SuppressWarnings("unused")
    public static final Creator<CalendarEvent> CREATOR = new Creator<CalendarEvent>() {
        @Override
        public CalendarEvent createFromParcel(Parcel in) {
            return new CalendarEvent(in);
        }

        @Override
        public CalendarEvent[] newArray(int size) {
            return new CalendarEvent[size];
        }
    };

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public String getTitle() {
        return title;
    }

    public long getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(startTime);
        dest.writeLong(endTime);
        dest.writeString(title);
        dest.writeLong(id);
    }
}
