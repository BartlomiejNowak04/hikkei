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
- [ ] assign permissions to users within a category
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

## Backend API
The frontend application communicates with the backend server through an API. Below is the description of available endpoints.
- `/api/user/`:
    - POST `login`: log a user in
    - POST `byid`: get user by id, params: Integer id
    - PUT `create`: create a user, params: String name, Picture pfp (optional)
    - PUT `update/name`: update username, params: Integer id, String name
    - PUT `update/pfp`: update profile picture, params: Integer id, Picture pfp
    - DELETE `delete`: delete a user, params: Integer id
- `/api/page/`:
    - POST `random`: get a random page
    - POST `bytitle`: get page with a given title, params: String title
    - POST `byurl`: get page with a given url, params: String url
    - POST `search`: search for pages with a phrase in title, params: String title
    - POST `category`: get pages within a category, params: Integer category
    - PUT `create`: create a page, params: String title, Integer category, Integer user
    - PUT `update/title`: update page title, params: Integer id, String title
    - PUT `update/category`: update page category, params: Integer id, Integer category
    - PUT `update/archive`: archive or de-archive page, params: Integer id, Boolean value
- `/api/revision/`:
    - POST `user`: get user's revisions, params: Integer user
    - POST `page`: get page's revisions, params: Integer page
    - POST `render`: render passed content, params: String content
    - POST `content`: get current content for a page, params: Integer id
    - POST `byid`: get revision by id, params: Integer id
    - POST `draft`: get user's draft revision for a page, params: Integer page, Integer user
    - PUT `create`: create a revision, params: Integer id (page's id), String content, Integer user
    - PUT `update/status`: approve or reject a revision, params: Integer id, Boolean status
    - PUT `draft/save`: save a draft revision, params: Integer page, Integer user, String content
    - DELETE `draft/delete`: delete a draft revision, params: Integer page, Integer user
- `/api/comment/`:
    - POST `user`: get user's comments, params: Integer user
    - POST `page`: get page's comments, params: Integer page
    - POST `byid`: get comment by id, params: Integer id
    - PUT `create`: create a comment, params: Integer id (page's id), String content, Integer user
    - DELETE `delete`: delete a comment, params: Integer id
- `/api/category/`:
    - POST `/`: get all categories
    - POST `byid`: get category by id, params: Integer id
    - POST `permission`: modify user's permissions to given category, params: Integer category, Integer user, Integer permission
    - PUT `create`: create a category, params: String name
    - PUT `update/name`: update category name, params: Integer id, String name


## Setup
The project's configuration is largely contained in the environment variables defined in the `.env` file located in the project root directory. The variables are as follows (their meaning is explained in appropriate sections):
```bash
POSTGRES_HOST=[hostname]:[port]
POSTGRES_NAME=[database_name]
POSTGRES_USER=[username]
POSTGRES_PASS=[password]
FILE_DIRECTORY=/directory/ending/with/a/slash/
PORT=[port_number]
```

### Database
The project uses PostgreSQL as it's main database. It requires a PostgreSQL database named `[POSTGRES_NAME]` running at the address `[POSTGRES_HOST]`. The user `[POSTGRES_USER]` with password `[POSTGRES_PASS]` needs permissions to access and modify the database. All of this can be achieved e.g. using the following commands:
```sql
sudo -u postgres psql postgres -- Log into psql as superuser
CREATE USER [POSTGRES_USER] WITH PASSWORD [POSTGRES_PASS];
CREATE DATABASE [POSTGRES_NAME] OWNER [POSTGRES_USER];
```
The database's schema can be created using the `create.sql` file located in `src/main/resources/dev/tfkls/hikkei/database`. The file `drop.sql` provides a clean-up functionality. It is customary to perform a clean-up before database initialisation. This can be achieved like this:
```sql
psql -h [hostname] -p [post] -d [database_name] -U [username]
\i drop.sql
\i create.sql
```

### Version Control
All of the version-control-related files are kept in the `[FILE_DIRECTORY]` directory, to which the user running the backend application needs read and write permissions.

### API Server
The project uses Vert.x to create an asynchronous HTTP API. It is run locally, on port `[PORT]`.

## Testing
Running integration tests requires appropriate environment variables to be defined in the `.test-env` file located in the project root directory. The variables are the same as in the `.env` file and have the same semantics. The integration tests will be skipped unless all necessary variables are properly defined.

## License
*tbd*
