package io.jynx.migrator.util;

import io.jynx.migrator.service.model.Migration;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MigrationHelper {

	public static Migration getMigration(File file) {
		Migration migration;
		try {
			String json = Files.readString(file.toPath());
			migration = new Migration(file.getName(), file.toPath(), json);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return migration;
	}

	public static boolean matchMigrations(Migration migration, Document dbEntry) {
		return migration.getVersion().equals(dbEntry.getInteger("version"))
				&& migration.getName().equals(dbEntry.getString("name"))
				&& migration.getChecksum().equals(dbEntry.getString("checksum"));
	}

}
