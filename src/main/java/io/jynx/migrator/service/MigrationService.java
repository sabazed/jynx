package io.jynx.migrator.service;


import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import io.jynx.migrator.exception.MigrationException;
import io.jynx.migrator.service.model.Migration;
import io.jynx.migrator.util.DatabaseSubscriber;
import io.jynx.migrator.util.MigrationFields;
import io.jynx.migrator.util.MigrationHelper;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.mongodb.client.model.Sorts.ascending;
import static io.jynx.migrator.service.model.Migration.VERSION_NAME_DELIMITER;


@Service
public class MigrationService {

	public static final Logger logger = LoggerFactory.getLogger(MigrationService.class);
	private static final String MIGRATION_VALIDATION_ERROR = "Exception occurred while validating migrations";
	private static final String MIGRATION_MISMATCH_ERROR = "Invalid migrations present, database version mismatch with present migration files";
	private static final String MIGRATION_CHECKSUM_ERROR = "Failed to validate migration checksum: %s";
	private static final String VERSIONS_COLLECTION = "jynx_version_history";

	private static final String CLASSPATH_PREFIX = "classpath:";

	private final ConfigurationProvider config;
	private final MongoDatabase database;

	@Autowired
	public MigrationService(ConfigurationProvider config) {
		this.config = config;
		this.database = MongoClients.create(config.getUrl()).getDatabase(config.getDatabase());
	}

	public void startMigrations() throws Throwable {
		if (isDatabaseClean()) {
			initializeVersionTable();
		}
		var migrations = getMigrations()
				.sorted(Comparator.comparingInt(Migration::getVersion))
				.toList();
		processMigrations(migrations);
	}

	private Stream<Migration> getMigrations() throws IOException {
		var locationPath = config.getLocation();
		if (locationPath.startsWith(CLASSPATH_PREFIX)) {
			var resources = new PathMatchingResourcePatternResolver()
					.getResources(locationPath + (locationPath.endsWith("/") ? "*" : "/*"));
			return Arrays.stream(resources).map(MigrationHelper::getMigration);
		}
		var location = new File(locationPath);
		var files = Optional.ofNullable(location.listFiles()).orElse(new File[0]);
		return Arrays.stream(files).map(MigrationHelper::getMigration);
	}

	private boolean isDatabaseClean() {
		try {
			var subscriber = new DatabaseSubscriber<String>();
			database.listCollectionNames()
					.subscribe(subscriber);
			subscriber.await();
			return subscriber.getReceived().contains(VERSIONS_COLLECTION);
		} catch (Throwable e) {
			return false;
		}
	}

	private void initializeVersionTable() throws Throwable {
		var subscriber = new DatabaseSubscriber<Void>();
		database.createCollection(VERSIONS_COLLECTION).subscribe(subscriber);
		subscriber.await();
	}

	private void processMigrations(List<Migration> migrations) throws Throwable {
		var versionsCollection = database.getCollection(VERSIONS_COLLECTION);

		var versionsSubscriber = new DatabaseSubscriber<Document>();
		versionsCollection.find()
				.sort(ascending(MigrationFields.VERSION.getName()))
				.subscribe(versionsSubscriber);
		versionsSubscriber.await();

		var errors = versionsSubscriber.getErrors();
		if (!errors.isEmpty()) {
			errors.forEach(e -> logger.error(MIGRATION_VALIDATION_ERROR, e));
			var e = errors.get(0);
			throw new RuntimeException(e);
		}

		var received = versionsSubscriber.getReceived();
		logger.debug("Applied migrations: {}", received);
		if (received.size() > migrations.size()) {
			throw new MigrationException(MIGRATION_MISMATCH_ERROR);
		}

		for (int i = 0; i < received.size(); i++) {
			if (!MigrationHelper.matchMigrations(migrations.get(i), received.get(i))) {
				throw new RuntimeException(String.format(MIGRATION_CHECKSUM_ERROR, migrations.get(i).getName()));
			}
		}
		logger.info("Successfully validated {} migrations that were applied", received.size());

		var lastMigrationVersion = !received.isEmpty()
				? received.get(received.size() - 1).getInteger(MigrationFields.VERSION.getName())
				: -1;
		var newMigrations = migrations.stream()
				.filter(migration -> migration.getVersion().compareTo(lastMigrationVersion) > 0)
				.toList();
		DatabaseSubscriber<Document> migrationSubscriber;
		DatabaseSubscriber<InsertOneResult> insertSubscriber;
		for (var migration : newMigrations) {
			migrationSubscriber = new DatabaseSubscriber<>();
			database.runCommand(migration.getContent()).subscribe(migrationSubscriber);
			migrationSubscriber.await();

			insertSubscriber = new DatabaseSubscriber<>();
			versionsCollection.insertOne(migration.toVersionEntry()).subscribe(insertSubscriber);
			insertSubscriber.await();

			logger.info("Successfully applied migration: V{}{}{}", migration.getVersion(), VERSION_NAME_DELIMITER, migration.getName());
		}
		if (!newMigrations.isEmpty()) {
			logger.info("{} new migrations have been applied", newMigrations.size());
		}

		var latestVersion = newMigrations.isEmpty() ? lastMigrationVersion : newMigrations.get(newMigrations.size() - 1).getVersion();
		if (latestVersion > 0) {
			logger.info("Your database is up do date, version - V{}", latestVersion);
		}
	}

}
