CREATE TABLE tbl_tactics
(
    id            VARCHAR(255) PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    description   VARCHAR(1024),
    states        TEXT,
    is_public     BOOLEAN   DEFAULT FALSE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_id    VARCHAR(100) NOT NULL,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_id    VARCHAR(100),
    is_template   BOOLEAN   DEFAULT FALSE,
    is_half_court BOOLEAN   DEFAULT FALSE
);

CREATE TABLE tbl_memberinfo
(
    id               uuid NOT NULL PRIMARY KEY,
    email            VARCHAR(64),
    name             VARCHAR(64),
    avatar_url       TEXT,
    refresh_token    VARCHAR(256) DEFAULT NULL,
    lastlogin_at     TIMESTAMP    DEFAULT NULL,
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    provider         VARCHAR(20),
    provider_user_id VARCHAR(255)
);

CREATE TABLE tbl_members
(
    id     UUID NOT NULL PRIMARY KEY,
    passwd TEXT
);

CREATE UNIQUE INDEX uidx_tbl_memberinfo_email ON tbl_memberinfo (email);

CREATE SEQUENCE public.casbin_sequence;

ALTER SEQUENCE public.casbin_sequence OWNER TO postgres;


CREATE TABLE public.tbl_casbin_rule
(
    id    INTEGER DEFAULT nextval('casbin_sequence'::regclass) NOT NULL
        PRIMARY KEY,
    ptype VARCHAR(100)                                         NOT NULL,
    v0    VARCHAR(100),
    v1    VARCHAR(100),
    v2    VARCHAR(100),
    v3    VARCHAR(100),
    v4    VARCHAR(100),
    v5    VARCHAR(100)
);

ALTER TABLE public.tbl_casbin_rule
    OWNER TO postgres;


alter table tbl_tactics
    add is_template boolean default false;


CREATE TABLE tbl_board
(
    id         VARCHAR(255) PRIMARY KEY,
    title      VARCHAR(256) NOT NULL,
    contents   TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_id VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_id VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS tbl_board_image
(
    id         VARCHAR(64) PRIMARY KEY,
    board_id   VARCHAR(64)  NULL,
    file_path  VARCHAR(512) NOT NULL,
    url_path   VARCHAR(256) NOT NULL,
    mime_type  VARCHAR(64)  NOT NULL,
    file_size  BIGINT       NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_id VARCHAR(64)  NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_board_image_board_id ON tbl_board_image (board_id);
CREATE INDEX IF NOT EXISTS idx_board_image_created_at ON tbl_board_image (created_at);

CREATE TABLE IF NOT EXISTS tbl_board_comment
(
    id         VARCHAR(64) PRIMARY KEY,
    board_id   VARCHAR(64) NOT NULL,
    parent_id  VARCHAR(64) NULL,
    contents   TEXT        NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    created_id VARCHAR(64) NOT NULL,
    updated_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP   NULL
);

CREATE INDEX IF NOT EXISTS idx_board_comment_board_id ON tbl_board_comment (board_id);
CREATE INDEX IF NOT EXISTS idx_board_comment_parent_id ON tbl_board_comment (parent_id);
CREATE INDEX IF NOT EXISTS idx_board_comment_created_at ON tbl_board_comment (created_at);
