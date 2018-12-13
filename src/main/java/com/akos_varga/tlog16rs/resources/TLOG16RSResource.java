package com.akos_varga.tlog16rs.resources;

import com.akos_varga.tlog16rs.TLOG16RSConfiguration;
import com.akos_varga.tlog16rs.core.beans.StartTaskRB;
import com.akos_varga.tlog16rs.core.beans.ModifyTaskRB;
import com.akos_varga.tlog16rs.core.beans.WorkDayRB;
import com.akos_varga.tlog16rs.core.beans.DeleteTaskRB;
import com.akos_varga.tlog16rs.core.beans.FinishTaskRB;
import com.akos_varga.tlog16rs.core.beans.UserRB;
import com.akos_varga.tlog16rs.entities.Task;
import com.akos_varga.tlog16rs.entities.WorkDay;
import com.akos_varga.tlog16rs.entities.WorkMonth;
import com.akos_varga.tlog16rs.core.beans.WorkMonthRB;
import com.akos_varga.tlog16rs.core.exceptions.*;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import javax.ws.rs.core.Response;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;

@Slf4j
@Path("/timelogger")
@Produces(MediaType.APPLICATION_JSON)
public class TLOG16RSResource {

    DBService dbService;
    JWTService jwtService;

    public TLOG16RSResource(TLOG16RSConfiguration config) {
        try {
            dbService = new DBService(config);
            jwtService = new JWTService();
        } catch (JoseException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
        }
    }

    @GET
    @Path("/workmonths")
    public Response getAllMonths(@HeaderParam("Authorization") String jwt) {
        try {
            String userName = jwtService.getUsernameIfValidJwt(jwt);
            return Response.ok(dbService.getAllMonths(userName)).build();
        } catch (InvalidJwtException | UserNotFoundException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        }
    }

    @POST
    @Path("/workmonths")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addNewMonth(WorkMonthRB month, @HeaderParam("Authorization") String jwt) {

        try {
            String userName = jwtService.getUsernameIfValidJwt(jwt);

            WorkMonth newWorkMonth = dbService.addMonth(userName, month.getYear(), month.getMonth());

            return Response.ok(newWorkMonth).build();
        } catch (NotNewMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        } catch (InvalidJwtException | UserNotFoundException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        }

    }

