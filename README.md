# Jynx Migrator

Jynx's Migrator is a dependency created for Spring Boot Data MongoDB Reactive compatible project, designed to handle MongoDB database migrations using JSON files. Leveraging MongoDB's reactive capabilities, it ensures efficient operations for seamless database updates.

## How It Works

Each migration script is a JSON file containing parameters for MongoDB `db.runCommand()` commands, following MongoDB's syntax. These files are automatically detected and executed in sequence, allowing for smooth and incremental database changes.

## Migration File Naming Convention

Migration files should be named in the format: `Vx__migration_name.json`

### Example:

- **Name:** `V01__initialize_first_collection.json`
- **Content:**
```json
{
  "create": "someCollectionName",
  "capped": false
}
```

## Configuration

Add the following properties to your `application.properties` file to configure the application:

``` properties
jynx.migrator.mongodb.url=mongodb://user:pass@localhost:port/db
jynx.migrator.mongodb.database=db
jynx.migrator.mongodb.location=classpath:/db/migration/mongo
```

## Usage

1. **Add Migration Files:** Place your migration JSON files in the specified directory (`src/main/resources/db/migration/mongo`).
2. **Run Application:** Start your Spring Boot application. The migrator will automatically execute the migrations in the correct order using the parameters defined in the JSON files with `db.runCommand()`.


## Dependency

To include Jynx Migrator in your project, add the following repository and dependency to your `build.gradle` file:
```groovy
repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation 'com.github.sabazed:jynx:{version}'
}
```
Use the release you wish to install instead of `version` 

## Important Notes

- Each migration should contain valid parameters for a MongoDB `db.runCommand()` command.
- Ensure that migration files are correctly formatted and named to avoid execution issues.

## Disclaimer

This product has not been thoroughly tested. Use it at your own risk. Always back up your data before performing migrations. 
**Use at your own risk**.