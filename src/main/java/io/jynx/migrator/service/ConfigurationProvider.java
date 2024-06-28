package io.jynx.migrator.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationProvider {

	@Value("${jynx.migrator.mongodb.url}")
	private String url;

	@Value("${jynx.migrator.mongodb.database}")
	private String database;

	@Value("${jynx.migrator.mongodb.location}")
	private String location;

	public String getUrl() {
		return url;
	}

	public String getDatabase() {
		return database;
	}

	public String getLocation() {
		return location;
	}

}
