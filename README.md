# FluDa SQL Init

Automated database schema management and data population on application startup for Jakarta EE / CDI applications.

## Modules

- `fluda-sql-init-core`: plain SQL script parser and runner (no CDI, no MicroProfile Config).
- `fluda-sql-init-config`: optional MicroProfile Config integration for `SqlInitConfig`.
- `fluda-sql-init-cdi`: portable CDI extension that applies SQL scripts when the application starts.

## Building

```bash
./mvnw clean install
```

## Documentation

See the [reference documentation site](https://fludakit.github.io/) for installation, quickstart, and full API reference.

## Contributing

Contributions are welcome — issues, pull requests, and feature suggestions are all encouraged. See [CONTRIBUTING.md](CONTRIBUTING.md) for how to build the project and submit changes.
