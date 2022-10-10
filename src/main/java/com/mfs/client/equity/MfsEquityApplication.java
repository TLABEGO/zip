package com.mfs.client.equity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = "com.mfs.client.equity")
public class MfsEquityApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
       try {
           SpringApplication.run(MfsEquityApplication.class, args);
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {

        return application.sources(MfsEquityApplication.class);

    }

}
