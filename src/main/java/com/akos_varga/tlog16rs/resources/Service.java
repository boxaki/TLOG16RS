package com.akos_varga.tlog16rs.resources;

import com.akos_varga.tlog16rs.TLOG16RSConfiguration;
import com.akos_varga.tlog16rs.entities.Task;
import com.akos_varga.tlog16rs.entities.TimeLogger;
import com.akos_varga.tlog16rs.entities.WorkDay;
import com.akos_varga.tlog16rs.entities.WorkMonth;
import com.akos_varga.tlog16rs.core.exceptions.EmptyTimeFieldException;
import com.akos_varga.tlog16rs.core.exceptions.FutureWorkException;
import com.akos_varga.tlog16rs.core.exceptions.InvalidTaskIdException;
import com.akos_varga.tlog16rs.core.exceptions.NegativeMinutesOfWorkException;
import com.akos_varga.tlog16rs.core.exceptions.NoTaskIdException;
import com.akos_varga.tlog16rs.core.exceptions.NotExpectedTimeOrderException;
import com.akos_varga.tlog16rs.core.exceptions.NotNewDateException;
import com.akos_varga.tlog16rs.core.exceptions.NotNewMonthException;
import com.akos_varga.tlog16rs.core.exceptions.NotSeparatedTimesException;
import com.akos_varga.tlog16rs.core.exceptions.NotTheSameMonthException;
import com.akos_varga.tlog16rs.core.exceptions.WeekendNotEnabledException;
import com.avaje.ebean.EbeanServer;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author Akos Varga
 */
public final class Service {

    private final EbeanServer server;
    TimeLogger timelogger;

    public Service(TLOG16RSConfiguration config) {
        CreateDatabase database = new CreateDatabase(config);
        server = database.getEbeanServer();
        timelogger = server.find(TimeLogger.class).findUnique();
        if (timelogger == null) {
            timelogger = new TimeLogger("Akos Varga");
        }
    }

    public List<WorkMonth> getAllMonths() {
        return server.find(WorkMonth.class).findList();
    }

    public WorkMonth addMonth(int year, int month) throws NotNewMonthException {
        WorkMonth newWorkMonth = new WorkMonth(year, month);
        timelogger.addNewMonth(newWorkMonth);
        server.save(timelogger); //insert

        return newWorkMonth;
    }

    //nem kell az ellenorzes(is new day), hogy vissza tudja kuldeni, mi volt a hiba
    public WorkDay addNewWeekDay(int requiredMinPerDay, int year, int month, int day) throws NegativeMinutesOfWorkException, FutureWorkException, NotNewMonthException, WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException {
        //if (isNewDay(year, month, day)) {
            WorkDay workDay = new WorkDay(requiredMinPerDay, year, month, day);
            WorkMonth workMonth = getWorkMonthOrAddIfNew(year, month);
            workMonth.addWorkDay(workDay);
            server.update(workMonth);
            server.save(timelogger); //insert  
            return workDay;
        //}
        //return null;
    }

    public WorkDay addNewWeekendDay(int requiredMinPerDay, int year, int month, int day) throws NegativeMinutesOfWorkException, FutureWorkException, NotNewMonthException, WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException {
        //if (isNewDay(year, month, day)) {
            WorkDay workDay = new WorkDay(requiredMinPerDay, year, month, day);
            WorkMonth workMonth = getWorkMonthOrAddIfNew(year, month);
            workMonth.addWorkDay(workDay, true);
            server.update(workMonth);
            server.save(timelogger); //insert  
            return workDay;
        //}
        //return null;

    }

    public boolean isNewMonth(int year, int month) {
        return getMonth(year, month) == null;
    }

    public WorkMonth getMonth(int year, int month) {

        for (WorkMonth existingMonth : timelogger.getMonths()) {
            if (existingMonth.getDate().getYear() == year && existingMonth.getDate().getMonthValue() == month) {
                return existingMonth;
            }
        }

        return null;
    }

    public boolean isNewDay(int year, int month, int day) {
        return getDay(year, month, day) == null;
    }

    public WorkDay getDay(int year, int month, int day) {
        WorkMonth workMonth = getMonth(year, month);
        if (workMonth != null) {
            for (WorkDay existingDay : workMonth.getDays()) {
                if (existingDay.getActualDay().getDayOfMonth() == day) {
                    return existingDay;
                }
            }
        }
        return null;
    }

    public boolean isNewTask(String taskId, int year, int month, int day, String startTime) {
        return getTask(taskId, year, month, day, startTime) == null;
    }

    public Task getTask(String taskId, int year, int month, int day, String startTime) {
        WorkDay workDay = getDay(year, month, day);
        if (workDay != null) {
            for (Task existingTask : workDay.getTasks()) {
                if (existingTask.getTaskId().equals(taskId) && existingTask.getStartTime().toString().equals(startTime)) {
                    return existingTask;
                }
            }
        }
        return null;
    }

