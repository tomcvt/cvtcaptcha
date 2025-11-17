package com.tomcvt.cvtcaptcha;

import org.springframework.boot.SpringApplication;

public class TestCvtcaptchaApplication {

	public static void main(String[] args) {
		SpringApplication.from(CvtcaptchaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
