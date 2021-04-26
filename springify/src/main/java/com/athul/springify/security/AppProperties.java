package com.athul.springify.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AppProperties {

    @Autowired
    Environment env;

    public String getTokenSecret() {
        return env.getProperty("tokenSecret");
    }
    public String getAdminPassword() {
        return env.getProperty("adminPassword");
    }
}