    @POST
    @Path("/workmonths/workdays")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addNewDay(WorkDayRB dayRB, @HeaderParam("Authorization") String jwt) {

        try {
            String userName = jwtService.getUsernameIfValidJwt(jwt);
            WorkDay workDay = dbService.addNewWeekDay(userName, dayRB.getRequiredMinPerDay(), dayRB.getYear(), dayRB.getMonth(), dayRB.getDay());
            if (workDay != null) {
                return Response.ok(workDay).build();
            } else {
                return Response.status(Response.Status.FORBIDDEN).build(); // ezt tesztelni...
            }

        } catch (NegativeMinutesOfWorkException | FutureWorkException | NotNewMonthException | NotNewDateException | NotTheSameMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        } catch (WeekendNotEnabledException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.FOUND).build(); //307?
        } catch (InvalidJwtException | UserNotFoundException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        }
    }

    @POST
    @Path("/workmonths/weekends")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addWeekendDay(WorkDayRB dayRB, @HeaderParam("Authorization") String jwt) {

        try {
            String userName = jwtService.getUsernameIfValidJwt(jwt);
            WorkDay workDay = dbService.addNewWeekendDay(userName, dayRB.getRequiredMinPerDay(), dayRB.getYear(), dayRB.getMonth(), dayRB.getDay());
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
        } catch (InvalidJwtException | UserNotFoundException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        }
    }

    @POST
    @Path("/workmonths/workdays/tasks/start")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addNewTask(StartTaskRB task, @HeaderParam("Authorization") String jwt) {
        try {
            String userName = jwtService.getUsernameIfValidJwt(jwt);
            Task newTask = dbService.addNewTask(userName, task.getYear(), task.getMonth(), task.getDay(), task.getTaskId(), task.getStartTime(), task.getComment());
            return Response.ok(newTask).build();

        } catch (NotNewMonthException | NotExpectedTimeOrderException | EmptyTimeFieldException | InvalidTaskIdException | NoTaskIdException | FutureWorkException | NotSeparatedTimesException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        } catch (InvalidJwtException | UserNotFoundException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        }
    }

    @PUT
    @Path("/workmonths/workdays/tasks/finish")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response finishTask(FinishTaskRB taskRB, @HeaderParam("Authorization") String jwt) {

        try {
            String userName = jwtService.getUsernameIfValidJwt(jwt);
            Task newTask = dbService.finishTask(userName, taskRB.getTaskId(), taskRB.getYear(), taskRB.getMonth(), taskRB.getDay(), taskRB.getStartTime(), taskRB.getEndTime());
            return Response.ok(newTask).build();

        } catch (NotExpectedTimeOrderException | EmptyTimeFieldException | InvalidTaskIdException | NoTaskIdException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException | NotNewMonthException | FutureWorkException | NotSeparatedTimesException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        } catch (InvalidJwtException | UserNotFoundException ex ) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        }
    }

    @PUT
    @Path("/workmonths/workdays/tasks/modify")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyOrCreateTask(ModifyTaskRB taskRB, @HeaderParam("Authorization") String jwt) {

        try {
            String userName = jwtService.getUsernameIfValidJwt(jwt);

            Task newTask = dbService.modifyOrCreateTask(userName, taskRB.getYear(), taskRB.getMonth(), taskRB.getDay(), taskRB.getTaskId(), taskRB.getStartTime(), taskRB.getNewTaskId(), taskRB.getNewStartTime(), taskRB.getNewEndTime(), taskRB.getNewComment());
            return Response.ok(newTask).build();

        } catch (NotExpectedTimeOrderException | EmptyTimeFieldException | InvalidTaskIdException | NoTaskIdException | FutureWorkException | NotNewMonthException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException | NotSeparatedTimesException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        } catch (InvalidJwtException | UserNotFoundException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        }
    }

    @PUT
    @Path("/workmonths/workdays/tasks/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteTask(DeleteTaskRB taskRB, @HeaderParam("Authorization") String jwt) {
        try {
            String userName = jwtService.getUsernameIfValidJwt(jwt);

            Task deletedTask = dbService.deleteTask(userName, taskRB.getYear(), taskRB.getMonth(), taskRB.getDay(), taskRB.getTaskId(), taskRB.getStartTime());
            if (deletedTask != null) {
                return Response.ok(deletedTask).build();
            }
            return Response.noContent().build();
        } catch (InvalidJwtException | UserNotFoundException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        }
    }

    @PUT
    @Path("/workmonths/deleteall")
    public Response deleteAll(@HeaderParam("Authorization") String jwt) {
        try {
            String userName = jwtService.getUsernameIfValidJwt(jwt);
            dbService.deleteAll(userName);

            //vagy toroljem a honapokat, akkor marad az id
            //return Response.ok().build();
            return Response.noContent().build();
        } catch (InvalidJwtException | UserNotFoundException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("/workmonths/{year}/{month}")
    public Response getDaysOfMonth(
            @PathParam(value = "year") int year,
            @PathParam(value = "month") int month,
            @HeaderParam("Authorization") String jwt) {

        try {
            String user = jwtService.getUsernameIfValidJwt(jwt);
            List<WorkDay> daysOfMonth = dbService.getDaysOfMonth(user, year, month);
            return Response.ok(daysOfMonth).build();

        } catch (NotNewMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        } catch (InvalidJwtException | UserNotFoundException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        }

    }

    @GET
    @Path("/workmonths/{year}/{month}/{day}")
    public Response getTasksOfDay(
            @PathParam(value = "year") int year,
            @PathParam(value = "month") int month,
            @PathParam(value = "day") int day,
            @HeaderParam("Authorization") String jwt) {
        try {
            String userName = jwtService.getUsernameIfValidJwt(jwt);
            List<Task> tasksOfDay = dbService.getTasksOfDay(userName, year, month, day);
            return Response.ok(tasksOfDay).build();

        } catch (FutureWorkException | NotNewMonthException | WeekendNotEnabledException | NotNewDateException | NotTheSameMonthException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        } catch (InvalidJwtException | UserNotFoundException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        }
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(UserRB user) {
        try {
            dbService.register(user.getName(), user.getPassword());

            return Response.ok(user).build(); // ne kuldje vissza  a usert
        } catch (UserExistsException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.CONFLICT).entity(ex.getMessage()).build();
        } catch (NoSuchAlgorithmException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }

    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(UserRB user) {
        try {
            dbService.login(user.getName(), user.getPassword());
            return Response.ok(user).header("authorization", "Bearer " + jwtService.createJWT(user.getName())) // ne kuldje vissza a usert
                    .header("Access-Control-Expose-Headers", "Authorization") // végleges kódot tesztelni nélküle
                    .build();
        } catch (UserNotFoundException | AuthenticationFailureException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        } catch (NoSuchAlgorithmException | JoseException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("/refresh")
    public Response refresh(@HeaderParam("Authorization") String jwt) {
        try {
            String userName = jwtService.getUsernameIfValidJwt(jwt);
            return Response.ok("{}").header("authorization", "Bearer " + jwtService.createJWT(userName))
                    .header("Access-Control-Expose-Headers", "Authorization") // végleges kódot tesztelni nélküle
                    .build();
        } catch (InvalidJwtException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (JoseException ex) {
            log.error(ex.getClass() + " " + ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

}
