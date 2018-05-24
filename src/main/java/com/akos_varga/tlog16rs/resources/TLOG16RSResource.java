package com.akos_varga.tlog16rs.resources;

import com.akos_varga.tlog16rs.TLOG16RSConfiguration;
import com.akos_varga.tlog16rs.core.beans.StartTaskRB;
import com.akos_varga.tlog16rs.core.beans.ModifyTaskRB;
import com.akos_varga.tlog16rs.core.beans.WorkDayRB;
import com.akos_varga.tlog16rs.core.beans.DeleteTaskRB;
import com.akos_varga.tlog16rs.core.beans.FinishTaskRB;
import com.akos_varga.tlog16rs.entities.Task;
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
import javax.ws.rs.core.Response;

@Slf4j
@Path("/timelogger/workmonths")
// a produces-t ki kell e szedni a response miatt vagy maradhat?
@Produces(MediaType.APPLICATION_JSON)
public class TLOG16RSResource {

    Service service;

    public TLOG16RSResource(TLOG16RSConfiguration config) {
        service = new Service(config);       
    }

    @GET
    public Response getAllMonths() {
        
        return Response.ok(service.getAllMonths()).build();

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addNewMonth(WorkMonthRB month) {

        try {
            WorkMonth newWorkMonth = service.addMonth(month.getYear(), month.getMonth()); 
         
            return Response.ok(newWorkMonth).build();
        } catch (NotNewMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        }

    }

    @POST
    @Path("/workdays")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addNewDay(WorkDayRB dayRB) {

        try {
            WorkDay workDay = service.addNewWeekDay(dayRB.getRequiredMinPerDay(), dayRB.getYear(), dayRB.getMonth(), dayRB.getDay());
            if (workDay != null) {
                return Response.ok(workDay).build();
            } else {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

        } catch (NegativeMinutesOfWorkException | FutureWorkException | NotNewMonthException | NotNewDateException | NotTheSameMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        } catch (WeekendNotEnabledException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.FOUND).build(); //307?
        }

    }

    @POST
    @Path("/weekends")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addWeekendDay(WorkDayRB dayRB) {

        try {
            WorkDay workDay = service.addNewWeekendDay(dayRB.getRequiredMinPerDay(), dayRB.getYear(), dayRB.getMonth(), dayRB.getDay());
            if (workDay != null) {
                return Response.ok(workDay).build();
            } else {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

        } catch (NegativeMinutesOfWorkException | FutureWorkException | NotNewMonthException | NotNewDateException | NotTheSameMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        } catch (WeekendNotEnabledException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(418).build();
        }

    }

    @POST
    @Path("/workdays/tasks/start")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addNewTask(StartTaskRB task) {
        Response response = Response.ok().build();

        try {
            Task newTask = service.addNewTask(task.getYear(), task.getMonth(), task.getDay(), task.getTaskId(), task.getStartTime(), task.getComment());
            response = Response.ok(newTask).build();

        } catch (NotNewMonthException | NotExpectedTimeOrderException | EmptyTimeFieldException | InvalidTaskIdException | NoTaskIdException | FutureWorkException | NotSeparatedTimesException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            response = Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        }
        return response;
    }

    @PUT
    @Path("/workdays/tasks/finish")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response finishTask(FinishTaskRB taskRB) {

        try {
            Task newTask = service.finishTask(taskRB.getTaskId(), taskRB.getYear(), taskRB.getMonth(), taskRB.getDay(), taskRB.getStartTime(), taskRB.getEndTime());
            return Response.ok(newTask).build();

        } catch (NotExpectedTimeOrderException | EmptyTimeFieldException | InvalidTaskIdException | NoTaskIdException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException | NotNewMonthException | FutureWorkException | NotSeparatedTimesException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        }
    }

    @PUT
    @Path("/workdays/tasks/modify")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyOrCreateTask(ModifyTaskRB taskRB) {
        Response response = Response.status(Response.Status.OK).build();

        try {
            Task newTask = service.modifyOrCreateTask(taskRB.getYear(), taskRB.getMonth(), taskRB.getDay(), taskRB.getTaskId(), taskRB.getStartTime(), taskRB.getNewTaskId(), taskRB.getNewStartTime(), taskRB.getNewEndTime(), taskRB.getNewComment());
            response = Response.ok(newTask).build();

        } catch (NotExpectedTimeOrderException | EmptyTimeFieldException | InvalidTaskIdException | NoTaskIdException | FutureWorkException | NotNewMonthException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException | NotSeparatedTimesException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            response = Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        }
        return response;

    }

    @PUT
    @Path("/workdays/tasks/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteTask(DeleteTaskRB taskRB) {

        Task deletedTask = service.deleteTask(taskRB.getYear(), taskRB.getMonth(), taskRB.getDay(), taskRB.getTaskId(), taskRB.getStartTime());
        if (deletedTask != null) {
            return Response.ok(deletedTask).build();
        }
        return Response.noContent().build();
    }

    @PUT
    @Path("/deleteall")
    public Response deleteAll() {
        service.deleteAll();

        //vagy toroljem a honapokat, akkor marad az id
        //return Response.ok().build();
        return Response.noContent().build();
    }

    @GET
    @Path("/{year}/{month}")
    public Response getDaysOfMonth(@PathParam(value = "year") int year, @PathParam(value = "month") int month) {
        Response response = Response.ok().build();

        try {
            List<WorkDay> daysOfMonth = service.getDaysOfMonth(year, month);
            response = Response.ok(daysOfMonth).build();

        } catch (NotNewMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            response = Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        }

        return response;

    }

    @GET
    @Path("/{year}/{month}/{day}")
    public Response getTasksOfDay(@PathParam(value = "year") int year, @PathParam(value = "month") int month, @PathParam(value = "day") int day) {
        try {
            List<Task> tasksOfDay = service.getTasksOfDay(year, month, day);
            return Response.ok(tasksOfDay).build();
            

        } catch (FutureWorkException | NotNewMonthException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        }

    }

}
