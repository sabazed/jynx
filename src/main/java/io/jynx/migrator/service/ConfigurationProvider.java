package io.jynx.migrator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationProvider {

	public static final String PROPERTIES_PREFIX = "jynx.migrator.mongodb";
	private static final String URL_PROPERTY = PROPERTIES_PREFIX + ".url";
	private static final String DATABASE_PROPERTY = PROPERTIES_PREFIX + ".database";
	private static final String LOCATION_PROPERTY = PROPERTIES_PREFIX + ".location";

	private final String url;
	private final String database;
	private final String location;

	@Autowired
	public ConfigurationProvider(Environment environment) {
		url = environment.getRequiredProperty(URL_PROPERTY);
		database = environment.getRequiredProperty(DATABASE_PROPERTY);
		location = environment.getRequiredProperty(LOCATION_PROPERTY);
	}

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
