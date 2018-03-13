package com.akos_varga.tlog16rs.entities;

import com.akos_varga.tlog16rs.core.beans.Util;
import com.akos_varga.tlog16rs.core.exceptions.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.time.*;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a task that has an id a time interval and an optional comment. 
 *
 * @author Akos Varga
 * @version 0.5.0
 */
@Getter
@Entity
public class Task {

    private static final String VALID_REDMINE_TASKID = "\\d{4}";
    private static final String VALID_LT_TASKID = "LT-\\d{4}";

    @Setter
    @Id
    @GeneratedValue    
    private Integer id;
    private String taskId;
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime startTime;
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime endTime;
    private String comment;
    private long min_per_task;

    /**
     * Constructs a new Task if the parameter is a valid Id. Leaves the time
     * fields <code>null</code> and sets the comment to an empty String.
     *
     * @param taskId is valid if it contains four digits or the characters "LT-"
     * plus four digits.
     * @throws InvalidTaskIdException if the taskId is not valid.
     * @throws NoTaskIdException if taskId is <code>null</code>.
     */
    public Task(String taskId) throws InvalidTaskIdException, NoTaskIdException {
        setTaskId(taskId);        
        this.comment = "";
    }

    /**
     * Constructs a new Task with all the fields set to valid values.
     *
     * @param taskId is valid if it contains four digits or the characters "LT-"
     * plus four digits.
     * @param startTime must be in the form of HH:MM.
     * @param endTime must be in the form of HH:MM.
     * @param comment optional
     * @throws InvalidTaskIdException if the taskId is not valid.
     * @throws NoTaskIdException if taskId is <code>null</code>.
     * @throws NotExpectedTimeOrderException if endTime is not later than
     * startTime.
     * @throws EmptyTimeFieldException if start time or end time is
     * <code>null</code>.
     */
    public Task(String taskId, String startTime, String endTime, String comment) throws NotExpectedTimeOrderException, EmptyTimeFieldException, InvalidTaskIdException, NoTaskIdException {
        this(taskId);
        setStartTime(startTime);
        setEndTime(endTime);
        setComment(comment);
    }

    /**
     * Constructs a new Task with all the fields set to valid values.
     *
     * @param taskId is valid if it contains four digits or the characters "LT-"
     * plus four digits.
     * @param comment optional
     * @throws NotExpectedTimeOrderException if the start time is after end
     * time.
     * @throws InvalidTaskIdException if taskId is not valid.
     * @throws NoTaskIdException if taskId is <code>null</code>.
     */
    public Task(String taskId, int startHour, int startMin, int endHour, int endMin, String comment) throws NotExpectedTimeOrderException, EmptyTimeFieldException, InvalidTaskIdException, NoTaskIdException {
        this(taskId, String.format("%02d:%02d", startHour, startMin), String.format("%02d:%02d", endHour, endMin), comment);
    }

    /**
     * @param taskId is valid if it contains four digits or the characters "LT-"
     * plus four digits.
     * @throws InvalidTaskIdException if taskId is not valid.
     * @throws NoTaskIdException if taskId is <code>null</code>.
     */
    public final void setTaskId(String taskId) throws InvalidTaskIdException, NoTaskIdException {
        if (taskId == null) {
            throw new NoTaskIdException("Missing task id!");
        }
        if (!isValidTaskId(taskId)) {
            throw new InvalidTaskIdException("Invalid task id!");
        }
        this.taskId = taskId;
    }

    public static boolean isValidTaskId(String taskId) {
        return isValidRedmineTaskId(taskId) || isValidLTTaskId(taskId);
    }

    private static boolean isValidRedmineTaskId(String taskId) {
        return taskId.matches(VALID_REDMINE_TASKID);
    }

    private static boolean isValidLTTaskId(String taskId) {
        return taskId.matches(VALID_LT_TASKID);
    }

    /**
     * @throws NotExpectedTimeOrderException if the start time is after end
     * time.
     */
    public void setStartTime(int startHour, int startMin) throws NotExpectedTimeOrderException, EmptyTimeFieldException {
        setStartTime(LocalTime.of(startHour, startMin));
    }

