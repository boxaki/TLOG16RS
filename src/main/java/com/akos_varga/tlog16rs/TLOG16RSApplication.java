package com.akos_varga.tlog16rs;

import com.akos_varga.tlog16rs.resources.CreateDatabase;
import com.akos_varga.tlog16rs.resources.TLOG16RSResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.io.FileInputStream;
import java.util.Properties;

public class TLOG16RSApplication extends Application<TLOG16RSConfiguration> {

    public static void main(final String[] args) throws Exception {        
        FileInputStream propFile = new FileInputStream("database.properties");
        Properties p = new Properties(System.getProperties());
        p.load(propFile);
        System.setProperties(p);
        
        new TLOG16RSApplication().run(args);
    }

    @Override
    public String getName() {
        return "TLOG16RS";
    }

    @Override
    public void initialize(final Bootstrap<TLOG16RSConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final TLOG16RSConfiguration configuration,
                    final Environment environment) {   
        environment.jersey().register(new TLOG16RSResource());
        environment.jersey().register(new CreateDatabase(configuration));
        
    }

}
