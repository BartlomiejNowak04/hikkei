CREATE VIEW users_with_data AS
SELECT u.id,
       u.joined,
       u.deleted,
       u.date_deleted,
       un.username,
       up.picture,
       um.minimal_permission
FROM users u
LEFT JOIN LATERAL (
    SELECT username
    FROM user_name
    WHERE user_id = u.id
    ORDER BY date DESC
    LIMIT 1
) un ON true
LEFT JOIN LATERAL (
    SELECT picture
    FROM user_picture
    WHERE user_id = u.id
    ORDER BY date DESC
    LIMIT 1
) up ON true
LEFT JOIN LATERAL (
    SELECT minimal_permission
    FROM user_permission
    WHERE user_id = u.id
    ORDER BY date DESC
    LIMIT 1
) um ON true;

CREATE VIEW categories_with_data AS
SELECT c.id,
       c.created,
       c.deleted,
       c.date_deleted,
       cn.name,
       cd.default_permission
FROM categories c
LEFT JOIN LATERAL (
    SELECT name
    FROM category_name
    WHERE category_id = c.id
    ORDER BY date DESC
    LIMIT 1
) cn ON true
LEFT JOIN LATERAL (
    SELECT default_permission
    FROM category_permission
    WHERE category_id = c.id
    ORDER BY date DESC
    LIMIT 1
) cd ON true;


CREATE VIEW pages_with_data AS
SELECT p.id,
       p.user_id,
       p.created,
       p.deleted,
       p.date_deleted,
       pt.title,
       pc.category,
       pa.archived
FROM pages p
LEFT JOIN LATERAL (
    SELECT title
    FROM page_title
    WHERE page_id = p.id
    ORDER BY date DESC
    LIMIT 1
) pt ON true
LEFT JOIN LATERAL (
    SELECT category
    FROM page_category
    WHERE page_id = p.id
    ORDER BY date DESC
    LIMIT 1
) pc ON true
LEFT JOIN LATERAL (
    SELECT archived
    FROM page_archived
    WHERE page_id = p.id
    ORDER BY date DESC
    LIMIT 1
) pa ON true;

CREATE VIEW revisions_with_data AS
SELECT r.id,
       r.page_id,
       r.user_id,
       r.file,
       r.date,
       r.deleted,
       r.date_deleted,
       ra.approved
FROM revisions r
LEFT JOIN LATERAL (
    SELECT approved
    FROM revision_approved
    WHERE revision_id = r.id
    ORDER BY date DESC
    LIMIT 1
) ra ON true;

CREATE VIEW user_category_current AS
SELECT uc.user_id, uc.category_id, uc.permission
FROM user_category uc
JOIN (
    SELECT user_id, category_id, max(date) AS max_date
    FROM user_category GROUP BY user_id, category_id
) latest
ON uc.user_id=latest.user_id AND uc.category_id=latest.category_id
AND uc.date=latest.max_date
JOIN users u ON uc.user_id=u.id
JOIN categories c on uc.category_id=c.id
WHERE u.deleted=false AND c.deleted=false AND uc.permission IS NOT NULL;
