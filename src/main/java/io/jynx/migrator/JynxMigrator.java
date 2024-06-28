package io.jynx.migrator;

import io.jynx.migrator.service.MigrationService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JynxMigrator {

	public static final Logger logger = LoggerFactory.getLogger(JynxMigrator.class);

	private final MigrationService migrationService;

	@Autowired
	public JynxMigrator(MigrationService migrationService) {
		this.migrationService = migrationService;
	}

	@PostConstruct
	public void migrate() {
		try {
			migrationService.startMigrations();
		} catch (Throwable e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

}
