
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(30) NOT NULL,
    joined DATE NOT NULL,
);

CREATE TABLE auth (
    user_id INTEGER REFERENCES users(id),
    type VARCHAR(10) NOT NULL,
    PRIMARY KEY (user_id, type),
    token TEXT NOT NULL,
);

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
);

CREATE TABLE user_role (
    user_id INTEGER REFERENCES users(id),
    role_id INTEGER REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id),
);

CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
);

CREATE TABLE category_role (
    category_id INTEGER REFERENCES categories(id),
    role_id INTEGER REFERENCES roles(id),
    PRIMARY KEY (category_id, role_id),
);

CREATE TABLE pages (
    id SERIAL PRIMARY KEY,
    url TEXT UNIQUE NOT NULL,
    category INTEGER REFERENCES categories(id) NOT NULL,
);

CREATE TABLE revisions (
    id SERIAL,
    page_id INTEGER REFERENCES pages(id),
    PRIMARY KEY (page_id, id),
    filename UUID DEFAULT gen_random_uuid(),
    date TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
);

CREATE TABLE comments (
    id SERIAL,
    page_id INTEGER REFERENCES pages(id),
    PRIMARY KEY (page_id, id),
    filename UUID DEFAULT gen_random_uuid(),
    date TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
);
