use testdb;

CREATE TABLE `tbl_member`
(
    `id`    int         NOT NULL AUTO_INCREMENT,
    `name`  varchar(45) NOT NULL,
    `email` varchar(45) NOT NULL,
    `age`   int DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb3;

