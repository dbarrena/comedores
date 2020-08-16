package com.dbxprts.comedores;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ComedoresApplication {

	public static void main(String[] args) {
		TwoWaySerialComm twoWaySerialComm = new TwoWaySerialComm();
		System.out.println("********* PUERTOOOOOOS :");
		twoWaySerialComm.listPorts();
		twoWaySerialComm.connect("COM7");
		SpringApplication.run(ComedoresApplication.class, args);
	}

}
