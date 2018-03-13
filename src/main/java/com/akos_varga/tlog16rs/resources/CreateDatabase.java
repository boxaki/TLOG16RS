package com.akos_varga.tlog16rs.resources;

import com.akos_varga.tlog16rs.TLOG16RSConfiguration;
import com.akos_varga.tlog16rs.entities.Task;
import com.akos_varga.tlog16rs.entities.TimeLogger;
import com.akos_varga.tlog16rs.entities.WorkDay;
import com.akos_varga.tlog16rs.entities.WorkMonth;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.ws.rs.Path;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Akos Varga
 */
@Slf4j
@Path("/timelogger/save/test")
public class CreateDatabase {

    @Getter
    private final EbeanServer ebeanServer;
    private DataSourceConfig sourceConfig;
    private ServerConfig serverConfig;    

    public CreateDatabase(TLOG16RSConfiguration config) {
        updateSchema(config);
        initDataSourceConfig(config);
        initServerConfing(config);
        ebeanServer = EbeanServerFactory.create(serverConfig);
    }    

    private void updateSchema(TLOG16RSConfiguration config) {
        try {
            DriverManager.registerDriver(new org.mariadb.jdbc.Driver());

            Connection connection = DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
            DatabaseConnection databaseConnection = new JdbcConnection(connection);
            Liquibase liquibase = new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), databaseConnection);
            liquibase.update(new Contexts());
        } catch (SQLException | LiquibaseException ex) {
            log.error(ex.getMessage());
        }
    }

    private void initDataSourceConfig(TLOG16RSConfiguration config) {
        sourceConfig = new DataSourceConfig();
        sourceConfig.setDriver(config.getDriver());
        sourceConfig.setUrl(config.getUrl());
        sourceConfig.setUsername(config.getUsername());
        sourceConfig.setPassword(config.getPassword());
    }

    private void initServerConfing(TLOG16RSConfiguration config) {
        serverConfig = new ServerConfig();
        serverConfig.setName(config.getServer_name());
        serverConfig.setDdlGenerate(false);
        serverConfig.setDdlRun(false);
        serverConfig.setRegister(true);
        serverConfig.setDataSourceConfig(sourceConfig);
        serverConfig.addClass(Task.class);
        serverConfig.addClass(WorkDay.class);
        serverConfig.addClass(WorkMonth.class);
        serverConfig.addClass(TimeLogger.class);
        serverConfig.setDefaultServer(true);
    }

}
