package com.akos_varga.tlog16rs.resources;

import com.akos_varga.tlog16rs.core.beans.StartTaskRB;
import com.akos_varga.tlog16rs.core.beans.FinishTaskRB;
import com.akos_varga.tlog16rs.core.beans.WorkDayRB;
import com.akos_varga.tlog16rs.core.beans.DeleteTaskRB;
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
public class TLOG16RSResource {

    //TimeLogger timelogger = new TimeLogger();
    TimeLogger timelogger = initTestData();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<WorkMonth> getAllMonths() {
        return timelogger.getMonths();   
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
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
    @Produces(MediaType.APPLICATION_JSON)
    public WorkDay addNewDay(WorkDayRB dayRB) {
        WorkDay workDay = null;

        int year = dayRB.getYear();
        int month = dayRB.getMonth();
        int day = dayRB.getDay();

        if (isNewDay(timelogger, year, month, day)) {
            try {
                workDay = new WorkDay(dayRB.getRequiredMinPerDay(), year, month, day);

                WorkMonth workMonth = getMonth(timelogger, year, month);
                if (workMonth == null) {
                    workMonth = new WorkMonth(year, month);
                    timelogger.addNewMonth(workMonth);
                }
                workMonth.addWorkDay(workDay);
            } catch (NegativeMinutesOfWorkException | FutureWorkException | NotNewMonthException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException ex) {
                log.error(ex.getClass() + " " + ex.getMessage());
            }
        }
        return workDay;
    }

    @POST
    @Path("/workdays/tasks/start")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Task addNewTask(StartTaskRB task) {
        Task newTask = null;
        int year = task.getYear();
        int month = task.getMonth();
        int day = task.getDay();

        try {
            newTask = new Task(task.getTaskId(), task.getStartTime(), task.getStartTime(), task.getComment());

            WorkDay workDay = getDay(timelogger, year, month, day);
            if (workDay == null) {
                workDay = new WorkDay(year, month, day);
                WorkMonth workMonth = getMonth(timelogger, year, month);
                if (workMonth == null) {
                    workMonth = new WorkMonth(year, month);
                    timelogger.addNewMonth(workMonth);
                }
                workMonth.addWorkDay(workDay);
            }
            workDay.addTask(newTask);

        } catch (NotNewMonthException | NotExpectedTimeOrderException | EmptyTimeFieldException | InvalidTaskIdException | NoTaskIdException | FutureWorkException | NotSeparatedTimesException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
        }
        return newTask;
    }

    @GET
    @Path("/{year}/{month}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<WorkDay> getDaysOfMonth(@PathParam(value = "year") int year, @PathParam(value = "month") int month) {
        List<WorkDay> daysOfMonth = null;
        WorkMonth workMonth = getMonth(timelogger, year, month);
        if (workMonth == null) {
            workMonth = new WorkMonth(year, month);
        }

        return workMonth.getDays();
    }

    @GET
    @Path("/{year}/{month}/{day}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Task> getTasksOfDay(@PathParam(value = "year") int year, @PathParam(value = "month") int month, @PathParam(value = "day") int day) {
        WorkDay workDay = getDay(timelogger, year, month, day);
        try {
            if (workDay == null) {
                workDay = new WorkDay(year, month, day);
                WorkMonth workMonth = getMonth(timelogger, year, month);
                if (workMonth == null) {
                    workMonth = new WorkMonth(year, month);
                    timelogger.addNewMonth(workMonth);
                }
                workMonth.addWorkDay(workDay);
            }
        } catch (FutureWorkException | NotNewMonthException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
        }
        return workDay.getTasks();
    }

    @PUT
    @Path("/workdays/tasks/modify")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Task modifyOrCreateTask(FinishTaskRB taskRB) {
        int year = taskRB.getYear();
        int month = taskRB.getMonth();
        int day = taskRB.getDay();

        Task savedTask = getTask(timelogger, taskRB.getTaskId(), year, month, day, taskRB.getStartTime());
        Task newTask = null;
        WorkDay workDay = null;
        try {
            newTask = new Task(taskRB.getNewTaskId(), taskRB.getNewStartTime(), taskRB.getNewEndTime(), taskRB.getNewComment());
            workDay = getDay(timelogger, year, month, day);
            if (workDay == null) {
                workDay = new WorkDay(year, month, day);

                WorkMonth workMonth = getMonth(timelogger, year, month);
                if (workMonth == null) {
                    workMonth = new WorkMonth(year, month);
                    timelogger.addNewMonth(workMonth);
                }
                workMonth.addWorkDay(workDay);
            }
            workDay.getTasks().remove(savedTask);
            workDay.addTask(newTask);
            
        } catch (NotExpectedTimeOrderException | EmptyTimeFieldException | InvalidTaskIdException | NoTaskIdException | FutureWorkException | NotNewMonthException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
        } catch (NotSeparatedTimesException ex) {
            if (savedTask != null) {
                try {
                    workDay.addTask(savedTask);
                    newTask = savedTask;
                } catch (EmptyTimeFieldException | NotSeparatedTimesException ex1) { //should never throw it                        
                }
            }
        }
        return newTask;
        
    }

    @PUT
    @Path("/dorkdays/tasks/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Task deleteTask(DeleteTaskRB taskRB) {
        int year = taskRB.getYear();
        int month = taskRB.getMonht();
        int day = taskRB.getDay();

        Task taskToDelete = getTask(timelogger, taskRB.getTaskId(), year, month, day, taskRB.getStartTime());
        if (taskToDelete != null) {
            getDay(timelogger, year, month, day).getTasks().remove(taskToDelete);
        }
        return taskToDelete;
    }
        
    private TimeLogger initTestData() {

        TimeLogger tl = new TimeLogger();
        try {

            Task t = new Task("1234", "08:00", "09:00", "");
            Task t2 = new Task("1234", "09:20", "10:20", "");
            Task t3 = new Task("1234", "10:22", "12:20", "");
            WorkDay wd = new WorkDay(2018, 2, 20);
            WorkMonth wm = new WorkMonth(2018, 2);
            wd.addTask(t);
            wd.addTask(t2);
            wd.addTask(t3);

            WorkDay wd2 = new WorkDay(2018, 2, 19);
            wd2.addTask(t);
            wd2.addTask(t2);
            wd2.addTask(t3);

            wm.addWorkDay(wd);
            wm.addWorkDay(wd2);
            tl.addNewMonth(wm);

        } catch (NotExpectedTimeOrderException | EmptyTimeFieldException | InvalidTaskIdException | NoTaskIdException | FutureWorkException | NotSeparatedTimesException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException | NotNewMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
        }
        return tl;
    }
    
}
