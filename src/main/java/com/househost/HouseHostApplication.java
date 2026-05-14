package com.househost;

import com.househost.config.DatabaseStartupProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HouseHostApplication {

    public static void main(String[] args) {
        DatabaseStartupProperties.configure();
        SpringApplication.run(HouseHostApplication.class, args);
    }
}
