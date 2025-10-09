--  需要新建数据库 renren_write和renren_read，用于测试使用

CREATE TABLE tb_user
(
    id bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
    username varchar(50) NOT NULL COMMENT '用户名',
    mobile varchar(50) COMMENT '手机号',
    create_time datetime COMMENT '创建时间',
    primary key (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户';


CREATE TABLE tb_user_log_0
(
    id bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
    name varchar(50) NOT NULL COMMENT '名称',
    content varchar(100) COMMENT '内容',
    create_time datetime COMMENT '创建时间',
    primary key (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户日志';

CREATE TABLE tb_user_log_1
(
    id bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
    name varchar(50) NOT NULL COMMENT '名称',
    content varchar(100) COMMENT '内容',
    create_time datetime COMMENT '创建时间',
    primary key (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户日志';