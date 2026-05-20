package com.example.libr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LibrApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibrApplication.class, args);
	}

}
