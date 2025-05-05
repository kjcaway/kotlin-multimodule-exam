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

CREATE TABLE tbl_memberinfo
(
    id         uuid NOT NULL PRIMARY KEY,
    email      VARCHAR(64),
    name       VARCHAR(64),
    avatar_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tbl_members
(
    id     uuid NOT NULL PRIMARY KEY,
    passwd text
);

create unique index uidx_tbl_memberinfo_email on tbl_memberinfo (email);

create table tbl_casbin_rule
(
    id    integer default nextval('casbin_sequence'::regclass) not null
        primary key,
    ptype varchar(100)                                         not null,
    v0    varchar(100),
    v1    varchar(100),
    v2    varchar(100),
    v3    varchar(100),
    v4    varchar(100),
    v5    varchar(100)
);

alter table tbl_casbin_rule
    owner to postgres;

