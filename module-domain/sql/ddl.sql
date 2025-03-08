create table tbl_member
(
    id    int auto_increment  primary key,
    name  varchar(45) not null,
    email varchar(45) not null,
    age   int null
);

-- courboard-api
create table tbl_tactics
(
    id          varchar(255) not null primary key,
    name        varchar(100) not null,
    description varchar(1024) null,
    states      text null,
    created_at  timestamp default CURRENT_TIMESTAMP null,
    created_id  varchar(100) not null,
    updated_at  timestamp default CURRENT_TIMESTAMP null,
    updated_id  varchar(100) null
);

-- performance-test-api
CREATE TABLE tbl_node
(
    id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE tbl_edge
(
    src_node_id BIGINT       NOT NULL,
    dst_node_id BIGINT       NOT NULL,
    type        VARCHAR(255) NOT NULL,
    PRIMARY KEY (src_node_id, dst_node_id, type)
);
