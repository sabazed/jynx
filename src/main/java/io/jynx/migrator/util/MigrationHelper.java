package io.jynx.migrator.util;

import io.jynx.migrator.service.model.Migration;
import org.bson.Document;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class MigrationHelper {

	public static Migration getMigration(Resource resource) {
		try {
			var name = resource.getFilename();
			var content = new BufferedReader(new InputStreamReader(resource.getInputStream()))
							.lines().collect(Collectors.joining(System.lineSeparator()));
			return new Migration(name, content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Migration getMigration(File file) {
		try {
			var name = file.getName();
			var content = Files.readString(file.toPath());
			return new Migration(name, content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean matchMigrations(Migration migration, Document dbEntry) {
		return migration.getVersion().equals(dbEntry.getInteger("version"))
				&& migration.getName().equals(dbEntry.getString("name"))
				&& migration.getChecksum().equals(dbEntry.getString("checksum"));
	}

}
