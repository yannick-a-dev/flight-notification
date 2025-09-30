package com.project_flight_notification;

import com.flight.project_flight.NotificationApplication;
import org.springframework.boot.SpringApplication;

public class TestNotificationApplication {

	public static void main(String[] args) {
		SpringApplication.from(NotificationApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
