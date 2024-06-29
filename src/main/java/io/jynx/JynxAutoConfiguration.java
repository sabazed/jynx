package io.jynx;

import io.jynx.migrator.JynxMigrator;
import io.jynx.migrator.service.ConfigurationProvider;
import io.jynx.migrator.service.MigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
public class JynxAutoConfiguration {

	private final Environment environment;

	@Autowired
	public JynxAutoConfiguration(Environment environment) {
		this.environment = environment;
	}

	@Bean
	public ConfigurationProvider configurationProvider() {
		return new ConfigurationProvider(environment);
	}

	@Bean
	public MigrationService migrationService() {
		return new MigrationService(configurationProvider());
	}

	@Bean
	public JynxMigrator jynxMigrator() {
		return new JynxMigrator(migrationService());
	}

}
