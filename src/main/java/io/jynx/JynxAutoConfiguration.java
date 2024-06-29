package io.jynx;

import io.jynx.migrator.JynxMigrator;
import io.jynx.migrator.service.ConfigurationProvider;
import io.jynx.migrator.service.MigrationService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class JynxAutoConfiguration {

	@Bean
	public ConfigurationProvider configurationProvider() {
		return new ConfigurationProvider();
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
