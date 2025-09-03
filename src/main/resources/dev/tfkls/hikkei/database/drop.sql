DROP TABLE IF EXISTS
    user_name,
    user_picture,
    auth,
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
    users,
    drafts
CASCADE;

DROP FUNCTION IF EXISTS
    unique_username,
    unique_title,
    deleted_has_date,
    insert_user_with_data,
    inserted_user_has_data,
    insert_role_with_data,
    inserted_role_has_data,
    insert_category_with_data,
    inserted_category_has_data,
    insert_page_with_data,
    inserted_page_has_data,
    insert_revision_with_data,
    check_not_deleted
CASCADE;
