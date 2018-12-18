package com.akos_varga.tlog16rs.resources;

import com.akos_varga.tlog16rs.TLOG16RSConfiguration;
import com.akos_varga.tlog16rs.core.exceptions.AuthenticationFailureException;
import com.akos_varga.tlog16rs.entities.Task;
import com.akos_varga.tlog16rs.entities.User;
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
import com.akos_varga.tlog16rs.core.exceptions.UserExistsException;
import com.akos_varga.tlog16rs.core.exceptions.UserNotFoundException;
import com.akos_varga.tlog16rs.core.exceptions.WeekendNotEnabledException;
import com.akos_varga.tlog16rs.entities.Util;
import static com.akos_varga.tlog16rs.resources.HashService.createHash;
import static com.akos_varga.tlog16rs.resources.HashService.createSalt;

import com.avaje.ebean.EbeanServer;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 *
 * @author Akos Varga
 */
public final class DBService {

    private final EbeanServer server;

    public DBService(TLOG16RSConfiguration config) {
        CreateDatabase database = new CreateDatabase(config);
        server = database.getEbeanServer();
    }

    public void register(String name, String password) throws UserExistsException, NoSuchAlgorithmException {
        if (isUserExists(name)) {
            throw new UserExistsException("Username already exists!");
        }

        String salt = createSalt();
        String encodedPassword = createHash(password, salt);
        User newUser = new User(name, encodedPassword, salt);
        server.save(newUser);
    }

    public void login(String name, String password) throws UserNotFoundException, NoSuchAlgorithmException, AuthenticationFailureException {
        User userToAuthenticate = getUser(name);

        String hashedPassword = createHash(password, userToAuthenticate.getSalt());
        if (!hashedPassword.equals(userToAuthenticate.getPassword())) {
            throw new AuthenticationFailureException("Password does not match!");
        }
    }

    public List<WorkMonth> getAllMonths(String userName) throws UserNotFoundException {
        User user = getUser(userName);

        return user.getMonths();
    }

    public WorkMonth addMonth(String userName, int year, int month) throws NotNewMonthException, UserNotFoundException {
        User user = getUser(userName);
        WorkMonth newWorkMonth = new WorkMonth(year, month);
        user.addNewMonth(newWorkMonth);

        server.save(user);

        return newWorkMonth;
    }

    public List<WorkDay> getDaysOfMonth(String userName, int year, int month) throws NotNewMonthException, UserNotFoundException {
        User user = getUser(userName);
        WorkMonth workMonth = getWorkMonthOrAddIfNew(user, year, month);
        server.save(user);

        List<WorkDay> daysOfMonth = null;
        if (workMonth != null) {
            daysOfMonth = workMonth.getDays();
        }

        return daysOfMonth;
    }

    public WorkDay addNewWeekDay(String userName, int requiredMinPerDay, int year, int month, int day) throws NegativeMinutesOfWorkException, FutureWorkException, NotNewMonthException, WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, UserNotFoundException {
        User user = getUser(userName);
        WorkDay workDay = new WorkDay(requiredMinPerDay, year, month, day);
        WorkMonth workMonth = getWorkMonthOrAddIfNew(user, year, month);
        workMonth.addWorkDay(workDay);

        server.update(workMonth);
        server.save(user);

        return workDay;
    }

    public WorkDay addNewWeekendDay(String userName, int requiredMinPerDay, int year, int month, int day) throws NegativeMinutesOfWorkException, FutureWorkException, NotNewMonthException, WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, UserNotFoundException {
        User user = getUser(userName);
        WorkDay workDay = new WorkDay(requiredMinPerDay, year, month, day);
        WorkMonth workMonth = getWorkMonthOrAddIfNew(user, year, month);
        workMonth.addWorkDay(workDay, true);

        server.update(workMonth);
        server.save(user);

        return workDay;
    }

    public List<Task> getTasksOfDay(String userName, int year, int month, int day) throws WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, NotNewMonthException, FutureWorkException, UserNotFoundException {
        User user = getUser(userName);

        WorkDay workDay = getWorkDayOrAddIfNew(user, year, month, day);
        server.save(user);

        return workDay.getTasks();
    }

    // atnevezes startNewTask?
    public Task addNewTask(String userName, int year, int month, int day, String taskId, String startTime, String comment) throws NotExpectedTimeOrderException, EmptyTimeFieldException, InvalidTaskIdException, NoTaskIdException, WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, NotNewMonthException, FutureWorkException, NotSeparatedTimesException, UserNotFoundException {
        User user = getUser(userName);
        Task newTask = new Task(taskId, startTime, startTime, comment);
        WorkDay workDay = getWorkDayOrAddIfNew(user, year, month, day);
        workDay.addTask(newTask);

        server.save(workDay);
        WorkMonth wm = getMonth(user, year, month);
        server.save(wm);
        server.save(user);

        return newTask;
    }

    public Task finishTask(String userName, String taskId, int year, int month, int day, String startTime, String endTime) throws NotExpectedTimeOrderException, EmptyTimeFieldException, InvalidTaskIdException, NoTaskIdException, WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, NotNewMonthException, FutureWorkException, NotSeparatedTimesException, UserNotFoundException {
        User user = getUser(userName);
        Task savedTask = getTask(user, taskId, year, month, day, startTime);
        Task newTask = new Task(taskId, startTime, endTime, savedTask == null ? "" : savedTask.getComment());
        newTask = modifyTaskIfPossible(user, savedTask, newTask, year, month, day);

        return newTask;
    }