    public WorkDay getWorkDayOrAddIfNew(int year, int month, int day) throws WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, NotNewMonthException, FutureWorkException {
        WorkDay workDay = getDay(year, month, day);
        if (workDay == null) {
            workDay = addNewWorkDay(year, month, day);
        }
        return workDay;
    }

    public WorkDay addNewWorkDay(int year, int month, int day) throws WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, NotNewMonthException, FutureWorkException {
        WorkDay newWorkDay = new WorkDay(year, month, day);
        WorkMonth workMonth = getWorkMonthOrAddIfNew(year, month);
        workMonth.addWorkDay(newWorkDay);
        server.save(workMonth);

        return newWorkDay;
    }

    public WorkMonth getWorkMonthOrAddIfNew(int year, int month) throws NotNewMonthException {
        WorkMonth workMonth = getMonth(year, month);
        if (workMonth == null) {
            workMonth = addNewWorkMonth(year, month);
            server.save(timelogger);
        }
        return workMonth;
    }

    private WorkMonth addNewWorkMonth(int year, int month) throws NotNewMonthException {
        WorkMonth newWorkMonth = new WorkMonth(year, month);
        timelogger.addNewMonth(newWorkMonth);
        server.save(timelogger);
        return newWorkMonth;
    }

    public Task modifyTaskIfPossible(Task originalTask, Task newTask, int year, int month, int day) throws WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, NotNewMonthException, FutureWorkException, EmptyTimeFieldException, NotSeparatedTimesException {
        WorkDay workDay = getWorkDayOrAddIfNew(year, month, day);
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
        server.update(workDay);
        WorkMonth workMonth = getMonth(year, month);
        server.update(workMonth);
        server.save(timelogger);

        return newTask;
    }

    Task addNewTask(int year, int month, int day, String taskId, String startTime, String comment) throws NotExpectedTimeOrderException, EmptyTimeFieldException, InvalidTaskIdException, NoTaskIdException, WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, NotNewMonthException, FutureWorkException, NotSeparatedTimesException {
        Task newTask = new Task(taskId, startTime, startTime, comment);
        WorkDay workDay = getWorkDayOrAddIfNew(year, month, day);
        workDay.addTask(newTask);

        server.save(workDay);
        WorkMonth wm = getMonth(year, month);
        server.save(wm);
        server.save(timelogger);

        return newTask;
    }

    Task finishTask(String taskId, int year, int month, int day, String startTime, String endTime) throws NotExpectedTimeOrderException, EmptyTimeFieldException, InvalidTaskIdException, NoTaskIdException, WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, NotNewMonthException, FutureWorkException, NotSeparatedTimesException {
        Task savedTask = getTask(taskId, year, month, day, startTime);
        Task newTask = new Task(taskId, startTime, endTime, savedTask == null ? "" : savedTask.getComment());
        newTask = modifyTaskIfPossible(savedTask, newTask, year, month, day);

        return newTask;
    }

    Task modifyOrCreateTask(int year, int month, int day, String taskId, String startTime, String newTaskId, String newStartTime, String newEndTime, String newComment) throws NotExpectedTimeOrderException, EmptyTimeFieldException, InvalidTaskIdException, NoTaskIdException, WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, NotNewMonthException, FutureWorkException, NotSeparatedTimesException {
        Task savedTask = getTask(taskId, year, month, day, startTime);
        Task newTask = new Task(newTaskId, newStartTime, newEndTime, newComment);
        newTask = modifyTaskIfPossible(savedTask, newTask, year, month, day);

        return newTask;
    }

    Task deleteTask(int year, int month, int day, String taskId, String startTime) {

        Task taskToDelete = getTask(taskId, year, month, day, startTime);
        if (taskToDelete != null) {
            WorkDay workDay = getDay(year, month, day);
            workDay.getTasks().remove(taskToDelete);
            server.update(workDay);
            WorkMonth workMonth = getMonth(year, month);
            server.update(workMonth);
            server.save(timelogger);
        }
        return taskToDelete;

    }

    void deleteAll() {
        server.delete(timelogger);
        timelogger = new TimeLogger("Akos Varga");
        server.save(timelogger);
    }

    List<WorkDay> getDaysOfMonth(int year, int month) throws NotNewMonthException {
        WorkMonth workMonth = getWorkMonthOrAddIfNew(year, month);
        server.save(timelogger);

        List<WorkDay> daysOfMonth = null;
        if (workMonth != null) {
            daysOfMonth = workMonth.getDays();
        }

        return daysOfMonth;

    }

    List<Task> getTasksOfDay(int year, int month, int day) throws WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, NotNewMonthException, FutureWorkException {

        WorkDay workDay = getWorkDayOrAddIfNew(year, month, day);
        server.save(timelogger);  //at kéne rakni a getWorkDayOr... meth-be, meg átnézni a server dolgokat
        for (WorkDay wd : server.find(WorkDay.class).findList()) {
            if (wd.getActualDay().equals(LocalDate.of(year, month, day))) {
                workDay = wd;
            }
        }
        
        
        List<Task> tasksOfDay = null;
        if (workDay != null) {
            tasksOfDay = workDay.getTasks();
        }
        return tasksOfDay;
    }

}