    /**
     * @param startTime in the form of HH:MM
     * @throws NotExpectedTimeOrderException if the start time is after end
     * time.
     * @throws EmptyTimeFieldException if start time or end time is
     * <code>null</code>.
     */
    public final void setStartTime(String startTime) throws NotExpectedTimeOrderException, EmptyTimeFieldException {
        if (startTime == null) {
            throw new EmptyTimeFieldException("Missing start time!");
        }
        setStartTime(LocalTime.parse(startTime));        
    }

    /**
     * @throws NotExpectedTimeOrderException if the start time is after end
     * time.
     */
    public final void setStartTime(LocalTime startTime) throws NotExpectedTimeOrderException, EmptyTimeFieldException {
        if (startTime == null) {
            throw new EmptyTimeFieldException("Missing start time!");
        }

        if (endTime == null || startTime.equals(endTime)) {
            this.startTime = startTime;
        } else if (startTime.isBefore(this.endTime)) {
            this.startTime = startTime;
            endTime = Util.roundToMultipleQuarterHour(this.startTime, endTime);
        } else {
            throw new NotExpectedTimeOrderException("Start time must not be later than end time!");
        }
        setMinPerTask();
    }

    /**
     * @throws EmptyTimeFieldException if end time is <code>null</code>.
     */
    public LocalTime getEndTime() throws EmptyTimeFieldException {
        if (endTime == null) {
            throw new EmptyTimeFieldException("Missing end time!");
        }
        return endTime;
    }

    /**
     * @throws NotExpectedTimeOrderException if the start time is after end
     * time.
     */
    public void setEndTime(int endHour, int endMin) throws NotExpectedTimeOrderException, EmptyTimeFieldException {
        setEndTime(LocalTime.of(endHour, endMin));
    }

    /**
     *
     * @param endTime in the form of HH:MM.
     * @throws NotExpectedTimeOrderException if the start time is after end
     * time.
     * @throws EmptyTimeFieldException if start time or end time is
     * <code>null</code>.
     */
    public final void setEndTime(String endTime) throws NotExpectedTimeOrderException, EmptyTimeFieldException {
        if (endTime == null) {
            throw new EmptyTimeFieldException("Missing end time!");
        }
        setEndTime(LocalTime.parse(endTime));
    }

    /**
     *
     * @throws NotExpectedTimeOrderException if the start time is after end
     * time.
     * @throws EmptyTimeFieldException if start time or end time is
     * <code>null</code>.
     */
    public final void setEndTime(LocalTime endTime) throws NotExpectedTimeOrderException, EmptyTimeFieldException {
        if(endTime == null){
           throw new EmptyTimeFieldException("Missing end time!"); 
        }

        if (!startTime.isAfter(endTime)) {
            this.endTime = Util.roundToMultipleQuarterHour(startTime, endTime);
        } else {
            throw new NotExpectedTimeOrderException("Start time must not be later than end time!");
        }
        setMinPerTask();
    }

    public final void setComment(String comment) {
        if (comment != null) {
            this.comment = comment;
        }
    }

    /**
     * @return @throws EmptyTimeFieldException if start time or end time is
     * <code>null</code>.
     */
    public long getMinPerTask() throws EmptyTimeFieldException {
        LocalTime taskStartTime = getStartTime();
        LocalTime taskEndTime = getEndTime();

        return Duration.between(taskStartTime, taskEndTime).toMinutes();
    }

    @Override
    public String toString() {
        String end = "not yet";
        if (!endTime.equals(startTime)) {
            end = endTime.toString();
        }
        return String.format("Id:%12s   started:%-10s   finished:%-10s \"%s\"", taskId, startTime.toString(), end, comment);
    }

    private static class LocalTimeSerializer extends JsonSerializer<LocalTime> {
        @Override
        public void serialize(LocalTime t, JsonGenerator jg, SerializerProvider sp) throws IOException {
            jg.writeString(t.toString());
        }
    }
    
    private void setMinPerTask(){
        if(startTime != null && endTime != null){
            min_per_task = Duration.between(startTime, endTime).toMinutes();
        }
    }

}