    public Task modifyOrCreateTask(String userName, int year, int month, int day, String taskId, String startTime, String newTaskId, String newStartTime, String newEndTime, String newComment) throws NotExpectedTimeOrderException, EmptyTimeFieldException, InvalidTaskIdException, NoTaskIdException, WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, NotNewMonthException, FutureWorkException, NotSeparatedTimesException, UserNotFoundException {
        User user = getUser(userName);
        Task savedTask = getTask(user, taskId, year, month, day, startTime);
        Task newTask = new Task(newTaskId, newStartTime, newEndTime, newComment);
        newTask = modifyTaskIfPossible(user, savedTask, newTask, year, month, day);

        return newTask;
    }

    public Task deleteTask(String userName, int year, int month, int day, String taskId, String startTime) throws UserNotFoundException {
        User user = getUser(userName);

        Task taskToDelete = getTask(user, taskId, year, month, day, startTime);
        if (taskToDelete != null) {
            WorkDay workDay = getWorkDay(user, year, month, day);
            workDay.getTasks().remove(taskToDelete);
            server.update(workDay);
            WorkMonth workMonth = getMonth(user, year, month);
            server.update(workMonth);
            server.save(user);
        }
        return taskToDelete;
    }

    private User getUser(String name) throws UserNotFoundException {

        User user = server.find(User.class)
                .where()
                .eq("name", name)
                .findUnique();  // findUser method-be

        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        return user;
    }

    private WorkMonth getWorkMonthOrAddIfNew(User user, int year, int month) throws NotNewMonthException {
        WorkMonth workMonth = getMonth(user, year, month);
        if (workMonth == null) {
            workMonth = addNewWorkMonth(user, year, month);
            server.save(user);
        }
        return workMonth;
    }

    private WorkMonth getMonth(User user, int year, int month) {

        for (WorkMonth existingMonth : user.getMonths()) {
            if (existingMonth.getDate().getYear() == year && existingMonth.getDate().getMonthValue() == month) {
                
                return existingMonth;
            }
        }

        return null;
    }

    private WorkMonth addNewWorkMonth(User user, int year, int month) throws NotNewMonthException {
        WorkMonth newWorkMonth = new WorkMonth(year, month);
        user.addNewMonth(newWorkMonth);

        server.save(user);

        return newWorkMonth;
    }

    private WorkDay getWorkDayOrAddIfNew(User user, int year, int month, int day) throws WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, NotNewMonthException, FutureWorkException {
        WorkDay workDay = getWorkDay(user, year, month, day);
        if (workDay == null) {
            workDay = addNewWorkDay(user, year, month, day);
        }
        return workDay;
    }

    private WorkDay getWorkDay(User user, int year, int month, int day) {
        WorkMonth workMonth = getMonth(user, year, month);
        if (workMonth != null) {
            for (WorkDay existingDay : workMonth.getDays()) {
                if (existingDay.getActualDay().getDayOfMonth() == day) {
                    return existingDay;
                }
            }
        }
        return null;
    }

    private WorkDay addNewWorkDay(User user, int year, int month, int day) throws WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, NotNewMonthException, FutureWorkException {
        WorkDay newWorkDay = new WorkDay(year, month, day);
        WorkMonth workMonth = getWorkMonthOrAddIfNew(user, year, month);
        workMonth.addWorkDay(newWorkDay);

        server.save(workMonth);

        return newWorkDay;
    }

    private Task getTask(User user, String taskId, int year, int month, int day, String startTime) {
        WorkDay workDay = getWorkDay(user, year, month, day);
        if (workDay != null) {
            for (Task existingTask : workDay.getTasks()) {
                if (existingTask.getTaskId().equals(taskId) && existingTask.getStartTime().equals(Util.parseTime(startTime))) {
                    return existingTask;
                }
            }
        }

        return null;
    }

    private Task modifyTaskIfPossible(User user, Task existingTask, Task newTask, int year, int month, int day) throws WeekendNotEnabledException, NotNewDateException, NotTheSameMonthException, NotNewMonthException, FutureWorkException, EmptyTimeFieldException, NotSeparatedTimesException {
        WorkDay workDay = getWorkDayOrAddIfNew(user, year, month, day);
        if (existingTask != null) {
            workDay.getTasks().remove(existingTask);
        }
        try {
            workDay.addTask(newTask);
        } catch (NotSeparatedTimesException ex) {
            if (existingTask != null) {
                workDay.addTask(existingTask);
                newTask = existingTask;
            }
        }
        server.update(workDay);
        WorkMonth workMonth = getMonth(user, year, month);
        server.update(workMonth);
        server.save(user);

        return newTask;
    }

    public void deleteAll(String userName) throws UserNotFoundException {
        User user = getUser(userName);

        server.delete(user);
    }

    private boolean isUserExists(String name) { // findUser-t hasznalja
        try {
            return getUser(name) != null;

        } catch (UserNotFoundException ex) {
            return false;
        }
    }
}
