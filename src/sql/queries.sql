
-- name: show-tables
SHOW tables;


-- name: fk-constraints
SELECT CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE CONSTRAINT_SCHEMA = :dbname
AND REFERENCED_TABLE_SCHEMA = :dbname;

