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
    - GET ``: get the logged in user
    - GET `:id`: get user by id
    - GET `name/:name`: get user by username
    - GET `pfp/:id`: get user's profile picture
    - GET `categories/:id`: get categories that a user has a permission set for
    - POST `create`: create a user, params: String name
    - PUT `update/name`: update logged in user's username, params: String name
    - PUT `update/pfp`: update profile picture, send the picture as multi-part file
    - PUT `update/permission`: update user's global permission, Params: Integer id, String permission
    - DELETE `delete`: delete the account
- `/api/page/`:
    - GET `random`: get a random page
    - GET `title/:title`: get page with a given title
    - GET `:url`: get page with a given url
    - GET `search/:title`: search for pages with a phrase in title
    - GET `category/:id`: get pages within a category
    - POST `create`: create a page, params: String title, Integer category
    - PUT `update/title`: update page title, params: Integer id, String title
    - PUT `update/category`: update page category, params: Integer id, Integer category
    - PUT `update/archive`: archive or de-archive page, params: Integer id, Boolean value
    - DELETE `delete`: delete a page, params: Integer id
- `/api/revision/`:
    - GET `user/:id`: get user's revisions
    - GET `page/:id`: get page's revisions
    - GET `current/:id/raw`: get current content for a page as raw markup for editing
    - GET `current/:id/rendered`: get current content for a page as rendered HTML for display
    - GET `:id`: get revision metadata by id
    - GET `:id/raw`: get revision content as raw markup for editing
    - GET `:id/rendered`: get revision content as rendered HTML for display
    - GET `draft/:page_id`: get user's draft revision for a page
    - POST `create`: create a revision, params: Integer page_id, String content
    - PUT `update/status`: approve or reject a revision, params: Integer id, Boolean status
    - POST `draft/save`: save a draft revision, params: Integer page, String content
    - DELETE `draft/delete`: delete a draft revision, params: Integer page
    - DELETE `delete`: delete a revision, params: Integer id
    - POST `render`: render passed content, params: String content
- `/api/comment/`:
    - GET `user/:id`: get user's comments
    - GET `page/:id`: get page's comments
    - GET `:id`: get comment by id
    - POST `create`: create a comment, params: Integer page_id, String content
    - DELETE `delete`: delete a comment, params: Integer id
- `/api/category/`:
    - GET `all`: get all categories
    - GET `:id`: get category by id
    - GET `users/:id`: get users that have a set permission in a category
    - PUT `permission`: modify user's permissions to given category, params: Integer category, Integer user, String permission
    - POST `create`: create a category, params: String name
    - PUT `update/name`: update category name, params: Integer id, String name
    - PUT `update/permission`: update default category permission, params: Integer id, String permission
    - DELETE `delete`: delete a category, params: Integer id
- `/api/blob`:
    - GET `:id/data`: get the raw blob data in it's specified mime type
    - GET `:id/meta`: get the blob metadata (creation time, mime type, owner's user id & more)
    - POST `create`: upload some raw blob data, with the mime type determined by the Content-Type header
    - GET `user/:id`: get all non-deleted blobs created by a given user
    - DELETE `delete`: delete a blob object and it's data, params: Integer id
- `/api/auth`:
    - GET `methods`: get available auth methods
    - GET `login/:method`: login a user with the specified method
    - GET `register/:method`: register a user with the specified method
    - GET `merge/:method`: add a new auth method for a user
    - GET `callback/:method`: route internally managed by each auth provider
    - GET `logout`: logout of the account
    - GET `logout/global`: logout of all the account's sessions
    - *NOTES*
      - As the login/register/merge endpoints begin an auth flow, the client should be redirected to them directly*
      - The auth client redirects to `/auth/success` on success and `/auth/failure` with the query parameters `error`, `debug` and `status`
        on failure, with the parameters containing respectively the error message, debug error message & the assigned HTTP response status
        (i.e. whether the error is client or server-side). These paths are not prefixed with `/api/` and are thus not handled.
        This is done so that the frontend can render the returned information to the user accordingly.


## Setup
The project's configuration is largely contained in the environment variables defined in the `.env` file located in the project root directory. The variables are as follows (their meaning is explained in appropriate sections):
```bash
POSTGRES_HOST=[hostname]:[port]
POSTGRES_NAME=[database_name]
POSTGRES_USER=[username]
POSTGRES_PASS=[password]
FILE_DIRECTORY=/directory/where/revisions/are/kept
IMAGE_DIRECTORY=/directory/where/images/are/kept
PORT=[port_number]
BASE_URI=[base_site_uri]
FRONTEND_URI=[frontend_hostname]

# For site administration
ADMIN_PASS=[default_superadmin_access_token]

# For GitHub login support
AUTH_GITHUB_CLIENT=[oauth2_client_id]
AUTH_GITHUB_SECRET=[oauth2_secret_token]

# For Google login support
AUTH_GOOGLE_CLIENT=[oauth2_client_id]
AUTH_GOOGLE_SECRET=[oauth2_secret_token]

# If frontend is not mounted on root.
# The backend redirects the user to [path]/success
#  or [path]/failure after the auth workflow.
AUTH_FRONTEND_REDIRECT=[path]
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
psql -h [hostname] -p [port] -d [database_name] -U [username]
\i drop.sql
\i create.sql
```

### Version Control
All of the version-control-related files are kept in the `[FILE_DIRECTORY]` directory, to which the user running the backend application needs read and write permissions.

### Static files
All of the static files are kept in the `[IMAGE_DIRECTORY]` directory.

### API Server
The project uses Vert.x to create an asynchronous HTTP API. It is run locally, on port `[PORT]`.

## Start-up
1. In the backend directory (or in the project root if the backend is there):
```bash
./gradlew run
```
By default, the backend usually listens on `http://localhost:8080`
(if a different port is configured, use that one).
2. Go to the frontend directory. Create file `.env.local`:
```bash
NEXT_PUBLIC_API_BASE=[http-method]://[hostname]
```
3. Install packages and run the frontend:
```bash
npm install
npm run dev
```
The frontend usually runs on `http://localhost:3000`.

## Testing
Running integration tests requires appropriate environment variables to be defined in the `.test-env` file located in the project root directory. The variables are the same as in the `.env` file and have the same semantics. The integration tests will be skipped unless all necessary variables are properly defined.

## License
*tbd*
