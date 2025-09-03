DO $$
DECLARE
    name text;
BEGIN
    FOREACH name in ARRAY $arr$
        { user_name,
        user_picture,
        role_name,
        user_role,
        category_name,
        category_role,
        page_title,
        page_category,
        page_archived,
        revision_approved,
        revisions,
        comments,
        pages,
        categories,
        roles,
        users
        }$arr$::text[]
    LOOP
        EXECUTE 'CREATE RULE no_deletes_' || name || ' AS ON DELETE TO ' || name || ' DO INSTEAD NOTHING';
    END LOOP;
END$$;

DO $$
    DECLARE
        name text;
    BEGIN
        FOREACH name in ARRAY $arr$
        { user_name,
        user_picture,
        role_name,
        user_role,
        category_name,
        category_role,
        page_title,
        page_category,
        page_archived,
        revision_approved
        }$arr$::text[]
        LOOP
            EXECUTE 'CREATE RULE no_updates_' || name || ' AS ON UPDATE TO ' || name || ' DO INSTEAD NOTHING';
        END LOOP;
END$$;

CREATE RULE no_updates_users AS ON UPDATE TO users
WHERE NEW.id<>OLD.id OR NEW.joined<>OLD.joined
DO INSTEAD NOTHING;

CREATE RULE no_updates_roles AS
ON UPDATE TO roles
WHERE NEW.id <> OLD.id OR NEW.created <> OLD.created
DO INSTEAD NOTHING;

CREATE RULE no_updates_categories AS
ON UPDATE TO categories
WHERE NEW.id <> OLD.id OR NEW.created <> OLD.created
DO INSTEAD NOTHING;

CREATE RULE no_updates_pages AS
ON UPDATE TO pages
WHERE NEW.id <> OLD.id OR NEW.user_id <> OLD.user_id OR NEW.created <> OLD.created
DO INSTEAD NOTHING;

CREATE RULE no_updates_revisions AS
ON UPDATE TO revisions
WHERE NEW.id <> OLD.id OR NEW.page_id <> OLD.page_id OR NEW.user_id <> OLD.user_id OR NEW.file <> OLD.file OR NEW.date <> OLD.date
DO INSTEAD NOTHING;

CREATE RULE no_updates_comments AS
ON UPDATE TO comments
WHERE NEW.id <> OLD.id OR NEW.page_id <> OLD.page_id OR NEW.user_id <> OLD.user_id OR NEW.content <> OLD.content OR NEW.date <> OLD.date
DO INSTEAD NOTHING;

CREATE RULE no_updates_drafts AS
ON UPDATE TO drafts
WHERE NEW.page_id <> OLD.page_id OR NEW.user_id <> OLD.user_id
DO INSTEAD NOTHING;

DO $$
    DECLARE
        name text;
    BEGIN
        FOREACH name in ARRAY $arr$
        { users,
        roles,
        categories,
        pages,
        revisions,
        comments
        }$arr$::text[]
            LOOP
                EXECUTE 'CREATE RULE no_updates_deleted_' || name || ' AS ON UPDATE TO ' || name || ' WHERE OLD.deleted=TRUE DO INSTEAD NOTHING';
            END LOOP;
    END$$;
