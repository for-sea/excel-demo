package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDBConfig {
    @Value("${spring.influx.user}")
    public String username;

    @Value("${spring.influx.password}")
    public String password;

    @Value("${spring.influx.url}")
    public String url;

    @Value("${spring.influx.database}")
    public String database;
}
