CREATE FUNCTION unique_username()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM users u
        JOIN LATERAL (
            SELECT username
            FROM user_name un
            WHERE un.user_id = u.id
            ORDER BY un.date DESC
            LIMIT 1
        ) cur ON true
        WHERE cur.username = NEW.username
    ) THEN
        RAISE EXCEPTION 'Username "%" already taken', NEW.username;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_unique_username
BEFORE INSERT ON user_name
FOR EACH ROW EXECUTE FUNCTION unique_username();


CREATE FUNCTION unique_title()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pages p
            JOIN LATERAL (
            SELECT title
            FROM page_title pt
            WHERE pt.page_id = p.id
            ORDER BY pt.date DESC
            LIMIT 1
            ) cur ON true
        WHERE cur.title = NEW.title
    ) THEN
        RAISE EXCEPTION 'Title "%" already taken', NEW.title;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_unique_title
BEFORE INSERT ON page_title
FOR EACH ROW EXECUTE FUNCTION unique_title();


CREATE FUNCTION deleted_has_date()
RETURNS TRIGGER AS $$
BEGIN
    IF (NEW.deleted=true AND NEW.date_deleted IS NULL) OR (NEW.deleted=false AND NEW.date_deleted IS NOT NULL) THEN
        RAISE EXCEPTION 'No date provided for deletion';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
DECLARE
    name text;
BEGIN
    FOREACH name in ARRAY $arr$
    { revisions,
    comments,
    pages,
    categories,
    roles,
    users
    }$arr$::text[]
    LOOP
        EXECUTE 'CREATE TRIGGER trg_deleted_has_date_' || name || ' BEFORE UPDATE OR INSERT ON ' || name || ' FOR EACH ROW EXECUTE FUNCTION deleted_has_date();';
    END LOOP;
END$$;


CREATE FUNCTION insert_user_with_data()
RETURNS TRIGGER AS $$
DECLARE
    new_user_id INTEGER;
BEGIN
    INSERT INTO users
    DEFAULT VALUES
    RETURNING id INTO new_user_id;

    INSERT INTO user_name (user_id, username)
    VALUES (new_user_id, NEW.username);

    return NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_insert_user_with_data
INSTEAD OF INSERT ON users_with_data
FOR EACH ROW
EXECUTE FUNCTION insert_user_with_data();


CREATE FUNCTION inserted_user_has_data()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM user_name WHERE user_id = NEW.id
    ) THEN
        RAISE EXCEPTION 'No username defined for user %', NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER trg_inserted_user_has_data
AFTER INSERT ON users
DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
EXECUTE FUNCTION inserted_user_has_data();


CREATE FUNCTION insert_role_with_data()
RETURNS TRIGGER AS $$
DECLARE
    new_role_id INTEGER;
BEGIN
    INSERT INTO roles
    DEFAULT VALUES
    RETURNING id INTO new_role_id;

    INSERT INTO role_name (role_id, name)
    VALUES (new_role_id, NEW.name);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_insert_role_with_data
INSTEAD OF INSERT ON roles_with_data
FOR EACH ROW
EXECUTE FUNCTION insert_role_with_data();


CREATE FUNCTION inserted_role_has_data()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM role_name WHERE role_id = NEW.id
    ) THEN
        RAISE EXCEPTION 'No name defined for role %', NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER trg_inserted_role_has_data
AFTER INSERT ON roles
DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
EXECUTE FUNCTION inserted_role_has_data();


CREATE FUNCTION insert_category_with_data()
RETURNS TRIGGER AS $$
DECLARE
    new_category_id INTEGER;
BEGIN
    INSERT INTO categories
    DEFAULT VALUES
    RETURNING id INTO new_category_id;

    INSERT INTO category_name (category_id, name)
    VALUES (new_category_id, NEW.name);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_insert_category_with_data
INSTEAD OF INSERT ON categories_with_data
FOR EACH ROW
EXECUTE FUNCTION insert_category_with_data();


CREATE FUNCTION inserted_category_has_data()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM category_name WHERE category_id = NEW.id
    ) THEN
        RAISE EXCEPTION 'No name defined for category %', NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER trg_inserted_category_has_data
AFTER INSERT ON categories
DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
EXECUTE FUNCTION inserted_category_has_data();


CREATE FUNCTION insert_page_with_data()
RETURNS TRIGGER AS $$
DECLARE
    new_page_id INTEGER;
BEGIN
    INSERT INTO pages (user_id)
    VALUES (NEW.user_id)
    RETURNING id INTO new_page_id;

    INSERT INTO page_title (page_id, title)
    VALUES (new_page_id, NEW.title);

    INSERT INTO page_category (page_id, category)
    VALUES (new_page_id, NEW.category);

    INSERT INTO page_archived (page_id, archived)
    VALUES (new_page_id, false);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_insert_page_with_data
INSTEAD OF INSERT ON pages_with_data
FOR EACH ROW
EXECUTE FUNCTION insert_page_with_data();


CREATE FUNCTION inserted_page_has_data()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM page_title WHERE page_id = NEW.id
    ) THEN
        RAISE EXCEPTION 'No title defined for page %', NEW.id;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM page_category WHERE page_id = NEW.id
    ) THEN
        RAISE EXCEPTION 'No category defined for page %', NEW.id;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM page_archived WHERE page_id = NEW.id
    ) THEN
        RAISE EXCEPTION 'No archived value defined for page %', NEW.id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER trg_inserted_page_has_data
AFTER INSERT ON pages
DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
EXECUTE FUNCTION inserted_page_has_data();


CREATE FUNCTION insert_revision_with_data()
RETURNS TRIGGER AS $$
DECLARE
    new_revision_id INTEGER;
BEGIN
    INSERT INTO revisions (page_id, user_id, file)
    VALUES (NEW.page_id, NEW.user_id, NEW.file)
    RETURNING id INTO new_revision_id;

    INSERT INTO revision_approved (revision_id, approved)
    VALUES (new_revision_id, NEW.approved);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_insert_revision_with_data
INSTEAD OF INSERT ON revisions_with_data
FOR EACH ROW
EXECUTE FUNCTION insert_revision_with_data();


CREATE FUNCTION inserted_revision_has_data()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM revision_approved WHERE revision_id = NEW.id
    ) THEN
        RAISE EXCEPTION 'No approval status defined for revision %', NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER trg_inserted_revision_has_data
AFTER INSERT ON revisions
DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
EXECUTE FUNCTION inserted_revision_has_data();
