package com.akos_varga.tlog16rs.resources;

import com.akos_varga.tlog16rs.TLOG16RSConfiguration;
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
