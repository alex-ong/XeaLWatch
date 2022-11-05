package com.example.xealwatch;

import java.util.Calendar;

public class TimeDegrees {
    static final int FINAL_MULT = 6;
    static final int HOUR_TO_SIXTY = 5;
    static final float MINUTE_PER_HOUR = 60f;
    static final float SECOND_PER_MINUTE = 60f;
    static final float SECOND_PER_HOUR = MINUTE_PER_HOUR * SECOND_PER_MINUTE; //3600f
    static final float MILLIS_PER_SECOND = 1000f;
    static final float MILLIS_PER_MINUTE = MILLIS_PER_SECOND*SECOND_PER_MINUTE;
    static final float MILLIS_PER_HOUR = MILLIS_PER_SECOND * SECOND_PER_HOUR; //3600000f

    public static float GetDegreesValue(int CalendarType, Calendar cal)
    {
        switch (CalendarType)
        {
            case Calendar.HOUR:
                return FINAL_MULT * (HOUR_TO_SIXTY * (cal.get(Calendar.HOUR)
                        + (cal.get(Calendar.MINUTE)/MINUTE_PER_HOUR)
                        + (cal.get(Calendar.SECOND)/SECOND_PER_HOUR)
                        + (cal.get(Calendar.MILLISECOND)/MILLIS_PER_HOUR)));
            case Calendar.MINUTE:
                return FINAL_MULT * (cal.get(Calendar.MINUTE)
                        + cal.get(Calendar.SECOND)/SECOND_PER_MINUTE
                        + cal.get(Calendar.MILLISECOND)/MILLIS_PER_MINUTE);
            case Calendar.SECOND:
                return FINAL_MULT * (cal.get(Calendar.SECOND)
                        + cal.get(Calendar.MILLISECOND)/MILLIS_PER_SECOND);
            default:
                return FINAL_MULT * (cal.get(Calendar.SECOND));
        }
    }

}
