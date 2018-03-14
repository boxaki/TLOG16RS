package com.akos_varga.tlog16rs.resources;

import com.akos_varga.tlog16rs.TLOG16RSConfiguration;
import com.akos_varga.tlog16rs.core.beans.StartTaskRB;
import com.akos_varga.tlog16rs.core.beans.ModifyTaskRB;
import com.akos_varga.tlog16rs.core.beans.WorkDayRB;
import com.akos_varga.tlog16rs.core.beans.DeleteTaskRB;
import com.akos_varga.tlog16rs.core.beans.FinishTaskRB;
import com.akos_varga.tlog16rs.entities.Task;
import com.akos_varga.tlog16rs.entities.TimeLogger;
import com.akos_varga.tlog16rs.entities.WorkDay;
import com.akos_varga.tlog16rs.entities.WorkMonth;
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
import com.avaje.ebean.EbeanServer;
import java.time.LocalDate;

@Slf4j
@Path("/timelogger/workmonths")
@Produces(MediaType.APPLICATION_JSON)
public class TLOG16RSResource {

    private final EbeanServer server;
    TimeLogger timelogger;

    public TLOG16RSResource(TLOG16RSConfiguration config) {
        CreateDatabase database = new CreateDatabase(config);
        server = database.getEbeanServer();
        timelogger = server.find(TimeLogger.class).findUnique();
        if (timelogger == null) {
            timelogger = new TimeLogger("Akos Varga");
        }
    }

    @GET
    public List<WorkMonth> getAllMonths() {
        return server.find(WorkMonth.class).findList();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public WorkMonth addNewMonth(WorkMonthRB month) {
        WorkMonth newWorkMonth = null;

        try {
            newWorkMonth = new WorkMonth(month.getYear(), month.getMonth());
            timelogger.addNewMonth(newWorkMonth);
            server.save(timelogger); //insert
        } catch (NotNewMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
        }
        return newWorkMonth;
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
                WorkMonth workMonth = getWorkMonthOrAddIfNew(server, timelogger, year, month);
                workMonth.addWorkDay(workDay);
                server.update(workMonth);
                server.save(timelogger); //insert
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
            WorkDay workDay = getWorkDayOrAddIfNew(server, timelogger, year, month, day);
            workDay.addTask(newTask);

            server.save(workDay);
            WorkMonth wm = getMonth(timelogger, year, month);
            server.save(wm);
            server.save(timelogger);

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
            newTask = new Task(taskRB.getTaskId(), taskRB.getStartTime(), taskRB.getEndTime(), savedTask == null ? "" : savedTask.getComment());
            newTask = modifyTaskIfPossible(server, timelogger, savedTask, newTask, year, month, day);

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
            newTask = modifyTaskIfPossible(server, timelogger, savedTask, newTask, year, month, day);

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
            WorkDay workDay = getDay(timelogger, year, month, day);
            workDay.getTasks().remove(taskToDelete);
            server.update(workDay);
            WorkMonth workMonth = getMonth(timelogger, year, month);
            server.update(workMonth);
            server.save(timelogger);
        }
        return taskToDelete;
    }

    @PUT
    @Path("/deleteall")
    public List<WorkMonth> deleteAll() {
        //vagy toroljem a honapokat, akkor marad az id
        server.delete(timelogger);
        timelogger = new TimeLogger("Akos Varga");
        server.save(timelogger);

        return null;       
    }

    @GET
    @Path("/{year}/{month}")
    public List<WorkDay> getDaysOfMonth(@PathParam(value = "year") int year, @PathParam(value = "month") int month) {
        WorkMonth workMonth = null;
        try {
            workMonth = getWorkMonthOrAddIfNew(server, timelogger, year, month);
            server.save(timelogger);

        } catch (NotNewMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
        }

        List<WorkDay> daysOfMonth = null;
        if (workMonth != null) {
            daysOfMonth = workMonth.getDays();
        } 
        
        return daysOfMonth;

    }

    @GET
    @Path("/{year}/{month}/{day}")
    public List<Task> getTasksOfDay(@PathParam(value = "year") int year, @PathParam(value = "month") int month, @PathParam(value = "day") int day) {
        WorkDay workDay = null;
        try {
            workDay = getWorkDayOrAddIfNew(server, timelogger, year, month, day);
            server.save(timelogger);
            for (WorkDay wd : server.find(WorkDay.class).findList()) {
                if (wd.getActualDay().equals(LocalDate.of(year, month, day))) {
                    workDay = wd;
                }
            }

        } catch (FutureWorkException | NotNewMonthException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
        }
        
        List<Task> tasksOfDay = null;
        if(workDay != null){
            tasksOfDay = workDay.getTasks();
        }
        return tasksOfDay;
    }

}
