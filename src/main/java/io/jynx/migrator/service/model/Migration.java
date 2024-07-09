package io.jynx.migrator.service.model;


import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import io.jynx.migrator.exception.MigrationException;
import org.bson.Document;

import java.io.IOException;
import java.util.regex.Pattern;

import static io.jynx.migrator.service.MigrationService.logger;


public class Migration {

	public static final String VERSION_NAME_DELIMITER = "__";
	private static final Pattern NAME_PATTERN = Pattern.compile("^V[0-9.]{1,6}__[a-zA-Z][a-zA-Z0-9_]{0,29}.json$");
	private static final String MIGRATION_INVALID_VERSION = "Migration found with an invalid version {}";
	private static final String MIGRATION_INVALID_NAME = "Migration has invalid name: {}";

	private final String name;
	private final Integer version;
	private final Document content;
	private final String checksum;

	public Migration(String fileName, String migration) {
		// Validate name and content
		validateFileName(fileName);
		var delimiterIndex = fileName.indexOf(VERSION_NAME_DELIMITER);
		var version = fileName.substring(1, delimiterIndex);
		try {
			this.version = Integer.parseInt(version);
		} catch (NumberFormatException e) {
			logger.error(MIGRATION_INVALID_VERSION, version);
			throw new MigrationException(e);
		}
		this.name = fileName.substring(delimiterIndex + 2);
		this.content = Document.parse(migration);
		this.checksum = calculateHash(migration);
	}

	public String getName() {
		return name;
	}

	public Integer getVersion() {
		return version;
	}

	public Document getContent() {
		return content;
	}

	public String getChecksum() {
		return checksum;
	}

	private void validateFileName(String fileName) {
		if (!NAME_PATTERN.matcher(fileName).matches()) {
			logger.error(MIGRATION_INVALID_NAME, fileName);
			throw new MigrationException(fileName);
		}
	}

	private String calculateHash(String content) {
		try {
			var byteSource = ByteSource.wrap(content.getBytes());
			return byteSource.hash(Hashing.sha256()).toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Document toVersionEntry() {
		var document = new Document();
		document.put("version", version);
		document.put("name", name);
		document.put("checksum", checksum);
		return document;
	}

}
