package com.akos_varga.tlog16rs.resources;

import com.akos_varga.tlog16rs.entities.TestEntity;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Akos Varga
 */
@Slf4j
@Path("/timelogger/save/test")
public class CreateDatabase {

    private DataSourceConfig sourceConfig;
    private ServerConfig serverConfig;
    private EbeanServer ebeanServer;
    private static final String SERVER_CONFIG_NAME = System.getProperty("servername");
    private static final String DRIVER = System.getProperty("driver");
    private static final String URL = System.getProperty("url");
    private static final String USERNAME = System.getProperty("username"); 
    private static final String PASSWORD = System.getProperty("password"); 

    public CreateDatabase() {        
        updateSchema();
        initDataSourceConfig();
        initServerConfing();
        ebeanServer = EbeanServerFactory.create(serverConfig);
    }

    private void updateSchema() {
        try {
            DriverManager.registerDriver(new org.mariadb.jdbc.Driver());

            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            DatabaseConnection databaseConnection = new JdbcConnection(connection);
            Liquibase liquibase = new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), databaseConnection);
            liquibase.update(new Contexts());
        } catch (SQLException | LiquibaseException ex) {
            log.error(ex.getMessage());
        }

    }

    private void initDataSourceConfig() {
        sourceConfig = new DataSourceConfig();
        sourceConfig.setDriver(DRIVER); 
        sourceConfig.setUrl(URL); 
        sourceConfig.setUsername(USERNAME); 
        sourceConfig.setPassword(PASSWORD); 
    }

    private void initServerConfing() {
        serverConfig = new ServerConfig();
        serverConfig.setName(SERVER_CONFIG_NAME);
        serverConfig.setDdlGenerate(false);
        serverConfig.setDdlRun(false);
        serverConfig.setRegister(true);
        serverConfig.setDataSourceConfig(sourceConfig);
        serverConfig.addClass(TestEntity.class);
        serverConfig.setDefaultServer(true);
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String saveEntity(String testData) {
        TestEntity entity = new TestEntity();
        entity.setText(testData);
        Ebean.save(entity);
        return testData;
    }

}
