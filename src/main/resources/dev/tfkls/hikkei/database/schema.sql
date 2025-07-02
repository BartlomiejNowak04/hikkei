-- DROP TABLE IF EXISTS users, auth, roles, user_role, categories, category_role, pages, revisions, comments CASCADE;

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(125) UNIQUE NOT NULL,
    picture BYTEA,
    joined TIMESTAMP NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE auth (
    user_id INTEGER REFERENCES users(id),
    type VARCHAR(125),
    PRIMARY KEY (user_id, type),
    token TEXT NOT NULL,
    UNIQUE(type, token)
);

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(125) NOT NULL
);

CREATE TABLE user_role (
    user_id INTEGER REFERENCES users(id),
    role_id INTEGER REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(125) NOT NULL
);

-- NOTE: Maybe permission_name should be an enumerator instead
CREATE TABLE category_role (
    category_id INTEGER REFERENCES categories(id),
    role_id INTEGER REFERENCES roles(id),
    permission_name VARCHAR(125),
    permission_status BOOLEAN,
    PRIMARY KEY (category_id, role_id, permission_name)
);

CREATE TABLE pages (
    id SERIAL PRIMARY KEY,
    url TEXT UNIQUE NOT NULL,
    category INTEGER REFERENCES categories(id) NOT NULL
);

CREATE TABLE revisions (
    id SERIAL,
    page_id INTEGER REFERENCES pages(id),
    PRIMARY KEY (page_id, id),
    -- No need to verify UUID uniqueness, if we collide the universe ends anyway
    filename UUID NOT NULL DEFAULT gen_random_uuid(),
    date TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE comments (
    id SERIAL,
    page_id INTEGER REFERENCES pages(id),
    PRIMARY KEY (page_id, id),
    content TEXT NOT NULL,
    date TIMESTAMP NOT NULL DEFAULT NOW()
);
