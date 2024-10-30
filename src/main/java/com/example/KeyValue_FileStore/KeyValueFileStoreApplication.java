package com.example.KeyValue_FileStore;


import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@SpringBootApplication
public class KeyValueFileStoreApplication {
	@Autowired
	private DataStoreController controller;

	public static void main(String[] args) {
		SpringApplication.run(KeyValueFileStoreApplication.class, args);
	}

}



