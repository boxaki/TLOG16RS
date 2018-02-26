package com.akos_varga.tlog16rs.resources;

import com.akos_varga.tlog16rs.core.beans.Task;
import com.akos_varga.tlog16rs.core.beans.TimeLogger;
import com.akos_varga.tlog16rs.core.beans.WorkDay;
import com.akos_varga.tlog16rs.core.beans.WorkMonth;
import com.akos_varga.tlog16rs.core.exceptions.EmptyTimeFieldException;
import com.akos_varga.tlog16rs.core.exceptions.FutureWorkException;
import com.akos_varga.tlog16rs.core.exceptions.NotNewDateException;
import com.akos_varga.tlog16rs.core.exceptions.NotNewMonthException;
import com.akos_varga.tlog16rs.core.exceptions.NotSeparatedTimesException;
import com.akos_varga.tlog16rs.core.exceptions.NotTheSameMonthException;
import com.akos_varga.tlog16rs.core.exceptions.WeekendNotEnabledException;

/**
 *
 * @author Akos Varga
 */
public class Service {

    public static boolean isNewMonth(TimeLogger timelogger, int year, int month) {
        return getMonth(timelogger, year, month) == null;
    }

    public static WorkMonth getMonth(TimeLogger timelogger, int year, int month) {
        for (WorkMonth existingMonth : timelogger.getMonths()) {
            if (existingMonth.getDate().getYear() == year && existingMonth.getDate().getMonthValue() == month) {
                return existingMonth;
            }
        }
        return null;
    }

    public static boolean isNewDay(TimeLogger timelogger, int year, int month, int day) {
        return getDay(timelogger, year, month, day) == null;
    }

    public static WorkDay getDay(TimeLogger timelogger, int year, int month, int day) {
        WorkMonth workMonth = getMonth(timelogger, year, month);
        if (workMonth != null) {
            for (WorkDay existingDay : workMonth.getDays()) {
                if (existingDay.getActualDay().getDayOfMonth() == day) {
                    return existingDay;
                }
            }
        }
        return null;
    }

    public static boolean isNewTask(TimeLogger timelogger, String taskId, int year, int month, int day, String startTime) {
        return getTask(timelogger, taskId, year, month, day, startTime) == null;
    }

    public static Task getTask(TimeLogger timelogger, String taskId, int year, int month, int day, String startTime) {
        WorkDay workDay = getDay(timelogger, year, month, day);
        if (workDay != null) {
            for (Task existingTask : workDay.getTasks()) {
                if (existingTask.getTaskId().equals(taskId) && existingTask.getStartTime().toString().equals(startTime)) {
                    return existingTask;
                }
            }
        }
        return null;
    }

    public static WorkDay getWorkDayOrCreateIfNotExist(TimeLogger timelogger, int year, int month, int day) throws WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, NotNewMonthException, FutureWorkException {
        WorkDay workDay = getDay(timelogger, year, month, day);
        if (workDay == null) {
            workDay = addNewWorkDay(timelogger, year, month, day);
        }
        return workDay;
    }

    public static WorkDay addNewWorkDay(TimeLogger timelogger, int year, int month, int day) throws WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, NotNewMonthException, FutureWorkException {
        WorkDay newWorkDay = new WorkDay(year, month, day);
        WorkMonth workMonth = getWorkMonthOrCreateIfNotExist(timelogger, year, month, day);
        workMonth.addWorkDay(newWorkDay);

        return newWorkDay;
    }

    public static WorkMonth getWorkMonthOrCreateIfNotExist(TimeLogger timelogger, int year, int month, int day) throws NotNewMonthException {
        WorkMonth workMonth = getMonth(timelogger, year, month);
        if (workMonth == null) {
            workMonth = addNewWorkMonth(timelogger, year, month, day);
        }
        return workMonth;
    }

    private static WorkMonth addNewWorkMonth(TimeLogger timelogger, int year, int month, int day) throws NotNewMonthException {
        WorkMonth newWorkMonth = new WorkMonth(year, month);
        timelogger.addNewMonth(newWorkMonth);
        return newWorkMonth;
    }

    public static Task modifyTaskIfPossible(TimeLogger timelogger, Task originalTask, Task newTask, int year, int month, int day) throws WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, NotNewMonthException, FutureWorkException, EmptyTimeFieldException, NotSeparatedTimesException {
        WorkDay workDay = getWorkDayOrCreateIfNotExist(timelogger, year, month, day);
        if (originalTask != null) {
            workDay.getTasks().remove(originalTask);
        }
        try {
            workDay.addTask(newTask);
        } catch (NotSeparatedTimesException ex) {
            if (originalTask != null) {
                workDay.addTask(originalTask);
                newTask = originalTask;
            }
        }
        return newTask;
    }

}
