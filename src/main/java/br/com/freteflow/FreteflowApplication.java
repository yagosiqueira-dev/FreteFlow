package br.com.freteflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FreteflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(FreteflowApplication.class, args);
	}

}
