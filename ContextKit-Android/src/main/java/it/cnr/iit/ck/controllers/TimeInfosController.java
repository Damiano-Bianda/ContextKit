package it.cnr.iit.ck.controllers;

import java.util.Calendar;
import java.util.GregorianCalendar;

import it.cnr.iit.ck.model.TimeInfosData;

public class TimeInfosController {

    public static TimeInfosData getTimeInfosData(){
        long time = System.currentTimeMillis();
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(time);

        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        TimeInfosData.DayType dayType = (weekDay == Calendar.SATURDAY || weekDay == Calendar.SUNDAY)?
                TimeInfosData.DayType.WEEKEND : TimeInfosData.DayType.WEEKDAY;

        TimeInfosData.TimeOfDay timeOfDay;
        if (5 <= hour && hour <= 12){
            timeOfDay = TimeInfosData.TimeOfDay.MORNING;
        } else if (13 <= hour && hour <= 17){
            timeOfDay = TimeInfosData.TimeOfDay.AFTERNOON;
        } else if (18 <= hour && hour <= 22){
            timeOfDay = TimeInfosData.TimeOfDay.EVENING;
        } else {
            timeOfDay = TimeInfosData.TimeOfDay.NIGHT;
        }

        return new TimeInfosData(dayType, timeOfDay);
    }

}
