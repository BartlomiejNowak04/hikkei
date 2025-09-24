DO $$
DECLARE
    name text;
BEGIN
    FOREACH name in ARRAY $arr$
        { user_name,
        user_picture,
        category_name,
        page_title,
        page_category,
        page_archived,
        revision_approved,
        revisions,
        comments,
        pages,
        categories,
        users,
        blobs
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
        category_name,
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
WHERE NEW.id<>OLD.id OR NEW.joined<>OLD.joined -- OR NEW.minimal_permission<>OLD.minimal_permission -- @tfkls: currently disabled, possibly refactor to match with username
DO INSTEAD NOTHING;

CREATE RULE no_updates_categories AS
ON UPDATE TO categories
WHERE NEW.id <> OLD.id OR NEW.created <> OLD.created -- OR NEW.default_permission<>OLD.default_permission -- @tfkls: currently disabled, possibly refactor to match with name
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

CREATE RULE no_updates_blobs AS
ON UPDATE TO blobs
WHERE NEW.id <> OLD.id OR NEW.user_id <> OLD.user_id OR NEW.mime <> OLD.mime OR NEW.size <> OLD.size OR NEW.created <> OLD.created OR NEW.file <> OLD.file
DO INSTEAD NOTHING;

DO $$
    DECLARE
        name text;
    BEGIN
        FOREACH name in ARRAY $arr$
        { users,
        categories,
        pages,
        revisions,
        comments,
        blobs
        }$arr$::text[]
            LOOP
                EXECUTE 'CREATE RULE no_updates_deleted_' || name || ' AS ON UPDATE TO ' || name || ' WHERE OLD.deleted=TRUE DO INSTEAD NOTHING';
            END LOOP;
    END$$;
