package me.krstic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

@SuppressWarnings("deprecation")
@SpringBootApplication
public class AlfrescoApplication extends SpringBootServletInitializer {
	
//	Deployable war file
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder alfrescoApplication) {
        return alfrescoApplication.sources(AlfrescoApplication.class);
    }

	public static void main(String[] args) {
		SpringApplication.run(AlfrescoApplication.class, args);
	}
}
