package com.wk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@Configuration
@SpringBootApplication
public class MainApplication {

	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}

//	@Bean
//	public MultipartConfigElement multipartConfigElement() {
//		MultipartConfigFactory multipartConfigFactory = new MultipartConfigFactory();
//		multipartConfigFactory.setMaxFileSize("20MB");
//		multipartConfigFactory.setMaxRequestSize("100MB");
//		return multipartConfigFactory.createMultipartConfig();
//	}
}
