CREATE TABLE tbl_tactics
(
    id          VARCHAR(255) PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(1024),
    states      TEXT,
    is_public   BOOLEAN   DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_id  VARCHAR(100) NOT NULL,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_id  VARCHAR(100)
);

CREATE TABLE tbl_memberinfo
(
    id            uuid NOT NULL PRIMARY KEY,
    email         VARCHAR(64),
    name          VARCHAR(64),
    avatar_url    TEXT,
    refresh_token VARCHAR(256) DEFAULT NULL,
    lastlogin_at  TIMESTAMP    DEFAULT NULL,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
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