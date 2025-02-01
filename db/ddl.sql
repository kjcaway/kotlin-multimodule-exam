CREATE TABLE tbl_tactics
(
    id          VARCHAR(255) PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(1024),
    states      TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_id  VARCHAR(100) NOT NULL,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_id  VARCHAR(100)
);