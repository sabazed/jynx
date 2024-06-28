package io.jynx.migrator.util;

public enum MigrationFields {

	VERSION("version"),
	NAME("name"),
	CHECKSUM("checksum");

	private final String name;

	MigrationFields(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
