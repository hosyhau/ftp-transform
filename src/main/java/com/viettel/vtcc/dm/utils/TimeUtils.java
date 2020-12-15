package com.viettel.vtcc.dm.utils;

import java.util.*;

/**
 * Created by thuyenhx on 20/12/2017.
 */
public class TimeUtils {

    public static final long FIVE_SEC_IN_MILIS = 5 * 1000L;
    public static final long TWENTY_SEC_IN_MILIS = 20 * 1000L;
    public static final long ONE_MIN_IN_MILIS = 1 * 60 * 1000L;
    public static final long FIVE_MIN_IN_MILIS = 5 * 60 * 1000L;
    public static final long ONE_HOUR_IN_MILIS = 60 * 60 * 1000L;
    public static final long SEVEN_HOUR_IN_MILIS = 7 * 60 * 60 * 1000L;
    public static final long ONE_DAY_IN_MILIS = 24 * 60 * 60 * 1000L;
    public static final long ONE_WEEK_IN_MILIS = 7 * 24 * 60 * 60 * 1000L;

    public static int getFiveSecondsId(long time) {
        return (int) (time / (5 * 1000));
    }

    public static int getOneMinutesId(long time) {
        return (int) (time / (1 * 60 * 1000));
    }

    public static int getFiveMinutesId(long time) {
        return (int) (time / (5 * 60 * 1000));
    }

    public static int getHourId(long time) {
        return (int) (time / (1 * 60 * 60 * 1000));
    }

    public static int getDayId(long time) {
        return (int) (time / (24 * 60 * 60 * 1000));
    }

    public static long getTwentySecRoundTime(long time) {
        return time - (time % TWENTY_SEC_IN_MILIS);
    }

    public static long getFiveMinRoundTime(long time) {
        return time - (time % FIVE_MIN_IN_MILIS);
    }

    public static long getFiveSecRoundTime(long time) {
        return time - (time % FIVE_SEC_IN_MILIS);
    }

    public static long getOneMinRoundTime(long time) {
        return time - (time % ONE_MIN_IN_MILIS);
    }

    public static long getHourRoundTime(long time) {
        return time - (time % ONE_HOUR_IN_MILIS);
    }

    public static synchronized long getDayRoundTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static synchronized int getWeekOfYear(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.setMinimalDaysInFirstWeek(4);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    public static synchronized long getWeekRoundTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return getDayRoundTime(calendar.getTimeInMillis());
    }

    public static Map<String, List<Long>> getWeekListOfMonth(long time) {
        long start = getMonthRoundTime(time);
        long end = getMonthLastDay(start);
        Map<Long, List<Long>> weekMap = new HashMap<>();
        for (long t = start; t <= end; t += ONE_DAY_IN_MILIS) {
            long weekRoundTime = getWeekRoundTime(t);
            List<Long> lst = weekMap.get(weekRoundTime);
            if (lst == null) {
                lst = new ArrayList();
                weekMap.put(weekRoundTime, lst);
            }
            lst.add(t);
        }

        Map<String, List<Long>> res = new HashMap<>();
        for (Map.Entry<Long, List<Long>> entry : weekMap.entrySet()) {
            long key = entry.getKey();
            List<Long> value = entry.getValue();

            if (value.size() < 7) {
                List<Long> lstTime = res.get("day");
                if (lstTime == null) {
                    lstTime = new ArrayList();
                    res.put("day", lstTime);
                }
                lstTime.addAll(value);
            } else {
                List<Long> lstTime = res.get("week");
                if (lstTime == null) {
                    lstTime = new ArrayList();
                    res.put("week", lstTime);
                }
                lstTime.add(key);
            }
        }
        return res;
    }

    public static synchronized long getMonthRoundTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.DATE, 1);
        return getDayRoundTime(calendar.getTimeInMillis());
    }

    /**
     * get last week monday
     */
    public static long getLastWeekMonday(long timeId) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeId - 7 * ONE_DAY_IN_MILIS);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return getDayRoundTime(calendar.getTimeInMillis());
    }

    /**
     * get first day of last month
     */
    public static long getLastMonthFirstDay(long timeId) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeId);
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DATE, 1);
        return getDayRoundTime(calendar.getTimeInMillis());
    }

    /**
     * get first day of next month
     */
    public static long getNextMonthFirstDay(long timeId) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeId);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DATE, 1);
        return getDayRoundTime(calendar.getTimeInMillis());
    }

    /**
     * get last day of last month
     */
    public static long getLastMonthLastDay(long timeId) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeId);
        calendar.set(Calendar.DATE, 1);
        calendar.add(Calendar.DATE, -1);
        return getDayRoundTime(calendar.getTimeInMillis());
    }

    /**
     * get last day of this month
     */
    public static long getMonthLastDay(long timeId) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeId);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return getDayRoundTime(calendar.getTimeInMillis());
    }

    public static List<Long> getMonthList(long fromTime, long toTime) {
        List<Long> res = new ArrayList<>();
        long fromRoundTime = getMonthRoundTime(fromTime);
        while (fromRoundTime <= toTime) {
            res.add(fromRoundTime);
            fromRoundTime = getNextMonthFirstDay(fromRoundTime);
        }
        return res;
    }

    public static List<Long> getWeekList(long fromTime, long toTime) {
        SortedSet<Long> res = new TreeSet<>();
        long fromRoundTime = TimeUtils.getDayRoundTime(fromTime);
        long toRoundTime = TimeUtils.getDayRoundTime(toTime) + TimeUtils.ONE_DAY_IN_MILIS;
        for (long i = fromRoundTime; i <= toRoundTime; i += TimeUtils.ONE_DAY_IN_MILIS) {
            res.add(getWeekRoundTime(i));
        }
        return new ArrayList<>(res);
    }

    /**
     * get list of day from range
     */
    public static List<Long> getDaysList(long fromTime, long toTime, int hour) {
        List<Long> res = new ArrayList<>();
        long count = (toTime - fromTime) / ONE_DAY_IN_MILIS + 1;
        for (long i = 0; i < count; i++) {
            long temp = getDayRoundTime(fromTime) + i * ONE_DAY_IN_MILIS + hour * ONE_HOUR_IN_MILIS;
            res.add(temp);
        }
        return res;
    }

    /**
     * get list of hour from range
     */
    public static List<Long> getHoursList(long day) {
        List<Long> res = new ArrayList<>();
        for (long i = 0; i < 23; i++) {
            long temp = getDayRoundTime(day) + i * ONE_HOUR_IN_MILIS;
            res.add(temp);
        }
        return res;
    }
}