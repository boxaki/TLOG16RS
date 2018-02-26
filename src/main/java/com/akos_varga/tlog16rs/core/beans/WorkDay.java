package com.akos_varga.tlog16rs.core.beans;

import com.akos_varga.tlog16rs.core.exceptions.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.time.*;
import java.util.*;

/**
 * Represents a work day that can contain Task(s).
 *
 * @author Akos Varga
 * @version 0.5.0
 */
@lombok.Getter
public class WorkDay {
    
    private final static int DEFAULT_REQUIRED_MIN_PER_DAY = 450;
    private final static LocalDate DEFAULT_ACTUAL_DAY = LocalDate.now();
    
    private final List<Task> tasks;
    private long requiredMinPerDay;
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate actualDay;
    private long sumPerDay;

    /**
     * Constructs a <code>WorkDay</code> with default values. Sets the
     * requiredMinPerDay to {@value #DEFAULT_REQUIRED_MIN_PER_DAY} and the
     * default day to the actual day.
     */
    public WorkDay() {
        this.tasks = new ArrayList<>();
        this.requiredMinPerDay = DEFAULT_REQUIRED_MIN_PER_DAY;
        this.actualDay = DEFAULT_ACTUAL_DAY;
        this.sumPerDay = 0;
    }

    /**
     * Construct an object with a user defined requiredMinPerDay and sets the
     * day to the actual day;
     *
     * @throws NegativeMinutesOfWorkException if requiredMinPerDayIsNegative.
     */
    public WorkDay(long requiredMinPerDay) throws NegativeMinutesOfWorkException {
        this();
        setRequiredMinOrThrowIfNegative(requiredMinPerDay);
    }

    /**
     * Constructs a WorkDay on any given not future date with requiredMinPerDay
     * set to {@value #DEFAULT_REQUIRED_MIN_PER_DAY}.
     *
     * @throws FutureWorkException if the given date is in the future.
     */
    public WorkDay(int year, int month, int day) throws FutureWorkException {
        this();
        LocalDate date = LocalDate.of(year, month, day);
        setActualDayOrThrowIfFutureDay(date);
    }

    /**
     * Constructs a WorkDay on any given not future date with requiredMinPerDay
     * set to a user defined value.
     *
     * @throws NegativeMinutesOfWorkException if requiredMinPerDayIsNegative.
     * @throws FutureWorkException FutureWorkException if the given date is in
     * the future.
     */
    public WorkDay(long requiredMinPerDay, int year, int month, int day) throws NegativeMinutesOfWorkException, FutureWorkException {
        this(year, month, day);
        setRequiredMinOrThrowIfNegative(requiredMinPerDay);
    }

    /**
     * @throws NotSeparatedTimesException if the Task to add has a common time
     * interval with existing Task(s).
     */
    public void addTask(Task t) throws EmptyTimeFieldException, NotSeparatedTimesException {
        if (Util.isSeparatedTime(t, tasks)) {
            if (Util.isMultipleQuarterHour(t.getMinPerTask())) {
                tasks.add(t);
                Collections.sort(tasks, Comparator.comparing(Task::getStartTime));
            }
        } else {
            throw new NotSeparatedTimesException("Time intervals overlapping each other!");
        }
    }

    /**
     * @throws NegativeMinutesOfWorkException if requiredMinPerDayIsNegative.
     */
    public void setRequiredMinPerDay(long requiredMinPerDay) throws NegativeMinutesOfWorkException {
        setRequiredMinOrThrowIfNegative(requiredMinPerDay);
    }

    /**
     * @throws FutureWorkException if the given date is in the future.
     */
    public void setActualDay(int year, int month, int day) throws FutureWorkException {
        LocalDate dayToSet = LocalDate.of(year, month, day);
        setActualDayOrThrowIfFutureDay(dayToSet);
    }

    /**
     *
     * @return the sum of the work hours for the day in minutes.
     * @throws EmptyTimeFieldException if start time or end time is missing.
     */
    public long getSumPerDay() throws EmptyTimeFieldException {
        
        sumPerDay = 0;
        for (Task t : tasks) {
            sumPerDay += t.getMinPerTask();
        }
        return sumPerDay;
    }

    /**
     * @return the difference between the sum of the work minutes and the
     * required minutes for the day.
     */
    public long getExtraMinPerDay() throws EmptyTimeFieldException {
        return getSumPerDay() - getRequiredMinPerDay();
    }

    /**
     * @return the end time of the latest task for a day or return
     * <code>null</code> if no tasks has been added.
     */
    public LocalTime getEndTimeOfLatestTask() throws EmptyTimeFieldException {
        if (tasks.isEmpty()) {
            return null;
        }
        return tasks.get(tasks.size() - 1).getEndTime();
    }
    
    private void setRequiredMinOrThrowIfNegative(long requiredMin) throws NegativeMinutesOfWorkException {
        if (requiredMin < 0) {
            throw new NegativeMinutesOfWorkException("Required minutes for a day cannot be negative!");
        }
        this.requiredMinPerDay = requiredMin;
    }
    
    private void setActualDayOrThrowIfFutureDay(LocalDate date) throws FutureWorkException {
        LocalDate today = LocalDate.now();
        if (date.isAfter(today)) {
            throw new FutureWorkException("Future date is not allowed!");
        }
        this.actualDay = date;
    }
    
    private static class LocalDateSerializer extends JsonSerializer<LocalDate> {        
        @Override
        public void serialize(LocalDate t, JsonGenerator jg, SerializerProvider sp) throws IOException {
            jg.writeString(t.toString());
        }
        
    }
    
}
