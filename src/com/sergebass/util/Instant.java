package com.sergebass.util;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 * Instant.
 *
 * @author Serge Perinsky
 */
/*
 * Instant.java (C) Serge Perinsky
 */

import java.util.*;

/**
 * Момент во времени, с точностью до миллисекунды.
 *
 * @see java.util.Date
 *
 * @author Serge Perinsky
 */
public class Instant
        extends Date {

    static TimeZone timeZone = TimeZone.getDefault();

    /**
     * Allocates a Instant object and initializes it so that it represents the
     * time at which it was allocated measured to the nearest millisecond. 
     */
    public Instant() {

        super();
    }

    /**
     * Allocates a Instant object and initializes it to represent the
     * specified number of milliseconds since January 1, 1970, 00:00:00 GMT.
     * (Unix "epoch" beginning)
     *
     * @param  time  the number of milliseconds since "epoch"
     */
    public Instant(long time) {

        super(time);
    }

    /**
     * Creates a Instant object from the standard Date object.
     *
     * @param  date  the Date object
     */
    public Instant(Date date) {

        setTime(date.getTime());
    }

    public static void setTimeZone(TimeZone timeZone) {

        Instant.timeZone = timeZone;
    }

    public static TimeZone getTimeZone() {

        return timeZone;
    }

/*
    public String getDayOfWeekName() {

        Calendar calendar = Calendar.getInstance(timeZone, locale);
        calendar.setTime(this);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        switch (dayOfWeek) {

            case Calendar.SUNDAY:
                return UtilResources.getString("sunday");

            case Calendar.MONDAY:
                return UtilResources.getString("monday");

            case Calendar.TUESDAY:
                return UtilResources.getString("tuesday");

            case Calendar.WEDNESDAY:
                return UtilResources.getString("wednesday");

            case Calendar.THURSDAY:
                return UtilResources.getString("thursday");

            case Calendar.FRIDAY:
                return UtilResources.getString("friday");

            case Calendar.SATURDAY:
                return UtilResources.getString("saturday");

            default:
                return "#" + dayOfWeek;
        }
    }
 */

    public String getId() {

        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(this);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        int second = calendar.get(Calendar.SECOND);
        int ms = calendar.get(Calendar.MILLISECOND);

        String msString;

        if (ms < 10) {
            msString = "00" + ms;
        } else if (ms < 100) {
            msString = "0" + ms;
        } else {
            msString = "" + ms;
        }

        return "" + year
              + (month < 10? "0" : "") + month
              + (day < 10? "0" : "") + day
              + (hour < 10? "0" : "") + hour
              + (minute < 10? "0" : "") + minute
              + (second < 10? "0" : "") + second
              + "" + msString;
    }

    public String getDateId() {

        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(this);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return "" + year
              + (month < 10? "0" : "") + month
              + (day < 10? "0" : "") + day;
    }

    public String getTimeId() {

        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(this);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        int second = calendar.get(Calendar.SECOND);

        return "" + (hour < 10? "0" : "") + hour
              + (minute < 10? "0" : "") + minute
              + (second < 10? "0" : "") + second;
    }

    public String getDateTimeId() {

        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(this);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        int second = calendar.get(Calendar.SECOND);

        return "" + year
              + (month < 10? "0" : "") + month
              + (day < 10? "0" : "") + day
              + (hour < 10? "0" : "") + hour
              + (minute < 10? "0" : "") + minute
              + (second < 10? "0" : "") + second;
    }

    /**
     * Returns the full String representation of the date.
     *
     * @return  the string representation
     */
    public String toString() {

        return getId();
    }

    /**
     * Возвращает значение заданного поля.
     *
     * @param field код поля (константа в классе Calendar, например Calendar.YEAR)
     *
     * @return значение заданного поля
     *
     * @see     java.util.Calendar
     * @see     java.util.Date
     */
    public int getValue(int field) {

        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(this);

        int value = calendar.get(field);

        return value;
    }

    public boolean equals(Instant other) {

        Calendar calendarThis = Calendar.getInstance(timeZone);
        calendarThis.setTime(this);

        Calendar calendarOther = Calendar.getInstance(timeZone);
        calendarOther.setTime(other);

        return calendarThis.equals(calendarOther);
    }

    public boolean before(Instant other) {

        Calendar calendarThis = Calendar.getInstance(timeZone);
        calendarThis.setTime(this);

        Calendar calendarOther = Calendar.getInstance(timeZone);
        calendarOther.setTime(other);

        return calendarThis.before(calendarOther);
    }

    public boolean after(Instant other) {

        Calendar calendarThis = Calendar.getInstance(timeZone);
        calendarThis.setTime(this);

        Calendar calendarOther = Calendar.getInstance(timeZone);
        calendarOther.setTime(other);

        return calendarThis.after(calendarOther);
    }
}
