package io.jynx.migrator.exception;

public class MigrationException extends RuntimeException {

	public MigrationException(Throwable cause) {
		super(cause);
	}

	public MigrationException(String message) {
		super(message);
	}

}
