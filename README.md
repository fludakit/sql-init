# FluDa SQL Init

[![Build](https://github.com/fludakit/sql-init/actions/workflows/build.yml/badge.svg)](https://github.com/fludakit/sql-init/actions/workflows/build.yml)

Automated database schema management and data population on application startup for Jakarta EE / CDI applications.

## Modules

- `fluda-sql-init-core`: plain SQL script parser and runner (no CDI, no MicroProfile Config).
- `fluda-sql-init-config`: optional MicroProfile Config integration for `SqlInitConfig`.
- `fluda-sql-init-cdi`: portable CDI extension that applies SQL scripts when the application starts.

## Usage

Place versioned scripts in `src/main/resources/db/migration/` (`V1__create_engineers.sql`, `V2__seed_engineers.sql`, …) and run them programmatically:

```java
import io.github.fludakit.sqlinit.DbMigrator;
import javax.sql.DataSource;

DataSource dataSource = ...; // obtain a DataSource
new DbMigrator(dataSource).migrate();
```

In a CDI environment, add `fluda-sql-init-cdi` and migrations run automatically on application startup — no code required.

For script naming, configuration properties, custom version strategies, and resource resolvers, see the [SQL Init documentation](https://fludakit.github.io/documentation/sql-init/getting-started/).

## Building

```bash
./mvnw clean install
```

## Documentation

See the [reference documentation site](https://fludakit.github.io/documentation/sql-init/getting-started/) for getting started, advanced topics, and full API reference.

## Contributing

Contributions are welcome — issues, pull requests, and feature suggestions are all encouraged. See [CONTRIBUTING.md](CONTRIBUTING.md) for how to build the project and submit changes.
