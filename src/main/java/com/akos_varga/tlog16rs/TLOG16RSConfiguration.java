package com.akos_varga.tlog16rs;

import io.dropwizard.Configuration;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;


@Getter
@Setter
public class TLOG16RSConfiguration extends Configuration {
    @NotEmpty
    protected String driver;
    @NotEmpty
    protected String url;
    @NotEmpty
    protected String username;
    @NotEmpty
    protected String password;
    @NotEmpty
    protected String serverName;
}
