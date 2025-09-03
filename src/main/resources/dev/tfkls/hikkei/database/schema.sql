CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    joined TIMESTAMP NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT false,
    date_deleted TIMESTAMP
);

CREATE TABLE user_name (
    user_id INTEGER REFERENCES users(id),
    date TIMESTAMP NOT NULL DEFAULT now(),
    username VARCHAR(125) NOT NULL,
    PRIMARY KEY (user_id, date)
);

CREATE TABLE user_picture (
    user_id INTEGER REFERENCES users(id),
    date TIMESTAMP NOT NULL DEFAULT now(),
    -- binary data from file provider
    picture BYTEA NOT NULL,
    PRIMARY KEY (user_id, date)
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
    created TIMESTAMP NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    date_deleted TIMESTAMP
);

CREATE TABLE role_name (
    role_id INTEGER REFERENCES roles(id),
    date TIMESTAMP NOT NULL DEFAULT now(),
    name VARCHAR(125) NOT NULL,
    PRIMARY KEY (role_id, date)
);

CREATE TABLE user_role (
    user_id INTEGER REFERENCES users(id),
    role_id INTEGER REFERENCES roles(id),
    date TIMESTAMP NOT NULL DEFAULT now(),
    set BOOLEAN NOT NULL,
    PRIMARY KEY (user_id, role_id, date)
);

CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    created TIMESTAMP NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    date_deleted TIMESTAMP
);

CREATE TABLE category_name (
    category_id INTEGER REFERENCES categories(id),
    date TIMESTAMP NOT NULL DEFAULT now(),
    name VARCHAR(125) NOT NULL,
    PRIMARY KEY (category_id, date)
);

-- NOTE: Maybe permission_name should be an enumerator instead
CREATE TABLE category_role (
    category_id INTEGER REFERENCES categories(id),
    role_id INTEGER REFERENCES roles(id),
    permission_name VARCHAR(125) NOT NULL,
    permission_status BOOLEAN,
    date TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (category_id, role_id, permission_name, date)
);

CREATE TABLE pages (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    created TIMESTAMP NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    date_deleted TIMESTAMP
);

CREATE TABLE page_title (
    page_id INTEGER REFERENCES pages(id),
    date TIMESTAMP NOT NULL DEFAULT now(),
    -- The title will serve as a URL (like on Wikipedia)
    title TEXT NOT NULL,
    PRIMARY KEY (page_id, date)
);

CREATE TABLE page_category (
    page_id INTEGER REFERENCES pages(id),
    date TIMESTAMP NOT NULL DEFAULT now(),
    category INTEGER REFERENCES categories(id) NOT NULL,
    PRIMARY KEY (page_id, date)
);

CREATE TABLE page_archived (
   page_id INTEGER REFERENCES pages(id),
   date TIMESTAMP NOT NULL DEFAULT now(),
   archived BOOLEAN NOT NULL,
   PRIMARY KEY (page_id, date)
);

CREATE TABLE revisions (
    id SERIAL PRIMARY KEY ,
    page_id INTEGER REFERENCES pages(id),
    user_id INTEGER REFERENCES users(id),
    -- binary data from file provider
    file BYTEA NOT NULL,
    date TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    date_deleted TIMESTAMP
);

CREATE TABLE revision_approved (
    revision_id INTEGER REFERENCES revisions(id),
    date TIMESTAMP NOT NULL DEFAULT now(),
    approved BOOLEAN NOT NULL,
    PRIMARY KEY (revision_id, date)
);

CREATE TABLE comments (
    id SERIAL PRIMARY KEY ,
    page_id INTEGER REFERENCES pages(id),
    user_id INTEGER REFERENCES users(id),
    content TEXT NOT NULL,
    date TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    date_deleted TIMESTAMP
);

CREATE TABLE drafts (
    user_id INTEGER REFERENCES users(id),
    page_id INTEGER REFERENCES pages(id),
    -- binary data from file provider
    file BYTEA NOT NULL,
    PRIMARY KEY (user_id, page_id)
);
