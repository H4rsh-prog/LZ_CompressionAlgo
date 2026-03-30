package com.compression;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class CompressorServiceApplication {

	public static void main(String[] args) {
		ApplicationContext cx = SpringApplication.run(CompressorServiceApplication.class, args);

	}
}
