package com.akos_varga.tlog16rs.resources;

import com.akos_varga.tlog16rs.entities.TestEntity;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Akos Varga
 */
@Path("/timelogger/save/test")
public class CreateDatabase {
    
    DataSourceConfig sourceConfig;
    ServerConfig serverConfig;
    EbeanServer ebeanServer;
    
    public CreateDatabase(){
        initDataSourceConfig();
        initServerConfing();
        ebeanServer = EbeanServerFactory.create(serverConfig);        
    } 
    
    private void initDataSourceConfig(){
        sourceConfig = new DataSourceConfig();
        sourceConfig.setDriver("org.mariadb.jdbc.Driver");
        sourceConfig.setUrl("jdbc:mariadb://127.0.0.1:9001/timelogger");
        sourceConfig.setUsername("timelogger");
        sourceConfig.setPassword("633Ym2aZ5b9Wtzh4EJc4pANx");
    }
    
    private void initServerConfing(){
        serverConfig = new ServerConfig();
        serverConfig.setName("timelogger");
        serverConfig.setDdlGenerate(true);
        serverConfig.setDdlRun(true);
        serverConfig.setRegister(true);
        serverConfig.setDataSourceConfig(sourceConfig);
        serverConfig.addClass(TestEntity.class);
        serverConfig.setDefaultServer(true);        
    }
    
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String saveEntity(String testData){
        TestEntity entity = new TestEntity();
        entity.setText(testData);        
        Ebean.save(entity);
        return testData;
    }
    
}
