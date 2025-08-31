# hikkei
**by team-06**

*A minimalistic yet feature-full wiki software*

## Features
#### Users
- [ ] create user accounts authenticated with Github
- [ ] display user's comments, pages and other data on their profile page
- [ ] change your username or profile picture
- [ ] delete user accounts
#### Pages
- [ ] display page's content and category
- [ ] comment on a page (or delete a comment) and display the comments
- [ ] change your page's category and title
- [ ] archive and de-archive your page
#### Editing
- [ ] list all revisions and display them (with plain-text or preview)
- [ ] accept or reject a revision
- [ ] write a revision draft and save it to edit later
- [ ] submit a revision
- [ ] preview your revision
#### Categories
- [ ] list all categories (with search)
- [ ] create a category
- [ ] change category's name
- [ ] list pages in a category (with search)
- [ ] create a page within a category
- [ ] assign permissions to roles within a category
#### Roles
- [ ] display all roles (with search)
- [ ] create and delete roles
- [ ] change role's name
- [ ] grant and revoke permissions by modifying user's roles
#### Main Page
- [ ] search for page by its title
- [ ] display a random article
- [ ] play the [Wiki Game](https://en.wikipedia.org/wiki/Wikipedia:Wiki_Game)

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

## Setup
The project's configuration is largely contained in the environment variables defined in the `.env` file located in the project root directory. The variables are as follows (their meaning is explained in appropriate sections):
```bash
POSTGRES_HOST=[hostname]:[port]
POSTGRES_NAME=[database_name]
POSTGRES_USER=[username]
POSTGRES_PASS=[password]
FILE_DIRECTORY=/directory/ending/with/a/slash/
```

### Database
The project uses PostgreSQL as it's main database. It requires a PostgreSQL database named `[POSTGRES_NAME]` running at the address `[POSTGRES_HOST]`. The user `[POSTGRES_USER]` with password `[POSTGRES_PASS]` needs permissions to access and modify the database. All of this can be achieved e.g. using the following commands:
```sql
sudo -u postgres psql postgres -- Log into psql as superuser
CREATE USER [POSTGRES_USER] WITH PASSWORD [POSTGRES_PASS];
CREATE DATABASE [POSTGRES_NAME] OWNER [POSTGRES_USER];
```

### Version Control
All of the version-control-related files are kept in the `[FILE_DIRECTORY]` directory, to which the user running the backend application needs read and write permissions.

## Testing
Running integration tests requires appropriate environment variables to be defined in the `.test-env` file located in the project root directory. The variables are the same as in the `.env` file and have the same semantics. The integration tests will be skipped unless all necessary variables are properly defined.

## License
*tbd*
