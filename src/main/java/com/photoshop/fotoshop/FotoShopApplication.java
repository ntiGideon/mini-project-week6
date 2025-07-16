package com.photoshop.fotoshop;

import com.photoshop.fotoshop.s3.AwsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(AwsProperties.class)
@SpringBootApplication
public class FotoShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(FotoShopApplication.class, args);
    }

}
