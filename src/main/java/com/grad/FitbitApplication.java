package com.grad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.grad.config.AuthorizationProperties;
import com.grad.config.MailInfoProperties;
import com.grad.config.MongoProperties;

/**
 * @author nikos_mas
 */

@SpringBootApplication
@EnableConfigurationProperties({ MongoProperties.class, AuthorizationProperties.class, MailInfoProperties.class })
public class FitbitApplication {

	public static void main(String[] args) {
		SpringApplication.run(FitbitApplication.class, args);
	}
}
