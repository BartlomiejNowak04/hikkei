# hikkei
**by team-06**

*A minimalistic yet feature-full wiki software*

## Features
- *tbd*

## Contributing
The project's development is in large part organized via the GitHub
[issue tracker](./../../issues) and via [pull requests](./../../pulls).

The project tries to follow a test-driven development approach.
As such every non-trivial change should be accompanied by the corresponding
tests and, unless for very good reasons, no class shall be left untested.

The code should be formatted according to the
[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).
Formatting can be done automatically by the provided configuration to the
[spotless](https://github.com/diffplug/spotless) Gradle plugin.

Commits to this repository shall be named in accordance to the
[Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)
convention, and follow the overall good commit guidelines which are described in
[this blog post](http://who-t.blogspot.com/2009/12/on-commit-messages.html).

## Testing
Running integration tests requires appropriate environment variables to be defined in the `.test-env` file located in the project root directory. The variables are as follows:
```bash
# Database integration testing
POSTGRES_HOST=[hostname]:[port]
POSTGRES_NAME=[testing_database_name]
POSTGRES_USER=[username]
POSTGRES_PASS=[password]
```

### Database testing
To run database integration tests one needs to configure PostgreSQL database named `[POSTGRES_TESTDB]` running at the address `[POSTGRES_HOST]`. The user `[POSTGRES_USER]` with password `[POSTGRES_PASS]` needs permissions to access and modify the database. This can be achieved like this:
```bash
sudo -u postgres psql postgres # Log into psql as superuser
CREATE USER [POSTGRES_USER] WITH PASSWORD [POSTGRES_PASS];
CREATE DATABASE [POSTGRES_NAME] OWNER [POSTGRES_USER];
```

## License
*tbd*
