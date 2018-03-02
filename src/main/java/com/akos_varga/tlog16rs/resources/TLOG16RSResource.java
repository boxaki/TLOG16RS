package com.akos_varga.tlog16rs.resources;

import com.akos_varga.tlog16rs.core.beans.StartTaskRB;
import com.akos_varga.tlog16rs.core.beans.ModifyTaskRB;
import com.akos_varga.tlog16rs.core.beans.WorkDayRB;
import com.akos_varga.tlog16rs.core.beans.DeleteTaskRB;
import com.akos_varga.tlog16rs.core.beans.FinishTaskRB;
import com.akos_varga.tlog16rs.core.beans.Task;
import com.akos_varga.tlog16rs.core.beans.TimeLogger;
import com.akos_varga.tlog16rs.core.beans.WorkDay;
import com.akos_varga.tlog16rs.core.beans.WorkMonth;
import com.akos_varga.tlog16rs.core.beans.WorkMonthRB;
import com.akos_varga.tlog16rs.core.exceptions.*;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import static com.akos_varga.tlog16rs.resources.Service.*;

@Slf4j
@Path("/timelogger/workmonths")
@Produces(MediaType.APPLICATION_JSON)
public class TLOG16RSResource {

    TimeLogger timelogger = new TimeLogger();
    
    @GET
    public List<WorkMonth> getAllMonths() {
        return timelogger.getMonths();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public WorkMonth addNewMonth(WorkMonthRB month) {
        WorkMonth workMonth = new WorkMonth(month.getYear(), month.getMonth());
        try {
            timelogger.addNewMonth(workMonth);
        } catch (NotNewMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
        }
        return workMonth;
    }

    @POST
    @Path("/workdays")
    @Consumes(MediaType.APPLICATION_JSON)
    public WorkDay addNewDay(WorkDayRB dayRB) {
        WorkDay workDay = null;
        
        int year = dayRB.getYear();
        int month = dayRB.getMonth();
        int day = dayRB.getDay();

        try {
            if (isNewDay(timelogger, year, month, day)) {
                workDay = new WorkDay(dayRB.getRequiredMinPerDay(), year, month, day);
                WorkMonth workMonth = getWorkMonthOrCreateIfNotExist(timelogger, year, month);
                workMonth.addWorkDay(workDay);                
            }
        } catch (NegativeMinutesOfWorkException | FutureWorkException | NotNewMonthException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
        }
        return workDay;
    }

    @POST
    @Path("/workdays/tasks/start")
    @Consumes(MediaType.APPLICATION_JSON)
    public Task addNewTask(StartTaskRB task) {
        Task newTask = null;
        
        int year = task.getYear();
        int month = task.getMonth();
        int day = task.getDay();

        try {
            newTask = new Task(task.getTaskId(), task.getStartTime(), task.getStartTime(), task.getComment());
            WorkDay workDay = getWorkDayOrCreateIfNotExist(timelogger, year, month, day);
            workDay.addTask(newTask);

        } catch (NotNewMonthException | NotExpectedTimeOrderException | EmptyTimeFieldException | InvalidTaskIdException | NoTaskIdException | FutureWorkException | NotSeparatedTimesException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
        }
        return newTask;
    }

    @PUT
    @Path("/workdays/tasks/finish")
    @Consumes(MediaType.APPLICATION_JSON)
    public Task finishTask(FinishTaskRB taskRB) {
        int year = taskRB.getYear();
        int month = taskRB.getMonth();
        int day = taskRB.getDay();

        Task savedTask = getTask(timelogger, taskRB.getTaskId(), year, month, day, taskRB.getStartTime());
        Task newTask = null;            
        try {
            newTask = new Task(taskRB.getTaskId(), taskRB.getStartTime(), taskRB.getEndTime(), savedTask == null?"":savedTask.getComment() );
            newTask = modifyTaskIfPossible(timelogger, savedTask, newTask, year, month, day);           

        } catch (NotExpectedTimeOrderException | EmptyTimeFieldException | InvalidTaskIdException | NoTaskIdException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException | NotNewMonthException | FutureWorkException | NotSeparatedTimesException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
        }
      
        return newTask;
    }

    @PUT
    @Path("/workdays/tasks/modify")
    @Consumes(MediaType.APPLICATION_JSON)
    public Task modifyOrCreateTask(ModifyTaskRB taskRB) {
        int year = taskRB.getYear();
        int month = taskRB.getMonth();
        int day = taskRB.getDay();

        Task savedTask = getTask(timelogger, taskRB.getTaskId(), year, month, day, taskRB.getStartTime());
        Task newTask = null;        
        try {
            newTask = new Task(taskRB.getNewTaskId(), taskRB.getNewStartTime(), taskRB.getNewEndTime(), taskRB.getNewComment());
            newTask = modifyTaskIfPossible(timelogger, savedTask, newTask, year, month, day);
           
        } catch (NotExpectedTimeOrderException | EmptyTimeFieldException | InvalidTaskIdException | NoTaskIdException | FutureWorkException | NotNewMonthException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException | NotSeparatedTimesException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
        }
        return newTask;

    }

    @PUT
    @Path("/workdays/tasks/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Task deleteTask(DeleteTaskRB taskRB) {
        int year = taskRB.getYear();
        int month = taskRB.getMonth();
        int day = taskRB.getDay();

        Task taskToDelete = getTask(timelogger, taskRB.getTaskId(), year, month, day, taskRB.getStartTime());
        if (taskToDelete != null) {
            getDay(timelogger, year, month, day).getTasks().remove(taskToDelete);
        }
        return taskToDelete;
    }

    @PUT
    @Path("/deleteall")
    public List<WorkMonth> deleteAll() {
        timelogger = new TimeLogger();
        if (timelogger.getMonths().isEmpty()) {
            return null;
        } else {
            return timelogger.getMonths();
        }
    }

    @GET
    @Path("/{year}/{month}")
    public List<WorkDay> getDaysOfMonth(@PathParam(value = "year") int year, @PathParam(value = "month") int month) {  
        WorkMonth workMonth = null;
        try {
            workMonth = getWorkMonthOrCreateIfNotExist(timelogger, year, month);
            
        } catch (NotNewMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
        }
       
        return workMonth.getDays();
    }

    @GET
    @Path("/{year}/{month}/{day}")
    public List<Task> getTasksOfDay(@PathParam(value = "year") int year, @PathParam(value = "month") int month, @PathParam(value = "day") int day) {
        WorkDay workDay = null; //getDay(timelogger, year, month, day);
        try {
            workDay = getWorkDayOrCreateIfNotExist(timelogger, year, month, day);
           
        } catch (FutureWorkException | NotNewMonthException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
        }
        return workDay.getTasks();
    }

}
