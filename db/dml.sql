delete from tbl_casbin_rule where ptype = 'p';;

insert into tbl_casbin_rule (ptype, v0, v1, v2, v3, v4, v5)
values ('p', 'admin', 'courtboard', '*', '(get)|(post)|(put)|(delete)', null, null),
       ('p', 'user', 'courtboard', '/api/tactics', '(get)|(post)', null, null),
       ('p', 'user', 'courtboard', '/api/tactics/*', '(get)|(post)|(put)|(delete)', null, null),
       ('p', 'user', 'courtboard', '/api/my/*', '(get)', null, null),
       ('p', 'guest', 'courtboard', '/api/tactics', '(post)', null, null),
       ('p', 'guest', 'courtboard', '/api/tactics/*', '(get)', null, null)
;