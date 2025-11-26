package com.tomcvt.cvtcaptcha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CvtcaptchaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CvtcaptchaApplication.class, args);
	}
}
