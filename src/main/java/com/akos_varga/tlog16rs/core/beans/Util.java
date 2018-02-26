package com.akos_varga.tlog16rs.core.beans;

import com.akos_varga.tlog16rs.core.exceptions.*;
import java.time.*;
import java.util.*;

/**
 *
 * @author Akos Varga
 * @version 0.5.0
 */
public class Util {

    /**
     * Rounds a time interval to the multiples of 15 minutes. Only sets the end
     * time if it needs changing.
     *
     * @return an end time that has a interval together with startTime that is
     * multiple of 15 minutes.
     * @throws EmptyTimeFieldException if <code>startTime</code> or
     * <code>endTime</code> has missing time field.
     * @throws NotExpectedTimeOrderException if <code>startTime</code> is after
     * <code>endTime</code>.
     */
    public static LocalTime roundToMultipleQuarterHour(LocalTime startTime, LocalTime endTime) throws EmptyTimeFieldException, NotExpectedTimeOrderException {
        if (!isMultipleQuarterHour(startTime, endTime)) {
            long duration = Duration.between(startTime, endTime).toMinutes();
            long mod = duration % 15;
            long roundedDuration = mod > 7 ? duration + 15 - mod : duration - mod;
            endTime = startTime.plusMinutes(roundedDuration);
        }
        return endTime;
    }

    /**
     * @param newTask
     * @param existingTasks
     * @return <code>true</code> if <code>newTask</code> has a common time
     * interval with any task in <code>existingTasks</code>.
     * @throws EmptyTimeFieldException if <code>newTask</code> has missing time
     * field.
     */
    public static boolean isSeparatedTime(Task newTask, List<Task> existingTasks) throws EmptyTimeFieldException {        
        LocalTime newTaskStartTime = newTask.getStartTime();
        LocalTime newTaskEndTime = newTask.getEndTime();
        /*
        matchingTask = existingTasks.stream()
                .filter(task -> task.getStartTime().isBefore(newTask.getEndTime()) && newTask.getStartTime().isBefore(task.getEndTime()))                
                .findFirst()
                .orElse(null);
         */
        for (Task existingTask : existingTasks) {
            LocalTime existingTaskStartTime = existingTask.getStartTime();
            LocalTime existingTaskEndTime = existingTask.getEndTime();
            if (existingTaskStartTime.equals(existingTaskEndTime) && existingTaskStartTime.equals(newTaskStartTime)) {
                return false;
            }
            if (newTaskStartTime.equals(newTaskEndTime) && newTaskStartTime.equals(existingTaskStartTime)) {
                return false;
            }
            if (existingTask.getStartTime().isBefore(newTask.getEndTime()) && newTask.getStartTime().isBefore(existingTask.getEndTime())) {
                return false;
            }        
        }        
        return true;
    }

    public static boolean isWeekday(LocalDate dayToCheck) {
        DayOfWeek day = dayToCheck.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    /**
     *
     * @return <code>true</code> if the time interval between
     * <code>startTime</code> and <code>endTime</code> is multiple of 15 minutes
     * <code>false</code> otherwise.
     * @throws EmptyTimeFieldException if <code>startTime</code> or
     * <code>endTime</code> has missing time field.
     * @throws NotExpectedTimeOrderException if <code>startTime</code> is after
     * <code>endTime</code>
     */
    public static boolean isMultipleQuarterHour(LocalTime startTime, LocalTime endTime) throws EmptyTimeFieldException, NotExpectedTimeOrderException {
        if (startTime == null || endTime == null) {
            throw new EmptyTimeFieldException();
        }
        if (startTime.isAfter(endTime)) {
            throw new NotExpectedTimeOrderException();
        }
        return isMultipleQuarterHour(Duration.between(startTime, endTime).toMinutes());
    }

    public static boolean isMultipleQuarterHour(long minutes) {
        return minutes % 15 == 0;
    }

}
