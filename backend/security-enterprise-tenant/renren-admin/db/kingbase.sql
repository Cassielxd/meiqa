CREATE TABLE sys_tenant (
  id int8 NOT NULL,
  datasource_id int8,
  tenant_name varchar(50),
  tenant_domain varchar(200),
  status int,
  remark varchar(200),
  user_id int8,
  username varchar(50),
  tenant_mode int,
  del_flag int,
  creator int8,
  create_date timestamp,
  updater int8,
  update_date timestamp,
  PRIMARY KEY (id)
);

CREATE INDEX idx_sys_tenant_create_date on public.sys_tenant(create_date);

COMMENT ON TABLE public.sys_tenant IS '租户管理';
COMMENT ON COLUMN public.sys_tenant.id IS 'id';
COMMENT ON COLUMN public.sys_tenant.datasource_id IS '租户数据源';
COMMENT ON COLUMN public.sys_tenant.tenant_name IS '租户名称';
COMMENT ON COLUMN public.sys_tenant.tenant_domain IS '租户域名';
COMMENT ON COLUMN public.sys_tenant.status IS '状态  0：停用    1：正常';
COMMENT ON COLUMN public.sys_tenant.remark IS '备注';
COMMENT ON COLUMN public.sys_tenant.user_id IS '登录账号ID';
COMMENT ON COLUMN public.sys_tenant.username IS '登录账号';
COMMENT ON COLUMN public.sys_tenant.tenant_mode IS '租户模式  0：字段模式   1：数据源模式';
COMMENT ON COLUMN public.sys_tenant.del_flag IS '删除标识 0：未删除    1：删除';
COMMENT ON COLUMN public.sys_tenant.creator IS '创建者';
COMMENT ON COLUMN public.sys_tenant.create_date IS '创建时间';
COMMENT ON COLUMN public.sys_tenant.updater IS '更新者';
COMMENT ON COLUMN public.sys_tenant.update_date IS '更新时间';


CREATE TABLE sys_tenant_datasource (
    id int8 NOT NULL,
    name varchar(50),
    driver_class_name varchar(200),
    url varchar(500),
    username varchar(100),
    password varchar(100),
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    PRIMARY KEY (id)
);

COMMENT ON TABLE public.sys_tenant_datasource IS '租户数据源';
COMMENT ON COLUMN public.sys_tenant_datasource.id IS 'id';
COMMENT ON COLUMN public.sys_tenant_datasource.name IS '名称';
COMMENT ON COLUMN public.sys_tenant_datasource.driver_class_name IS '驱动';
COMMENT ON COLUMN public.sys_tenant_datasource.url IS 'url';
COMMENT ON COLUMN public.sys_tenant_datasource.username IS '用户名';
COMMENT ON COLUMN public.sys_tenant_datasource.password IS '密码';
COMMENT ON COLUMN public.sys_tenant_datasource.creator IS '创建者';
COMMENT ON COLUMN public.sys_tenant_datasource.create_date IS '创建时间';
COMMENT ON COLUMN public.sys_tenant_datasource.updater IS '更新者';
COMMENT ON COLUMN public.sys_tenant_datasource.update_date IS '更新时间';


CREATE TABLE sys_user (
  id int8 NOT NULL,
  username varchar(50) NOT NULL,
  password varchar(100),
  real_name varchar(50),
  head_url varchar(200),
  gender  int,
  email varchar(100),
  mobile varchar(100),
  dept_id int8,
  super_admin  int,
  super_tenant         int,
  tenant_code          int8,
  status  int,
  creator int8,
  create_date timestamp,
  updater int8,
  update_date timestamp,
  primary key (id)
);

CREATE INDEX idx_sys_user_create_date on public.sys_user(create_date);

COMMENT ON TABLE public.sys_user IS '用户管理';
COMMENT ON COLUMN public.sys_user.id IS 'id';
COMMENT ON COLUMN public.sys_user.username IS '用户名';
COMMENT ON COLUMN public.sys_user.password IS '密码';
COMMENT ON COLUMN public.sys_user.real_name IS '姓名';
COMMENT ON COLUMN public.sys_user.head_url IS '头像';
COMMENT ON COLUMN public.sys_user.gender IS '性别   0：男   1：女    2：保密';
COMMENT ON COLUMN public.sys_user.email IS '邮箱';
COMMENT ON COLUMN public.sys_user.mobile IS '手机号';
COMMENT ON COLUMN public.sys_user.dept_id IS '部门ID';
COMMENT ON COLUMN public.sys_user.super_admin IS '超级管理员   0：否   1：是';
COMMENT ON COLUMN public.sys_user.status IS '状态  0：停用    1：正常';
COMMENT ON COLUMN public.sys_user.creator IS '创建者';
COMMENT ON COLUMN public.sys_user.create_date IS '创建时间';
COMMENT ON COLUMN public.sys_user.updater IS '更新者';
COMMENT ON COLUMN public.sys_user.update_date IS '更新时间';


CREATE TABLE sys_dept (
  id int8 NOT NULL,
  pid int8,
  pids varchar(500),
  name varchar(50),
  leader_id int8,
  sort  int,
  tenant_code          int8,
  creator int8,
  create_date timestamp,
  updater int8,
  update_date timestamp,
  primary key (id)
);
CREATE INDEX idx_sys_dept_pid on public.sys_dept(pid);
CREATE INDEX idx_sys_dept_idx_sort on public.sys_dept(sort);

COMMENT ON TABLE public.sys_dept IS '部门管理';
COMMENT ON COLUMN public.sys_dept.id IS 'id';
COMMENT ON COLUMN public.sys_dept.pid IS '上级ID';
COMMENT ON COLUMN public.sys_dept.pids IS '所有上级ID，用逗号分开';
COMMENT ON COLUMN public.sys_dept.name IS '部门名称';
COMMENT ON COLUMN public.sys_dept.sort IS '排序';
COMMENT ON COLUMN public.sys_dept.creator IS '创建者';
COMMENT ON COLUMN public.sys_dept.create_date IS '创建时间';
COMMENT ON COLUMN public.sys_dept.updater IS '更新者';
COMMENT ON COLUMN public.sys_dept.update_date IS '更新时间';


create table sys_role
(
  id                   int8 NOT NULL,
  name                 varchar(50),
  remark               varchar(100),
  dept_id              int8,
  tenant_code          int8,
  creator              int8,
  create_date          timestamp,
  updater              int8,
  update_date          timestamp,
  primary key (id)
);

CREATE INDEX idx_sys_role_dept_id on public.sys_role(dept_id);

COMMENT ON TABLE public.sys_role IS '角色管理';
COMMENT ON COLUMN public.sys_role.id IS 'id';
COMMENT ON COLUMN public.sys_role.name IS '角色名称';
COMMENT ON COLUMN public.sys_role.remark IS '备注';
COMMENT ON COLUMN public.sys_role.dept_id IS '部门ID';
COMMENT ON COLUMN public.sys_role.creator IS '创建者';
COMMENT ON COLUMN public.sys_role.create_date IS '创建时间';
COMMENT ON COLUMN public.sys_role.updater IS '更新者';
COMMENT ON COLUMN public.sys_role.update_date IS '更新时间';


create table sys_menu
(
  id                   int8 NOT NULL,
  pid                  int8,
  url                  varchar(200),
  permissions          varchar(500),
  menu_type            int,
  open_style           int,
  menu_hide            int default 0,
  icon                 varchar(50),
  sort                 int,
  creator              int8,
  create_date          timestamp,
  updater              int8,
  update_date          timestamp,
  primary key (id)
);

CREATE INDEX idx_sys_menu_pid on public.sys_menu(pid);
CREATE INDEX idx_sys_menu_sort on public.sys_menu(sort);

COMMENT ON TABLE public.sys_menu IS '菜单管理';
COMMENT ON COLUMN public.sys_menu.id IS 'id';
COMMENT ON COLUMN public.sys_menu.pid IS '上级ID，一级菜单为0';
COMMENT ON COLUMN public.sys_menu.url IS '菜单URL';
COMMENT ON COLUMN public.sys_menu.permissions IS '授权(多个用逗号分隔，如：sys:user:list,sys:user:save)';
COMMENT ON COLUMN public.sys_menu.menu_type IS '类型   0：菜单   1：按钮';
COMMENT ON COLUMN public.sys_menu.open_style IS '打开方式   0：内部   1：外部';
COMMENT ON COLUMN public.sys_menu.icon IS '菜单图标';
COMMENT ON COLUMN public.sys_menu.sort IS '排序';
COMMENT ON COLUMN public.sys_menu.creator IS '创建者';
COMMENT ON COLUMN public.sys_menu.create_date IS '创建时间';
COMMENT ON COLUMN public.sys_menu.updater IS '更新者';
COMMENT ON COLUMN public.sys_menu.update_date IS '更新时间';


create table sys_role_user
(
  id                   int8 NOT NULL,
  role_id              int8,
  user_id              int8,
  creator              int8,
  create_date          timestamp,
  primary key (id)
);

CREATE INDEX idx_sys_role_user_role_id on public.sys_role_user(role_id);
CREATE INDEX idx_sys_role_user_user_id on public.sys_role_user(user_id);

COMMENT ON TABLE public.sys_role_user IS '角色用户关系';
COMMENT ON COLUMN public.sys_role_user.role_id IS '角色ID';
COMMENT ON COLUMN public.sys_role_user.user_id IS '用户ID';
COMMENT ON COLUMN public.sys_role_user.creator IS '创建者';
COMMENT ON COLUMN public.sys_role_user.create_date IS '创建时间';


create table sys_role_menu
(
  id                   int8 NOT NULL,
  role_id              int8,
  menu_id              int8,
  creator              int8,
  create_date          timestamp,
  primary key (id)
);

CREATE INDEX idx_sys_role_menu_role_id on public.sys_role_menu(role_id);
CREATE INDEX idx_sys_role_menu_menu_id on public.sys_role_menu(menu_id);

COMMENT ON TABLE public.sys_role_menu IS '角色菜单关系';
COMMENT ON COLUMN public.sys_role_menu.role_id IS '角色ID';
COMMENT ON COLUMN public.sys_role_menu.menu_id IS '菜单ID';
COMMENT ON COLUMN public.sys_role_menu.creator IS '创建者';
COMMENT ON COLUMN public.sys_role_menu.create_date IS '创建时间';


create table sys_role_data_scope
(
  id                   int8 NOT NULL,
  role_id              int8,
  dept_id              int8,
  creator              int8,
  create_date          timestamp,
  primary key (id)
);
CREATE INDEX idx_data_scope_role_id on public.sys_role_data_scope(role_id);

COMMENT ON TABLE public.sys_role_data_scope IS '角色数据权限';
COMMENT ON COLUMN public.sys_role_data_scope.role_id IS '角色ID';
COMMENT ON COLUMN public.sys_role_data_scope.dept_id IS '部门ID';
COMMENT ON COLUMN public.sys_role_data_scope.creator IS '创建者';
COMMENT ON COLUMN public.sys_role_data_scope.create_date IS '创建时间';


create table sys_params
(
  id                   int8 NOT NULL,
  param_code           varchar(32),
  param_value          varchar(2000),
  param_type           int DEFAULT 1 NOT NULL,
  remark               varchar(200),
  creator              int8,
  create_date          timestamp,
  updater              int8,
  update_date          timestamp,
  primary key (id)
);
CREATE UNIQUE INDEX uk_sys_params_param_code on public.sys_params(param_code);
CREATE INDEX idx_sys_params_create_date on public.sys_params(create_date);

COMMENT ON TABLE public.sys_params IS '参数管理';
COMMENT ON COLUMN public.sys_params.param_code IS '参数编码';
COMMENT ON COLUMN public.sys_params.param_value IS '参数值';
COMMENT ON COLUMN public.sys_params.param_type IS '类型   0：系统参数   1：非系统参数';
COMMENT ON COLUMN public.sys_params.remark IS '备注';
COMMENT ON COLUMN public.sys_params.creator IS '创建者';
COMMENT ON COLUMN public.sys_params.create_date IS '创建时间';
COMMENT ON COLUMN public.sys_params.updater IS '更新者';
COMMENT ON COLUMN public.sys_params.update_date IS '更新时间';


create table sys_dict_type
(
    id                   int8 NOT NULL,
    dict_type            varchar(100),
    dict_name            varchar(255),
    remark               varchar(255),
    sort                 int,
    creator              int8,
    create_date          timestamp,
    updater              int8,
    update_date          timestamp,
    primary key (id)
);
CREATE UNIQUE INDEX uk_sys_dict_type_dict_type on public.sys_dict_type(dict_type);

COMMENT ON TABLE public.sys_dict_type IS '字典类型';
COMMENT ON COLUMN public.sys_dict_type.id IS 'id';
COMMENT ON COLUMN public.sys_dict_type.dict_type IS '字典类型';
COMMENT ON COLUMN public.sys_dict_type.dict_name IS '字典名称';
COMMENT ON COLUMN public.sys_dict_type.remark IS '备注';
COMMENT ON COLUMN public.sys_dict_type.sort IS '排序';
COMMENT ON COLUMN public.sys_dict_type.creator IS '创建者';
COMMENT ON COLUMN public.sys_dict_type.create_date IS '创建时间';
COMMENT ON COLUMN public.sys_dict_type.updater IS '更新者';
COMMENT ON COLUMN public.sys_dict_type.update_date IS '更新时间';

create table sys_dict_data
(
    id                   int8 NOT NULL,
    dict_type_id         int8 NOT NULL,
    dict_label           varchar(255),
    dict_value           varchar(255),
    remark               varchar(255),
    sort                 int,
    creator              int8,
    create_date          timestamp,
    updater              int8,
    update_date          timestamp,
    primary key (id)
);
CREATE INDEX idx_sys_dict_data_sort on public.sys_dict_data(sort);
CREATE UNIQUE INDEX uk_dict_type_value on public.sys_dict_data(dict_type_id, dict_value);

COMMENT ON TABLE public.sys_dict_data IS '字典数据';
COMMENT ON COLUMN public.sys_dict_data.id IS 'id';
COMMENT ON COLUMN public.sys_dict_data.dict_type_id IS '字典类型ID';
COMMENT ON COLUMN public.sys_dict_data.dict_label IS '字典标签';
COMMENT ON COLUMN public.sys_dict_data.dict_value IS '字典值';
COMMENT ON COLUMN public.sys_dict_data.remark IS '备注';
COMMENT ON COLUMN public.sys_dict_data.sort IS '排序';
COMMENT ON COLUMN public.sys_dict_data.creator IS '创建者';
COMMENT ON COLUMN public.sys_dict_data.create_date IS '创建时间';
COMMENT ON COLUMN public.sys_dict_data.updater IS '更新者';
COMMENT ON COLUMN public.sys_dict_data.update_date IS '更新时间';


create table sys_log_login
(
  id                   int8 NOT NULL,
  operation            int,
  status               int,
  user_agent           varchar(500),
  ip                   varchar(32),
  creator_name         varchar(50),
  creator              int8,
  create_date          timestamp,
  primary key (id)
);
CREATE INDEX idx_login_status on public.sys_log_login(status);
CREATE INDEX idx_login_create_date on public.sys_log_login(create_date);

COMMENT ON TABLE public.sys_log_login IS '登录日志';
COMMENT ON COLUMN public.sys_log_login.id IS 'id';
COMMENT ON COLUMN public.sys_log_login.operation IS '用户操作   0：用户登录   1：用户退出';
COMMENT ON COLUMN public.sys_log_login.status IS '状态  0：失败    1：成功    2：账号已锁定';
COMMENT ON COLUMN public.sys_log_login.user_agent IS '用户代理';
COMMENT ON COLUMN public.sys_log_login.ip IS '操作IP';
COMMENT ON COLUMN public.sys_log_login.creator_name IS '用户名';
COMMENT ON COLUMN public.sys_log_login.creator IS '创建者';
COMMENT ON COLUMN public.sys_log_login.create_date IS '创建时间';


create table sys_log_operation
(
  id                   int8 NOT NULL,
  operation            varchar(50),
  request_uri          varchar(200),
  request_method       varchar(20),
  request_params       text,
  request_time          int,
  user_agent           varchar(500),
  ip                   varchar(32),
  status                int,
  creator_name         varchar(50),
  creator              int8,
  create_date          timestamp,
  primary key (id)
);
CREATE INDEX idx_operation_create_date on public.sys_log_operation(create_date);

COMMENT ON TABLE public.sys_log_operation IS '操作日志';
COMMENT ON COLUMN public.sys_log_operation.id IS 'id';
COMMENT ON COLUMN public.sys_log_operation.operation IS '用户操作';
COMMENT ON COLUMN public.sys_log_operation.request_uri IS '请求URI';
COMMENT ON COLUMN public.sys_log_operation.request_method IS '请求方式';
COMMENT ON COLUMN public.sys_log_operation.request_params IS '请求参数';
COMMENT ON COLUMN public.sys_log_operation.request_time IS '请求时长(毫秒)';
COMMENT ON COLUMN public.sys_log_operation.user_agent IS '用户代理';
COMMENT ON COLUMN public.sys_log_operation.ip IS '操作IP';
COMMENT ON COLUMN public.sys_log_operation.status IS '状态  0：失败   1：成功';
COMMENT ON COLUMN public.sys_log_operation.creator_name IS '用户名';
COMMENT ON COLUMN public.sys_log_operation.creator IS '创建者';
COMMENT ON COLUMN public.sys_log_operation.create_date IS '创建时间';


create table sys_log_error
(
  id                   int8 NOT NULL,
  request_uri          varchar(200),
  request_method       varchar(20),
  request_params       text,
  user_agent           varchar(500),
  ip                   varchar(32),
  error_info           text,
  creator              int8,
  create_date          timestamp,
  primary key (id)
);
CREATE INDEX idx_error_create_date on public.sys_log_error(create_date);

COMMENT ON TABLE public.sys_log_error IS '异常日志';
COMMENT ON COLUMN public.sys_log_error.id IS 'id';
COMMENT ON COLUMN public.sys_log_error.request_uri IS '请求URI';
COMMENT ON COLUMN public.sys_log_error.request_method IS '请求方式';
COMMENT ON COLUMN public.sys_log_error.request_params IS '请求参数';
COMMENT ON COLUMN public.sys_log_error.user_agent IS '用户代理';
COMMENT ON COLUMN public.sys_log_error.ip IS '操作IP';
COMMENT ON COLUMN public.sys_log_error.error_info IS '异常信息';
COMMENT ON COLUMN public.sys_log_error.creator IS '创建者';
COMMENT ON COLUMN public.sys_log_error.create_date IS '创建时间';


create table sys_sms
(
    id                   int8 NOT NULL,
    sms_code             varchar(32),
    platform             int,
    sms_config           varchar(2000),
    remark               varchar(200),
    creator              int8,
    create_date          timestamp,
    updater              int8,
    update_date          timestamp,
    primary key (id)
);
CREATE UNIQUE INDEX uk_sms_code on public.sys_sms(sms_code);
CREATE INDEX idx_sys_sms_create_date on public.sys_sms(create_date);

COMMENT ON TABLE public.sys_sms IS '短信';
COMMENT ON COLUMN public.sys_sms.id IS 'id';
COMMENT ON COLUMN public.sys_sms.sms_code IS '短信编码';
COMMENT ON COLUMN public.sys_sms.platform IS '平台类型';
COMMENT ON COLUMN public.sys_sms.sms_config IS '短信配置';
COMMENT ON COLUMN public.sys_sms.remark IS '备注';
COMMENT ON COLUMN public.sys_sms.creator IS '创建者';
COMMENT ON COLUMN public.sys_sms.create_date IS '创建时间';
COMMENT ON COLUMN public.sys_sms.updater IS '更新者';
COMMENT ON COLUMN public.sys_sms.update_date IS '更新时间';


CREATE TABLE sys_sms_log (
    id int8 NOT NULL,
    sms_code   varchar(32),
    platform int,
    mobile varchar(20),
    params_1 varchar(50),
    params_2 varchar(50),
    params_3 varchar(50),
    params_4 varchar(50),
    status int,
    creator int8,
    create_date timestamp,
    PRIMARY KEY (id)
);
CREATE INDEX idx_sys_sms_log_sms_code on public.sys_sms_log(sms_code);

COMMENT ON TABLE public.sys_sms_log IS '短信发送记录';
COMMENT ON COLUMN public.sys_sms_log.id IS 'id';
COMMENT ON COLUMN public.sys_sms_log.sms_code IS '短信编码';
COMMENT ON COLUMN public.sys_sms_log.platform IS '平台类型';
COMMENT ON COLUMN public.sys_sms_log.mobile IS '手机号';
COMMENT ON COLUMN public.sys_sms_log.params_1 IS '参数1';
COMMENT ON COLUMN public.sys_sms_log.params_2 IS '参数2';
COMMENT ON COLUMN public.sys_sms_log.params_3 IS '参数3';
COMMENT ON COLUMN public.sys_sms_log.params_4 IS '参数4';
COMMENT ON COLUMN public.sys_sms_log.status IS '发送状态  0：失败  1：成功';
COMMENT ON COLUMN public.sys_sms_log.creator IS '创建者';
COMMENT ON COLUMN public.sys_sms_log.create_date IS '创建时间';

CREATE TABLE sys_notice (
    id int8 NOT NULL,
    notice_type int NOT NULL,
    title varchar(200),
    content text,
    receiver_type int,
    receiver_type_ids varchar(500),
    status int,
    sender_name varchar(50),
    sender_date timestamp,
    creator int8,
    create_date timestamp,
    PRIMARY KEY (id)
);
CREATE INDEX idx_sys_notice_create_date on public.sys_notice(create_date);

COMMENT ON TABLE public.sys_notice IS '通知管理';
COMMENT ON COLUMN public.sys_notice.id IS 'id';
COMMENT ON COLUMN public.sys_notice.notice_type IS '通知类型';
COMMENT ON COLUMN public.sys_notice.title IS '标题';
COMMENT ON COLUMN public.sys_notice.content IS '内容';
COMMENT ON COLUMN public.sys_notice.receiver_type IS '接收者  0：全部  1：部门';
COMMENT ON COLUMN public.sys_notice.receiver_type_ids IS '接收者ID，用逗号分开';
COMMENT ON COLUMN public.sys_notice.status IS '发送状态  0：草稿  1：已发布';
COMMENT ON COLUMN public.sys_notice.sender_name IS '发送者';
COMMENT ON COLUMN public.sys_notice.sender_date IS '发送时间';
COMMENT ON COLUMN public.sys_notice.creator IS '创建者';
COMMENT ON COLUMN public.sys_notice.create_date IS '创建时间';


CREATE TABLE sys_notice_user (
    receiver_id int8 NOT NULL,
    notice_id int8 NOT NULL,
    read_status int NOT NULL,
    read_date timestamp,
    PRIMARY KEY (receiver_id, notice_id)
);

COMMENT ON TABLE public.sys_notice_user IS '我的通知';
COMMENT ON COLUMN public.sys_notice_user.receiver_id IS '接收者ID';
COMMENT ON COLUMN public.sys_notice_user.notice_id IS '通知ID';
COMMENT ON COLUMN public.sys_notice_user.read_status IS '阅读状态  0：未读  1：已读';
COMMENT ON COLUMN public.sys_notice_user.read_date IS '阅读时间';


CREATE TABLE sys_mail_template (
  id int8 NOT NULL,
  name varchar(100),
  subject varchar(200),
  content text,
  creator int8,
  create_date timestamp,
  updater int8,
  update_date timestamp,
  PRIMARY KEY (id)
);

CREATE INDEX idx_mail_template_create_date on public.sys_mail_template(create_date);

COMMENT ON TABLE public.sys_mail_template IS '邮件模板';
COMMENT ON COLUMN public.sys_mail_template.id IS 'id';
COMMENT ON COLUMN public.sys_mail_template.name IS '模板名称';
COMMENT ON COLUMN public.sys_mail_template.subject IS '邮件主题';
COMMENT ON COLUMN public.sys_mail_template.content IS '邮件正文';
COMMENT ON COLUMN public.sys_mail_template.creator IS '创建者';
COMMENT ON COLUMN public.sys_mail_template.create_date IS '创建时间';
COMMENT ON COLUMN public.sys_mail_template.updater IS '更新者';
COMMENT ON COLUMN public.sys_mail_template.update_date IS '更新时间';


CREATE TABLE sys_mail_log (
  id int8 NOT NULL,
  template_id int8 NOT NULL,
  mail_from varchar(200),
  mail_to varchar(400),
  mail_cc varchar(400),
  subject varchar(200),
  content text,
  status  int,
  creator int8,
  create_date timestamp,
  PRIMARY KEY (id)
);

CREATE INDEX idx_mail_log_create_date on public.sys_mail_log(create_date);

COMMENT ON TABLE public.sys_mail_log IS '邮件发送记录';
COMMENT ON COLUMN public.sys_mail_log.id IS 'id';
COMMENT ON COLUMN public.sys_mail_log.template_id IS '邮件模板ID';
COMMENT ON COLUMN public.sys_mail_log.mail_from IS '发送者';
COMMENT ON COLUMN public.sys_mail_log.mail_to IS '收件人';
COMMENT ON COLUMN public.sys_mail_log.mail_cc IS '抄送者';
COMMENT ON COLUMN public.sys_mail_log.subject IS '邮件主题';
COMMENT ON COLUMN public.sys_mail_log.content IS '邮件正文';
COMMENT ON COLUMN public.sys_mail_log.status IS '发送状态  0：失败  1：成功';
COMMENT ON COLUMN public.sys_mail_log.creator IS '创建者';
COMMENT ON COLUMN public.sys_mail_log.create_date IS '创建时间';


CREATE TABLE sys_oss (
  id int8 NOT NULL,
  url varchar(200),
  creator int8,
  create_date timestamp,
  PRIMARY KEY (id)
);
CREATE INDEX idx_sys_oss_create_date on public.sys_oss(create_date);

COMMENT ON TABLE public.sys_oss IS '文件上传';
COMMENT ON COLUMN public.sys_oss.id IS 'id';
COMMENT ON COLUMN public.sys_oss.url IS 'URL地址';
COMMENT ON COLUMN public.sys_oss.creator IS '创建者';
COMMENT ON COLUMN public.sys_oss.create_date IS '创建时间';


CREATE TABLE schedule_job (
  id int8 NOT NULL,
  bean_name varchar(200),
  params varchar(2000),
  cron_expression varchar(100),
  status  int,
  remark varchar(255),
  creator int8,
  create_date timestamp,
  updater int8,
  update_date timestamp,
  PRIMARY KEY (id)
);

CREATE INDEX idx_schedule_job_create_date on public.schedule_job(create_date);

COMMENT ON TABLE public.schedule_job IS '定时任务';
COMMENT ON COLUMN public.schedule_job.id IS 'id';
COMMENT ON COLUMN public.schedule_job.bean_name IS 'spring bean名称';
COMMENT ON COLUMN public.schedule_job.params IS '参数';
COMMENT ON COLUMN public.schedule_job.cron_expression IS 'cron表达式';
COMMENT ON COLUMN public.schedule_job.status IS '任务状态  0：暂停  1：正常';
COMMENT ON COLUMN public.schedule_job.remark IS '备注';
COMMENT ON COLUMN public.schedule_job.creator IS '创建者';
COMMENT ON COLUMN public.schedule_job.create_date IS '创建时间';
COMMENT ON COLUMN public.schedule_job.updater IS '更新者';
COMMENT ON COLUMN public.schedule_job.update_date IS '更新时间';


CREATE TABLE schedule_job_log (
  id int8 NOT NULL,
  job_id int8 NOT NULL,
  bean_name varchar(200),
  params varchar(2000),
  status  int,
  error varchar(2000),
  times  int,
  create_date timestamp,
  PRIMARY KEY (id)
);

CREATE INDEX idx_job_log_job_id on public.schedule_job_log(job_id);
CREATE INDEX idx_job_log_create_date on public.schedule_job_log(create_date);

COMMENT ON TABLE public.schedule_job_log IS '定时任务日志';
COMMENT ON COLUMN public.schedule_job_log.id IS 'id';
COMMENT ON COLUMN public.schedule_job_log.bean_name IS 'spring bean名称';
COMMENT ON COLUMN public.schedule_job_log.params IS '参数';
COMMENT ON COLUMN public.schedule_job_log.status IS '任务状态  0：失败    1：成功';
COMMENT ON COLUMN public.schedule_job_log.error IS '失败信息';
COMMENT ON COLUMN public.schedule_job_log.times IS '耗时(单位：毫秒)';
COMMENT ON COLUMN public.schedule_job.create_date IS '创建时间';


CREATE TABLE sys_user_token (
  id int8 NOT NULL,
  user_id int8,
  token varchar(100),
  expire_date timestamp,
  update_date timestamp,
  create_date timestamp,
  PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_sys_user_token_user_id on public.sys_user_token(user_id);
CREATE UNIQUE INDEX uk_sys_user_token on public.sys_user_token(token);

COMMENT ON TABLE public.sys_user_token IS '系统用户Token';
COMMENT ON COLUMN public.sys_user_token.id IS 'id';
COMMENT ON COLUMN public.sys_user_token.user_id IS '用户id';
COMMENT ON COLUMN public.sys_user_token.token IS '用户token';
COMMENT ON COLUMN public.sys_user_token.expire_date IS '过期时间';
COMMENT ON COLUMN public.sys_user_token.update_date IS '更新时间';
COMMENT ON COLUMN public.sys_user_token.create_date IS '创建时间';


CREATE TABLE sys_language (
  table_name varchar(32) NOT NULL,
  table_id int8 NOT NULL,
  field_name varchar(32) NOT NULL,
  field_value varchar(200) NOT NULL,
  language varchar(10) NOT NULL,
  primary key (table_name, table_id, field_name, language)
);

CREATE INDEX idx_sys_language_table_id on public.sys_language(table_id);

COMMENT ON TABLE public.sys_language IS '国际化';
COMMENT ON COLUMN public.sys_language.table_name IS '表名';
COMMENT ON COLUMN public.sys_language.table_id IS '表主键';
COMMENT ON COLUMN public.sys_language.field_name IS '字段名';
COMMENT ON COLUMN public.sys_language.field_value IS '字段值';
COMMENT ON COLUMN public.sys_language.language IS '语言';

CREATE TABLE sys_region (
    id int8 NOT NULL,
    pid int8,
    name varchar(100),
    tree_level int,
    leaf int,
    sort int8,
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    PRIMARY KEY (id)
);

COMMENT ON TABLE public.sys_region IS '行政区域';
COMMENT ON COLUMN public.sys_region.pid IS '上级ID';
COMMENT ON COLUMN public.sys_region.name IS '名称';
COMMENT ON COLUMN public.sys_region.tree_level IS '层级';
COMMENT ON COLUMN public.sys_region.leaf IS '是否叶子节点  0：否   1：是';
COMMENT ON COLUMN public.sys_region.sort IS '排序';
COMMENT ON COLUMN public.sys_region.creator IS '创建者';
COMMENT ON COLUMN public.sys_region.create_date IS '创建时间';
COMMENT ON COLUMN public.sys_region.updater IS '更新者';
COMMENT ON COLUMN public.sys_region.update_date IS '更新时间';

CREATE TABLE tb_news (
  id int8 NOT NULL,
  title varchar(100),
  content text,
  pub_date timestamp,
  dept_id int8,
  creator int8,
  create_date timestamp,
  updater int8,
  update_date timestamp,
  PRIMARY KEY (id)
);

COMMENT ON TABLE public.tb_news IS '新闻管理';
COMMENT ON COLUMN public.tb_news.id IS 'id';
COMMENT ON COLUMN public.tb_news.title IS '标题';
COMMENT ON COLUMN public.tb_news.content IS '内容';
COMMENT ON COLUMN public.tb_news.pub_date IS '发布时间';
COMMENT ON COLUMN public.tb_news.dept_id IS '创建者dept_id';
COMMENT ON COLUMN public.tb_news.creator IS '创建者';
COMMENT ON COLUMN public.tb_news.create_date IS '创建时间';
COMMENT ON COLUMN public.tb_news.updater IS '更新者';
COMMENT ON COLUMN public.tb_news.update_date IS '更新时间';

CREATE TABLE tb_product (
    id int8 NOT NULL,
    name varchar(100),
    content varchar(5000),
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    PRIMARY KEY (id)
);

COMMENT ON TABLE public.tb_product IS '产品管理';
COMMENT ON COLUMN public.tb_product.id IS 'id';
COMMENT ON COLUMN public.tb_product.name IS '产品名称';
COMMENT ON COLUMN public.tb_product.content IS '产品介绍';
COMMENT ON COLUMN public.tb_product.creator IS '创建者';
COMMENT ON COLUMN public.tb_product.create_date IS '创建时间';
COMMENT ON COLUMN public.tb_product.updater IS '更新者';
COMMENT ON COLUMN public.tb_product.update_date IS '更新时间';

CREATE TABLE tb_product_params (
    id int8 NOT NULL,
    param_name varchar(100),
    param_value varchar(200),
    product_id int8,
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    PRIMARY KEY (id)
);

COMMENT ON TABLE public.tb_product_params IS '产品参数管理';
COMMENT ON COLUMN public.tb_product_params.id IS 'id';
COMMENT ON COLUMN public.tb_product_params.param_name IS '参数名';
COMMENT ON COLUMN public.tb_product_params.param_value IS '参数值';
COMMENT ON COLUMN public.tb_product_params.product_id IS '产品ID';
COMMENT ON COLUMN public.tb_product_params.creator IS '创建者';
COMMENT ON COLUMN public.tb_product_params.create_date IS '创建时间';
COMMENT ON COLUMN public.tb_product_params.updater IS '更新者';
COMMENT ON COLUMN public.tb_product_params.update_date IS '更新时间';


create table sys_post (
    id int8 NOT NULL,
    post_code varchar(100),
    post_name varchar(100),
    sort int,
    status int,
    tenant_code          int8,
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    PRIMARY KEY (id)
);

COMMENT ON TABLE public.sys_post IS '岗位管理';
COMMENT ON COLUMN public.sys_post.id IS 'id';
COMMENT ON COLUMN public.sys_post.post_code IS '岗位编码';
COMMENT ON COLUMN public.sys_post.post_name IS '岗位名称';
COMMENT ON COLUMN public.sys_post.sort IS '排序';
COMMENT ON COLUMN public.sys_post.status IS '状态  0：停用   1：正常';
COMMENT ON COLUMN public.sys_post.creator IS '创建者';
COMMENT ON COLUMN public.sys_post.create_date IS '创建时间';
COMMENT ON COLUMN public.sys_post.updater IS '更新者';
COMMENT ON COLUMN public.sys_post.update_date IS '更新时间';


CREATE TABLE sys_user_post (
    id                   int8 NOT NULL,
    post_id              int8,
    user_id              int8,
    creator              int8,
    create_date          timestamp,
    primary key (id)
);

CREATE INDEX idx_sys_user_post_post_id on public.sys_user_post(post_id);
CREATE INDEX idx_sys_user_post_user_id on public.sys_user_post(user_id);

COMMENT ON TABLE public.sys_user_post IS '用户岗位关系';
COMMENT ON COLUMN public.sys_user_post.id IS 'id';
COMMENT ON COLUMN public.sys_user_post.post_id IS '岗位ID';
COMMENT ON COLUMN public.sys_user_post.user_id IS '用户ID';
COMMENT ON COLUMN public.sys_post.creator IS '创建者';
COMMENT ON COLUMN public.sys_post.create_date IS '创建时间';


CREATE TABLE tb_excel_data (
    id int8 NOT NULL,
    real_name varchar(100),
    user_identity varchar(100),
    address varchar(200),
    join_date timestamp,
    class_name varchar(100),
    creator int8,
    create_date timestamp,
    primary key (id)
);

COMMENT ON TABLE public.tb_excel_data IS 'Excel导入演示';
COMMENT ON COLUMN public.tb_excel_data.id IS 'id';
COMMENT ON COLUMN public.tb_excel_data.real_name IS '学生姓名';
COMMENT ON COLUMN public.tb_excel_data.user_identity IS '身份证';
COMMENT ON COLUMN public.tb_excel_data.address IS '家庭地址';
COMMENT ON COLUMN public.tb_excel_data.join_date IS '入学日期';
COMMENT ON COLUMN public.tb_excel_data.class_name IS '班级名称';
COMMENT ON COLUMN public.tb_excel_data.creator IS '创建者';
COMMENT ON COLUMN public.tb_excel_data.create_date IS '创建时间';

CREATE TABLE tb_order (
    id int8 NOT NULL,
    order_id int8,
    product_id int8,
    product_name varchar(100),
    pay_amount numeric(10,2),
    status int,
    user_id int8,
    pay_at timestamp,
    create_date timestamp,
    primary key (id)
);

CREATE UNIQUE INDEX uk_tb_order_order_id on public.tb_order(order_id);

COMMENT ON TABLE public.tb_order IS '订单';
COMMENT ON COLUMN public.tb_order.id IS 'id';
COMMENT ON COLUMN public.tb_order.order_id IS '订单ID';
COMMENT ON COLUMN public.tb_order.product_id IS '产品ID';
COMMENT ON COLUMN public.tb_order.product_name IS '产品名称';
COMMENT ON COLUMN public.tb_order.pay_amount IS '支付金额';
COMMENT ON COLUMN public.tb_order.status IS '订单状态  -1：已取消   0：等待付款   1：已完成';
COMMENT ON COLUMN public.tb_order.user_id IS '购买用户ID';
COMMENT ON COLUMN public.tb_order.pay_at IS '支付时间';
COMMENT ON COLUMN public.tb_order.create_date IS '下单时间';


CREATE TABLE tb_alipay_notify_log (
    id int8 NOT NULL,
    out_trade_no int8,
    total_amount numeric(10,2),
    buyer_pay_amount numeric(10,2),
    receipt_amount numeric(10,2),
    invoice_amount numeric(10,2),
    notify_id varchar(50),
    buyer_id varchar(50),
    seller_id varchar(50),
    trade_no varchar(50),
    trade_status varchar(50),
    create_date timestamp,
    primary key (id)
);

COMMENT ON TABLE public.tb_alipay_notify_log IS '支付宝回调日志';
COMMENT ON COLUMN public.tb_alipay_notify_log.id IS 'id';
COMMENT ON COLUMN public.tb_alipay_notify_log.out_trade_no IS '订单号';
COMMENT ON COLUMN public.tb_alipay_notify_log.total_amount IS '订单金额';
COMMENT ON COLUMN public.tb_alipay_notify_log.buyer_pay_amount IS '付款金额';
COMMENT ON COLUMN public.tb_alipay_notify_log.receipt_amount IS '实收金额';
COMMENT ON COLUMN public.tb_alipay_notify_log.invoice_amount IS '开票金额';
COMMENT ON COLUMN public.tb_alipay_notify_log.notify_id IS '通知校验ID';
COMMENT ON COLUMN public.tb_alipay_notify_log.buyer_id IS '买家支付宝用户号';
COMMENT ON COLUMN public.tb_alipay_notify_log.seller_id IS '卖家支付宝用户号';
COMMENT ON COLUMN public.tb_alipay_notify_log.trade_no IS '支付宝交易号';
COMMENT ON COLUMN public.tb_alipay_notify_log.trade_status IS '交易状态';
COMMENT ON COLUMN public.tb_alipay_notify_log.create_date IS '创建时间';


CREATE TABLE tb_wechat_notify_log (
    id int8 NOT NULL,
    out_trade_no varchar(100),
    total int,
    payer_total int,
    currency varchar(50),
    payer_currency varchar(50),
    bank_type varchar(50),
    trade_state varchar(50),
    trade_state_desc varchar(500),
    trade_type varchar(50),
    transaction_id varchar(100),
    success_time varchar(100),
    create_date timestamp,
    primary key (id)
);

COMMENT ON TABLE public.tb_wechat_notify_log IS '微信回调日志';
COMMENT ON COLUMN public.tb_wechat_notify_log.id IS 'id';
COMMENT ON COLUMN public.tb_wechat_notify_log.out_trade_no IS '订单号';
COMMENT ON COLUMN public.tb_wechat_notify_log.total IS '订单总金额，单位为分';
COMMENT ON COLUMN public.tb_wechat_notify_log.payer_total IS '用户支付金额，单位为分';
COMMENT ON COLUMN public.tb_wechat_notify_log.currency IS 'CNY：人民币，境内商户号仅支持人民币';
COMMENT ON COLUMN public.tb_wechat_notify_log.payer_currency IS '用户支付币种';
COMMENT ON COLUMN public.tb_wechat_notify_log.bank_type IS '银行类型';
COMMENT ON COLUMN public.tb_wechat_notify_log.trade_state IS '交易状态';
COMMENT ON COLUMN public.tb_wechat_notify_log.trade_state_desc IS '交易状态描述';
COMMENT ON COLUMN public.tb_wechat_notify_log.trade_type IS '交易类型';
COMMENT ON COLUMN public.tb_wechat_notify_log.transaction_id IS '微信支付系统生成的订单号';
COMMENT ON COLUMN public.tb_wechat_notify_log.success_time IS '支付完成时间';
COMMENT ON COLUMN public.tb_wechat_notify_log.create_date IS '创建时间';


CREATE TABLE mp_account (
    id int8 NOT NULL,
    name varchar(100),
    app_id varchar(100),
    app_secret varchar(100),
    token varchar(100),
    aes_key varchar(100),
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    primary key (id)
);

COMMENT ON TABLE public.mp_account IS '公众号账号管理';
COMMENT ON COLUMN public.mp_account.id IS 'id';
COMMENT ON COLUMN public.mp_account.name IS '名称';
COMMENT ON COLUMN public.mp_account.app_id IS 'AppID';
COMMENT ON COLUMN public.mp_account.app_secret IS 'AppSecret';
COMMENT ON COLUMN public.mp_account.token IS 'Token';
COMMENT ON COLUMN public.mp_account.aes_key IS 'EncodingAESKey';
COMMENT ON COLUMN public.mp_account.creator IS '创建者';
COMMENT ON COLUMN public.mp_account.create_date IS '创建时间';
COMMENT ON COLUMN public.mp_account.updater IS '更新者';
COMMENT ON COLUMN public.mp_account.update_date IS '更新时间';


CREATE TABLE mp_menu (
    id int8 NOT NULL,
    menu varchar(2000),
    app_id varchar(100),
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    primary key (id)
);

COMMENT ON TABLE public.mp_menu IS '公众号自定义菜单';
COMMENT ON COLUMN public.mp_menu.id IS 'id';
COMMENT ON COLUMN public.mp_menu.menu IS '菜单json数据';
COMMENT ON COLUMN public.mp_menu.app_id IS 'AppID';
COMMENT ON COLUMN public.mp_menu.creator IS '创建者';
COMMENT ON COLUMN public.mp_menu.create_date IS '创建时间';
COMMENT ON COLUMN public.mp_menu.updater IS '更新者';
COMMENT ON COLUMN public.mp_menu.update_date IS '更新时间';


CREATE TABLE bpm_form (
    id int8 NOT NULL,
    name varchar(50),
    remark varchar(200),
    content text,
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    primary key (id)
);

COMMENT ON TABLE public.bpm_form IS '流程表单';
COMMENT ON COLUMN public.bpm_form.id IS 'id';
COMMENT ON COLUMN public.bpm_form.name IS '名称';
COMMENT ON COLUMN public.bpm_form.remark IS '备注';
COMMENT ON COLUMN public.bpm_form.content IS '表单内容';
COMMENT ON COLUMN public.bpm_form.creator IS '创建者';
COMMENT ON COLUMN public.bpm_form.create_date IS '创建时间';
COMMENT ON COLUMN public.bpm_form.updater IS '更新者';
COMMENT ON COLUMN public.bpm_form.update_date IS '更新时间';

CREATE TABLE bpm_definition_ext (
    id int8 NOT NULL,
    model_id varchar(64),
    process_definition_id varchar(64),
    form_type varchar(64),
    form_id varchar(500),
    form_content text,
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    primary key (id)
);

COMMENT ON TABLE public.bpm_definition_ext IS '流程定义扩展';
COMMENT ON COLUMN public.bpm_definition_ext.id IS 'id';
COMMENT ON COLUMN public.bpm_definition_ext.model_id IS '名称';
COMMENT ON COLUMN public.bpm_definition_ext.process_definition_id IS '流程定义ID';
COMMENT ON COLUMN public.bpm_definition_ext.form_id IS '表单ID';
COMMENT ON COLUMN public.bpm_definition_ext.form_content IS '表单内容';
COMMENT ON COLUMN public.bpm_definition_ext.creator IS '创建者';
COMMENT ON COLUMN public.bpm_definition_ext.create_date IS '创建时间';
COMMENT ON COLUMN public.bpm_definition_ext.updater IS '更新者';
COMMENT ON COLUMN public.bpm_definition_ext.update_date IS '更新时间';

CREATE TABLE bpm_instance_ext (
    id int8 NOT NULL,
    process_instance_id varchar(64),
    process_definition_id varchar(64),
    form_variables varchar(4000),
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    primary key (id)
);

COMMENT ON TABLE public.bpm_instance_ext IS '流程实例扩展';
COMMENT ON COLUMN public.bpm_instance_ext.id IS 'id';
COMMENT ON COLUMN public.bpm_instance_ext.process_instance_id IS '流程实例ID';
COMMENT ON COLUMN public.bpm_instance_ext.process_definition_id IS '流程定义ID';
COMMENT ON COLUMN public.bpm_instance_ext.form_variables IS '表单值';
COMMENT ON COLUMN public.bpm_instance_ext.creator IS '创建者';
COMMENT ON COLUMN public.bpm_instance_ext.create_date IS '创建时间';
COMMENT ON COLUMN public.bpm_instance_ext.updater IS '更新者';
COMMENT ON COLUMN public.bpm_instance_ext.update_date IS '更新时间';

CREATE TABLE bpm_form_correction (
    id int8 NOT NULL,
    apply_post varchar(255),
    entry_date timestamp,
    correction_date timestamp,
    work_content varchar(2000),
    achievement varchar(2000),
    instance_id varchar(80),
    creator int8,
    create_date timestamp,
    PRIMARY KEY (id)
);


CREATE TABLE ai_model
(
    id               int8 NOT NULL,
    platform         varchar(200) NOT NULL,
    name             varchar(100) NOT NULL,
    model            varchar(100) NOT NULL,
    api_url          varchar(200) NOT NULL,
    api_key          varchar(200),
    status           int,
    sort             int,
    creator          int8,
    create_date      timestamp,
    primary key (id)
);

COMMENT ON TABLE public.ai_model IS 'AI模型';
COMMENT ON COLUMN public.ai_model.id IS 'id';
COMMENT ON COLUMN public.ai_model.platform IS '所属平台';
COMMENT ON COLUMN public.ai_model.name IS '模型名称';
COMMENT ON COLUMN public.ai_model.model IS '模型标识';
COMMENT ON COLUMN public.ai_model.api_url IS 'API地址';
COMMENT ON COLUMN public.ai_model.api_key IS 'API秘钥';
COMMENT ON COLUMN public.ai_model.status IS '状态  0：停用   1：正常';
COMMENT ON COLUMN public.ai_model.sort IS '排序';
COMMENT ON COLUMN public.ai_model.creator IS '创建者';

CREATE TABLE ai_chat_conversation
(
    id               int8 NOT NULL,
    title            varchar(200) NOT NULL,
    model_id         int8 NOT NULL,
    model            varchar(100) NOT NULL,
    user_id          int8,
    create_date      timestamp,
    primary key (id)
);

COMMENT ON TABLE public.ai_chat_conversation IS 'AI聊天对话';
COMMENT ON COLUMN public.ai_chat_conversation.id IS 'id';
COMMENT ON COLUMN public.ai_chat_conversation.title IS '对话标题';
COMMENT ON COLUMN public.ai_chat_conversation.model_id IS '模型ID';
COMMENT ON COLUMN public.ai_chat_conversation.model IS '模型标识';
COMMENT ON COLUMN public.ai_chat_conversation.user_id IS '用户ID';
COMMENT ON COLUMN public.ai_chat_conversation.create_date IS '创建时间';

CREATE TABLE ai_chat_message
(
    id               int8 NOT NULL,
    type             varchar(50) NOT NULL,
    content          varchar(5000) NOT NULL,
    model_id         int8 NOT NULL,
    model            varchar(100) NOT NULL,
    conversation_id  int8,
    create_date      timestamp,
    primary key (id)
);

COMMENT ON TABLE public.ai_chat_message IS 'AI聊天消息';
COMMENT ON COLUMN public.ai_chat_message.id IS 'id';
COMMENT ON COLUMN public.ai_chat_message.type IS '消息类型';
COMMENT ON COLUMN public.ai_chat_message.content IS '回答内容';
COMMENT ON COLUMN public.ai_chat_message.model_id IS '模型ID';
COMMENT ON COLUMN public.ai_chat_message.model IS '模型标识';
COMMENT ON COLUMN public.ai_chat_message.conversation_id IS '对话ID';
COMMENT ON COLUMN public.ai_chat_message.create_date IS '创建时间';



-- 初始数据
INSERT INTO public.sys_tenant (id, datasource_id, tenant_name, tenant_domain, remark, user_id, username, status, tenant_mode, del_flag, creator, create_date, updater, update_date) VALUES (10000, NULL, '默认租户', NULL, NULL, 1067246875800000001, 'admin', 1, 0, 0, NULL, now(), NULL, now());

INSERT INTO public.sys_user(id, username, password, real_name, gender, email, mobile, status, dept_id, super_admin, tenant_code, super_tenant, creator, create_date, updater, update_date) VALUES (1067246875800000001, 'admin', '$2a$10$012Kx2ba5jzqr9gLlG4MX.bnQJTD9UWqF57XDo2N3.fPtLne02u/m', '管理员', 0, 'root@renren.io', '13612345678', 1, null, 1, 10000, 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_user(id, username, password, real_name, gender, email, mobile, dept_id, super_admin, status, tenant_code, super_tenant, creator, create_date, updater, update_date) VALUES (1353943117220315138, 'test', '$2a$10$012Kx2ba5jzqr9gLlG4MX.bnQJTD9UWqF57XDo2N3.fPtLne02u/m', '测试用户', 0, 'test@renren.io', '13012345678', 1067246875800000066, 0, 1, 10000, 0, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000002, '0', NULL, NULL, 0, 0, 'icon-safetycertificate', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000003, 1067246875800000055, NULL, 'sys:user:save,sys:dept:list,sys:role:list', 1, 0, NULL, 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000004, 1067246875800000055, NULL, 'sys:user:update,sys:dept:list,sys:role:list', 1, 0, NULL, 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000005, 1067246875800000055, NULL, 'sys:user:delete', 1, 0, NULL, 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000006, 1067246875800000055, NULL, 'sys:user:export', 1, 0, NULL, 4, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000007, 1067246875800000002, 'sys/role', NULL, 0, 0, 'icon-team', 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000008, 1067246875800000007, NULL, 'sys:role:page,sys:role:info', 1, 0, NULL, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000009, 1067246875800000007, NULL, 'sys:role:save,sys:menu:select,sys:dept:list', 1, 0, NULL, 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000010, 1067246875800000007, NULL, 'sys:role:update,sys:menu:select,sys:dept:list', 1, 0, NULL, 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000011, 1067246875800000007, NULL, 'sys:role:delete', 1, 0, NULL, 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000012, 1067246875800000002, 'sys/dept', NULL, 0, 0, 'icon-apartment', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000014, 1067246875800000012, NULL, 'sys:dept:list,sys:dept:info', 1, 0, NULL, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000015, 1067246875800000012, NULL, 'sys:dept:save', 1, 0, NULL, 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000016, 1067246875800000012, NULL, 'sys:dept:update', 1, 0, NULL, 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000017, 1067246875800000012, NULL, 'sys:dept:delete', 1, 0, NULL, 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000018, 1150940491508928513, 'flow/bpmform', 'flow:bpmform:all', 0, 0, 'icon-detail', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000019, 1150940491508928513, 'flow/model', 'sys:model:all', 0, 0, 'icon-appstore-fill', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000020, 1150940491508928513, 'flow/running', 'sys:running:all,sys:flow:all', 0, 0, 'icon-play-square', 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000021, 1067246875800000024, 'message/sms', 'sys:sms:all', 0, 0, 'icon-message-fill', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000022, 1067246875800000024, 'message/mail_template', 'sys:mail:all', 0, 0, 'icon-mail', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000023, 1067246875800000024, 'message/mail_log', 'sys:mail:log', 0, 0, 'icon-detail-fill', 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000024, '0', NULL, NULL, 0, 0, 'icon-message', 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000025, 1067246875800000035, 'sys/menu', NULL, 0, 0, 'icon-unorderedlist', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000026, 1067246875800000025, NULL, 'sys:menu:list,sys:menu:info', 1, 0, NULL, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000027, 1067246875800000025, NULL, 'sys:menu:save', 1, 0, NULL, 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000028, 1067246875800000025, NULL, 'sys:menu:update', 1, 0, NULL, 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000029, 1067246875800000025, NULL, 'sys:menu:delete', 1, 0, NULL, 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000030, 1067246875800000035, 'job/schedule', NULL, 0, 0, 'icon-dashboard', 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000031, 1067246875800000030, NULL, 'sys:schedule:page,sys:schedule:info', 1, 0, NULL, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000032, 1067246875800000030, NULL, 'sys:schedule:save', 1, 0, NULL, 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000033, 1067246875800000030, NULL, 'sys:schedule:update', 1, 0, NULL, 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000034, 1067246875800000030, NULL, 'sys:schedule:delete', 1, 0, NULL, 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000035, '0', NULL, NULL, 0, 0, 'icon-setting', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000036, 1067246875800000030, NULL, 'sys:schedule:pause', 1, 0, NULL, 4, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000037, 1067246875800000030, NULL, 'sys:schedule:resume', 1, 0, NULL, 5, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000038, 1067246875800000030, NULL, 'sys:schedule:run', 1, 0, NULL, 6, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000039, 1067246875800000030, NULL, 'sys:schedule:log', 1, 0, NULL, 7, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000040, 1067246875800000035, 'sys/params', '', 0, 0, 'icon-fileprotect', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000041, 1067246875800000035, 'sys/dict-type', NULL, 0, 0, 'icon-golden-fill', 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000042, 1067246875800000041, NULL, 'sys:dict:page,sys:dict:info', 1, 0, NULL, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000043, 1067246875800000041, NULL, 'sys:dict:save', 1, 0, NULL, 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000044, 1067246875800000041, NULL, 'sys:dict:update', 1, 0, NULL, 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000045, 1067246875800000041, NULL, 'sys:dict:delete', 1, 0, NULL, 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000046, '0', NULL, NULL, 0, 0, 'icon-container', 5, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000047, 1067246875800000035, 'oss/oss', 'sys:oss:all', 0, 0, 'icon-upload', 4, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000048, 1067246875800000046, 'sys/log-login', 'sys:log:login', 0, 0, 'icon-filedone', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000049, 1067246875800000046, 'sys/log-operation', 'sys:log:operation', 0, 0, 'icon-solution', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000050, 1067246875800000046, 'sys/log-error', 'sys:log:error', 0, 0, 'icon-file-exception', 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000051, 1067246875800000053, '{{ApiUrl}}/druid/sql.html', NULL, 0, 0, 'icon-database', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000052, 1067246875800000054, 'demo/news', 'demo:news:all', 0, 0, 'icon-file-word', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000053, '0', NULL, NULL, 0, 0, 'icon-desktop', 5, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000054, '0', NULL, NULL, 0, 0, 'icon-windows', 6, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000055, 1067246875800000002, 'sys/user', NULL, 0, 0, 'icon-user', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000056, 1067246875800000055, NULL, 'sys:user:page,sys:user:info', 1, 0, NULL, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000057, 1067246875800000040, NULL, 'sys:params:save', 1, 0, NULL, 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000058, 1067246875800000040, NULL, 'sys:params:export', 1, 0, NULL, 4, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000059, 1067246875800000040, '', 'sys:params:page,sys:params:info', 1, 0, NULL, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000060, 1067246875800000040, NULL, 'sys:params:update', 1, 0, NULL, 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000061, 1067246875800000040, '', 'sys:params:delete', 1, 0, '', 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1150940491508928513, '0', NULL, NULL, 0, 0, 'icon-reloadtime', 4, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1150941310262235138, '0',  NULL, NULL, 0, 0, 'icon-user', 4, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1150941384811794433, 1150941310262235138, 'flow/todo', 'sys:flow:all,sys:user:page', 0, 0, 'icon-dashboard', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1150941447038488577, 1150941310262235138, 'flow/done', 'sys:flow:all', 0, 0, 'icon-check-square', 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1150941506626965506, 1150941310262235138, 'flow/start', 'sys:flow:all', 0, 0, 'icon-play-square', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1150941588046794754, 1150941310262235138, 'flow/my-send', 'sys:flow:all', 0, 0, 'icon-edit-square', 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1156748733921165314, 1067246875800000053, '{{ApiUrl}}/doc.html', '', 0, 1, 'icon-file-word', 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1113375699198033921, 0, '', '', 0, 0, 'icon-home', 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1113378406403162113, 1113375699198033921, 'tenant/tenant', 'sys:tenant:all,sys:datasource:all', 0, 0, 'icon-team', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1114779542196600833, 1113375699198033921, 'tenant/tenant-datasource', 'tenant:datasource:all', 0, 0, 'icon-database', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1114779542196600834, 1113375699198033921, 'tenant/tenant-role', 'sys:tenantrole:all', 0, 0, 'icon-carryout-fill', 1, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1158267114314842115, 1067246875800000035, 'sys/region', NULL, 0, 0, 'icon-location', 4, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1158267114314842116, 1158267114314842115, NULL, 'sys:region:list,sys:region:info', 1, 0, NULL, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1158267114314842117, 1158267114314842115, NULL, 'sys:region:save', 1, 0, NULL, 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1158267114314842118, 1158267114314842115, NULL, 'sys:region:update', 1, 0, NULL, 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1158267114314842119, 1158267114314842115, NULL, 'sys:region:delete', 1, 0, NULL, 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000300, '0',  NULL, NULL, 0, 0, 'icon-bell', 5, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000301, 1067246875800000300, 'notice/notice', 'sys:notice:all', 0, 0, 'icon-bell', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000302, 1067246875800000300, 'notice/notice-user', 'sys:notice:all', 0, 0, 'icon-notification', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1067246875800000303, 1067246875800000024, 'message/sms-log', 'sys:smslog:all', 0, 0, 'icon-unorderedlist', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1270380959719501825, 1067246875800000054, 'demo/product', NULL, 0, 0, 'icon-tag', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1270380959719501826, 1270380959719501825, NULL, 'demo:product:page,demo:product:info', 1, 0, NULL, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1270380959719501827, 1270380959719501825, NULL, 'demo:product:save', 1, 0, NULL, 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1270380959719501828, 1270380959719501825, NULL, 'demo:product:update', 1, 0, NULL, 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1270380959719501829, 1270380959719501825, NULL, 'demo:product:delete', 1, 0, NULL, 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1341596622612987906, 1067246875800000002, 'sys/post', NULL, 0, 0, 'icon-pic-left', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1341596622688485377, 1341596622612987906, NULL, 'sys:post:page,sys:post:info', 1, 0, NULL, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1341596622755594242, 1341596622612987906, NULL, 'sys:post:save', 1, 0, NULL, 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1341596622835286018, 1341596622612987906, NULL, 'sys:post:update', 1, 0, NULL, 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1341596622902394881, 1341596622612987906, NULL, 'sys:post:delete', 1, 0, NULL, 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1341659662599495681, 1067246875800000053, 'sys/online', 'sys:online:all', 0, 0, 'icon-team', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1341676084016852994, 1067246875800000054, 'demo/excel', 'demo:excel:all', 0, 0, 'icon-table', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1343074487777677313, 0, '', '', 0, 0, 'icon-Dollar', 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1343074685589442561, 1343074487777677313, 'pay/order', 'pay:order:all', 0, 0, 'icon-unorderedlist', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1343074794440019970, 1343074487777677313, 'pay/alipaynotifylog', 'pay:alipayNotifyLog:all', 0, 0, 'icon-filedone', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1343074794440019971, 1343074487777677313, 'pay/wechatnotifylog', 'pay:wechatNotifyLog:all', 0, 0, 'icon-filedone', 2, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1270380959719501800, 1067246875800000054, 'demo/charts', NULL, 0, 0, 'icon-tag', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu (id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1419551957005963266, 0, '', '', 0, 0, 'icon-wechat-fill', 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu (id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1419553543706972161, 1419551957005963266, 'mp/account', 'mp:account:all', 0, 0, 'icon-user', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu (id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1419963799817691137, 1419551957005963266, 'mp/menu', 'mp:menu:all', 0, 0, 'icon-unorderedlist', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1300278047072649217, 0, '', '', 0, 0, 'icon-filesearch', 3, 1067246875800000001, now(), 1067246875800000001, now());



INSERT INTO public.sys_dict_type (id, dict_type, dict_name, remark, sort, creator, create_date, updater, update_date) VALUES (1899835801273425922, 'model_status', '模型状态', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_type (id, dict_type, dict_name, remark, sort, creator, create_date, updater, update_date) VALUES (1899836220481527809, 'ai_platform', 'AI 平台', '', 0, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1899835910191112194, 1899835801273425922, '禁用', '0', '', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1899835947377811457, 1899835801273425922, '启用', '1', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1899836364186771458, 1899836220481527809, 'OpenAI', 'OpenAI', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1899839861087051778, 1899836220481527809, 'DeepSeek', 'DeepSeek', '', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1899839914707034114, 1899836220481527809, 'Ollama', 'Ollama', '', 2, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO public.sys_menu (id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1899830353732349953, 0, '', '', 0, 0, 'icon-font-colors', 4, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu (id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1899830660956729345, 1899830353732349953, 'ai/model/index', 'ai:model', 0, 0, 'icon-safetycertificate', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu (id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1899830944986607617, 1899830353732349953, 'ai/chat/index', 'ai:chat', 0, 0, 'icon-filesearch', 1, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1899830353732349953, 'name', 'AI Model', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1899830353732349953, 'name', 'AI 大模型', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1899830353732349953, 'name', 'AI 大模型', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1899830660956729345, 'name', 'AI Model', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1899830660956729345, 'name', 'AI 模型', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1899830660956729345, 'name', 'AI 模型', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1899830944986607617, 'name', 'AI Chat', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1899830944986607617, 'name', 'AI 对话', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1899830944986607617, 'name', 'AI 对话', 'zh-TW');


INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000002, 'name', 'Authority Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000002, 'name', '权限管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000002, 'name', '權限管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000003, 'name', 'Add', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000003, 'name', '新增', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000003, 'name', '新增', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000004, 'name', 'Edit', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000004, 'name', '修改', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000004, 'name', '修改', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000005, 'name', 'Delete', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000005, 'name', '删除', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000005, 'name', '刪除', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000006, 'name', 'Export', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000006, 'name', '导出', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000006, 'name', '導出', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000007, 'name', 'Role Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000007, 'name', '角色管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000007, 'name', '角色管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000008, 'name', 'View', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000008, 'name', '查看', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000008, 'name', '查看', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000009, 'name', 'Add', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000009, 'name', '新增', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000009, 'name', '新增', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000010, 'name', 'Edit', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000010, 'name', '修改', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000010, 'name', '修改', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000011, 'name', 'Delete', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000011, 'name', '删除', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000011, 'name', '刪除', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000012, 'name', 'Department Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000012, 'name', '部门管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000012, 'name', '部門管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000014, 'name', 'View', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000014, 'name', '查看', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000014, 'name', '查看', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000015, 'name', 'Add', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000015, 'name', '新增', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000015, 'name', '新增', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000016, 'name', 'Edit', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000016, 'name', '修改', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000016, 'name', '修改', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000017, 'name', 'Delete', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000017, 'name', '删除', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000017, 'name', '刪除', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000018, 'name', 'Form Design', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000018, 'name', '表单设计', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000018, 'name', '表单设计', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000019, 'name', 'Process Design', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000019, 'name', '流程设计', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000019, 'name', '流程设计', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000020, 'name', 'Running Process', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000020, 'name', '运行中的流程', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000020, 'name', '運行中的流程', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000021, 'name', 'SMS Service', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000021, 'name', '短信服务', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000021, 'name', '短信服務', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000022, 'name', 'Mail Template', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000022, 'name', '邮件模板', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000022, 'name', '郵件模板', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000023, 'name', 'Mail Log', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000023, 'name', '邮件发送记录', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000023, 'name', '郵件發送記錄', 'zh-TW ');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000024, 'name', 'Message Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000024, 'name', '消息管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000024, 'name', '消息管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000025, 'name', 'Menu Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000025, 'name', '菜单管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000025, 'name', '菜單管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000026, 'name', 'View', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000026, 'name', '查看', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000026, 'name', '查看', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000027, 'name', 'Add', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000027, 'name', '新增', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000027, 'name', '新增', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000028, 'name', 'Edit', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000028, 'name', '修改', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000028, 'name', '修改', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000029, 'name', 'Delete', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000029, 'name', '删除', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000029, 'name', '刪除', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000030, 'name', 'Timed Task', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000030, 'name', '定时任务', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000030, 'name', '定時任務', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000031, 'name', 'View', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000031, 'name', '查看', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000031, 'name', '查看', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000032, 'name', 'Add', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000032, 'name', '新增', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000032, 'name', '新增', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000033, 'name', 'Edit', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000033, 'name', '修改', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000033, 'name', '修改', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000034, 'name', 'Delete', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000034, 'name', '删除', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000034, 'name', '刪除', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000035, 'name', 'Setting', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000035, 'name', '系统设置', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000035, 'name', '系統設置', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000036, 'name', 'Pause', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000036, 'name', '暂停', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000036, 'name', '暫停', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000037, 'name', 'Resume', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000037, 'name', '恢复', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000037, 'name', '恢復', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000038, 'name', 'Execute', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000038, 'name', '立即执行', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000038, 'name', '立即執行', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000039, 'name', 'Log List', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000039, 'name', '日志列表', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000039, 'name', '日誌列表', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000040, 'name', 'Parameter Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000040, 'name', '参数管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000040, 'name', '參數管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000041, 'name', 'Dict Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000041, 'name', '字典管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000041, 'name', '字典管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000042, 'name', 'View', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000042, 'name', '查看', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000042, 'name', '查看', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000043, 'name', 'Add', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000043, 'name', '新增', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000043, 'name', '新增', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000044, 'name', 'Edit', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000044, 'name', '修改', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000044, 'name', '修改', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000045, 'name', 'Delete', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000045, 'name', '删除', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000045, 'name', '刪除', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000046, 'name', 'Log Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000046, 'name', '日志管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000046, 'name', '日誌管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000047, 'name', 'File Upload', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000047, 'name', '文件上传', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000047, 'name', '文件上傳', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000048, 'name', 'Login Log', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000048, 'name', '登录日志', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000048, 'name', '登錄日誌', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000049, 'name', 'Operation Log', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000049, 'name', '操作日志', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000049, 'name', '操作日誌', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000050, 'name', 'Error Log', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000050, 'name', '异常日志', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000050, 'name', '異常日誌', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000051, 'name', 'SQL Monitoring', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000051, 'name', 'SQL监控', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000051, 'name', 'SQL監控', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000052, 'name', 'News Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000052, 'name', '新闻管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000052, 'name', '新聞管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000053, 'name', 'System Monitoring', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000053, 'name', '系统监控', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000053, 'name', '系統監控', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000054, 'name', 'Demo', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000054, 'name', '功能示例', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000054, 'name', '功能示例', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000055, 'name', 'User Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000055, 'name', '用户管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000055, 'name', '用戶管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000056, 'name', 'View', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000056, 'name', '查看', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000056, 'name', '查看', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000057, 'name', 'Add', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000057, 'name', '新增', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000057, 'name', '新增', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000058, 'name', 'Export', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000058, 'name', '导出', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000058, 'name', '導出', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000059, 'name', 'View', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000059, 'name', '查看', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000059, 'name', '查看', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000060, 'name', 'Edit', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000060, 'name', '修改', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000060, 'name', '修改', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000061, 'name', 'Delete', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000061, 'name', '删除', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000061, 'name', '刪除', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1150940491508928513, 'name', 'Process Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1150940491508928513, 'name', '流程管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1150940491508928513, 'name', '流程管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1150941310262235138, 'name', 'Personal Office', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1150941310262235138, 'name', '办公管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1150941310262235138, 'name', '辦公管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1150941384811794433, 'name', 'Todo', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1150941384811794433, 'name', '待办任务', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1150941384811794433, 'name', '待辦任務', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1150941447038488577, 'name', 'Task Already', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1150941447038488577, 'name', '已办任务', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1150941447038488577, 'name', '已辦任務', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1150941506626965506, 'name', 'Initiation Process', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1150941506626965506, 'name', '发起流程', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1150941506626965506, 'name', '發起流程', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1150941588046794754, 'name', 'My Send', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1150941588046794754, 'name', '我发起的', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1150941588046794754, 'name', '我發起的', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1156748733921165314, 'name', 'Interface Document', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1156748733921165314, 'name', '接口文档', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1156748733921165314, 'name', '接口文檔', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1113375699198033921, 'name', 'Tenant Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1113375699198033921, 'name', '租户管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1113375699198033921, 'name', '租戶管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1113378406403162113, 'name', 'Tenant Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1113378406403162113, 'name', '租户管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1113378406403162113, 'name', '租戶管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1114779542196600833, 'name', 'Tenant DataSource', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1114779542196600833, 'name', '租户数据源', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1114779542196600833, 'name', '租戶數據源', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1114779542196600834, 'name', 'Tenant Package', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1114779542196600834, 'name', '租户套餐', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1114779542196600834, 'name', '租户套餐', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1158267114314842115, 'name', 'Administrative Regions', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1158267114314842115, 'name', '行政区域', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1158267114314842115, 'name', '行政區域', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1158267114314842116, 'name', 'View', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1158267114314842116, 'name', '查看', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1158267114314842116, 'name', '查看', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1158267114314842117, 'name', 'Add', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1158267114314842117, 'name', '新增', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1158267114314842117, 'name', '新增', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1158267114314842118, 'name', 'Edit', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1158267114314842118, 'name', '修改', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1158267114314842118, 'name', '修改', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1158267114314842119, 'name', 'Delete', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1158267114314842119, 'name', '删除', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1158267114314842119, 'name', '刪除', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000300, 'name', 'Station Notice', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000300, 'name', '站内通知', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000300, 'name', '站內通知', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000301, 'name', 'Notice Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000301, 'name', '通知管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000301, 'name', '通知管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000302, 'name', 'My Notice', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000302, 'name', '我的通知', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000302, 'name', '我的通知', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000303, 'name', 'SMS History', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000303, 'name', '短信发送记录', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000303, 'name', '短信發送記錄', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1300278047072649217, 'name', 'Report Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1300278047072649217, 'name', '报表管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1300278047072649217, 'name', '報表管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501825, 'name', 'Master And Child', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501825, 'name', '主子表演示', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501825, 'name', '主子表演示', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501826, 'name', 'View', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501826, 'name', '查看', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501826, 'name', '查看', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501827, 'name', 'Add', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501827, 'name', '新增', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501827, 'name', '新增', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501828, 'name', 'Edit', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501828, 'name', '修改', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501828, 'name', '修改', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501829, 'name', 'Delete', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501829, 'name', '删除', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501829, 'name', '刪除', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501800, 'name', 'ECharts', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501800, 'name', 'ECharts', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501800, 'name', 'ECharts', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341659662599495681, 'name', 'Online User', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341659662599495681, 'name', '在线用户', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341659662599495681, 'name', '在線用戶', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341676084016852994, 'name', 'Excel Demo', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341676084016852994, 'name', 'Excel导入演示', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341676084016852994, 'name', 'Excel導入演示', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074487777677313, 'name', 'Pay Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074487777677313, 'name', '支付管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074487777677313, 'name', '支付管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074685589442561, 'name', 'Order Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074685589442561, 'name', '订单管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074685589442561, 'name', '訂單管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074794440019970, 'name', 'AliPay Log', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074794440019970, 'name', '支付宝回调日志', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074794440019970, 'name', '支付寶回調日誌', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074794440019971, 'name', 'WeChat Log', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074794440019971, 'name', '微信回调日志', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074794440019971, 'name', '微信回調日誌', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622612987906, 'name', 'Job Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622612987906, 'name', '岗位管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622612987906, 'name', '崗位管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622688485377, 'name', 'View', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622688485377, 'name', '查看', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622688485377, 'name', '查看', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622755594242, 'name', 'Add', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622755594242, 'name', '新增', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622755594242, 'name', '新增', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622835286018, 'name', 'Edit', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622835286018, 'name', '修改', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622835286018, 'name', '修改', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622902394881, 'name', 'Delete', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622902394881, 'name', '删除', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622902394881, 'name', '刪除', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1419551957005963266, 'name', 'Wechat Management', 'en-US');
INSERT INTO public.sys_language (table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1419551957005963266, 'name', '微信管理', 'zh-CN');
INSERT INTO public.sys_language (table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1419551957005963266, 'name', '微信管理', 'zh-TW');
INSERT INTO public.sys_language (table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1419553543706972161, 'name', 'Mp Management', 'en-US');
INSERT INTO public.sys_language (table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1419553543706972161, 'name', '公众号管理', 'zh-CN');
INSERT INTO public.sys_language (table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1419553543706972161, 'name', '公眾號管理', 'zh-TW');
INSERT INTO public.sys_language (table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1419963799817691137, 'name', 'Custom Menu', 'en-US');
INSERT INTO public.sys_language (table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1419963799817691137, 'name', '自定义菜单', 'zh-CN');
INSERT INTO public.sys_language (table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1419963799817691137, 'name', '自定義選單', 'zh-TW');

INSERT INTO public.sys_dept(id, pid, pids, name, sort, tenant_code, creator, create_date, updater, update_date) VALUES (1067246875800000062, 1067246875800000063, '1067246875800000066,1067246875800000063', '技术部', 2, 10000, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dept(id, pid, pids, name, sort, tenant_code, creator, create_date, updater, update_date) VALUES (1067246875800000063, 1067246875800000066, '1067246875800000066', '长沙分公司', 1, 10000, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dept(id, pid, pids, name, sort, tenant_code, creator, create_date, updater, update_date) VALUES (1067246875800000064, 1067246875800000066, '1067246875800000066', '上海分公司', 0, 10000, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dept(id, pid, pids, name, sort, tenant_code, creator, create_date, updater, update_date) VALUES (1067246875800000065, 1067246875800000064, '1067246875800000066,1067246875800000064', '市场部', 0, 10000, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dept(id, pid, pids, name, sort, tenant_code, creator, create_date, updater, update_date) VALUES (1067246875800000066, 0, '0', '人人开源集团', 0, 10000, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dept(id, pid, pids, name, sort, tenant_code, creator, create_date, updater, update_date) VALUES (1067246875800000067, 1067246875800000064, '1067246875800000066,1067246875800000064', '销售部', 0, 10000, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dept(id, pid, pids, name, sort, tenant_code, creator, create_date, updater, update_date) VALUES (1067246875800000068, 1067246875800000063, '1067246875800000066,1067246875800000063', '产品部', 1, 10000, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO public.sys_dict_type(id, dict_type, dict_name, remark, sort, creator, create_date, updater, update_date) VALUES (1160061077912858625, 'gender', '性别', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_type(id, dict_type, dict_name, remark, sort, creator, create_date, updater, update_date) VALUES (1341593474355838978, 'post_status', '岗位管理状态', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_type(id, dict_type, dict_name, remark, sort, creator, create_date, updater, update_date) VALUES (1343069688596295682, 'order_status', '订单状态', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_type(id, dict_type, dict_name, remark, sort, creator, create_date, updater, update_date) VALUES (1225813644059140097, 'notice_type', '站内通知-类型', '', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_type (id, dict_type, dict_name, remark, sort, creator, create_date, updater, update_date) VALUES (1524264808701771777, 'tenant_datasource', '租户数据源', '', 0, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO public.sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1160061112075464705, 1160061077912858625, '男', '0', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1160061146967879681, 1160061077912858625, '女', '1', '', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1160061190127267841, 1160061077912858625, '保密', '2', '', 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1341593562419445762, 1341593474355838978, '停用', '0', '', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1341593595407646722, 1341593474355838978, '正常', '1', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1343069765549191170, 1343069688596295682, '已取消', '-1', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1343069839847092226, 1343069688596295682, '等待付款', '0', '', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1343069914518286337, 1343069688596295682, '已完成', '1', '', 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1524265018958036993, 1524264808701771777, 'com.mysql.cj.jdbc.Driver', 'com.mysql.cj.jdbc.Driver', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1524265078106112001, 1524264808701771777, 'oracle.jdbc.OracleDriver', 'oracle.jdbc.OracleDriver', '', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1524265128836218881, 1524264808701771777, 'org.postgresql.Driver', 'org.postgresql.Driver', '', 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1524265165754482689, 1524264808701771777, 'com.microsoft.sqlserver.jdbc.SQLServerDriver', 'com.microsoft.sqlserver.jdbc.SQLServerDriver', '', 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1572226197755904002, 1524264808701771777, 'dm.jdbc.driver.DmDriver', 'dm.jdbc.driver.DmDriver', '', 4, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO public.sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1225814069634195457, 1225813644059140097, '公告', '0', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1225814107559092225, 1225813644059140097, '会议', '1', '', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1225814271879340034, 1225813644059140097, '其他', '2', '', 2, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO public.sys_post(id, post_code, post_name, sort, status, tenant_code, creator, create_date, updater, update_date) VALUES (1341597192832811009, 'tech', '技术岗', 0, 1, 10000, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO public.tb_excel_data(id, real_name, user_identity, address, join_date, class_name, creator, create_date) VALUES (1343762012112445441, '大力', '430212199910102980', '上海市长宁区中山公园', now(), '姚班2101', 1067246875800000001, now());

INSERT INTO public.sys_sms(id, sms_code, platform, sms_config, remark, creator, create_date, updater, update_date) VALUES (1228954061084676097, '1001', 1, '{"aliyunAccessKeyId":"1","aliyunAccessKeySecret":"1","aliyunSignName":"1","aliyunTemplateCode":"1","qcloudAppKey":"","qcloudSignName":"","qcloudTemplateId":"","qiniuAccessKey":"","qiniuSecretKey":"","qiniuTemplateId":""}', '', 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO public.sys_params(id, param_code, param_value, param_type, remark, creator, create_date, updater, update_date) VALUES (1067246875800000073, 'CLOUD_STORAGE_CONFIG_KEY', '{"type":1,"qiniuDomain":"http://test.oss.renren.io","qiniuPrefix":"upload","qiniuAccessKey":"NrgMfABZxWLo5B-YYSjoE8-AZ1EISdi1Z3ubLOeZ","qiniuSecretKey":"uIwJHevMRWU0VLxFvgy0tAcOdGqasdtVlJkdy6vV","qiniuBucketName":"renren-oss","aliyunDomain":"","aliyunPrefix":"","aliyunEndPoint":"","aliyunAccessKeyId":"","aliyunAccessKeySecret":"","aliyunBucketName":"","qcloudDomain":"","qcloudPrefix":"","qcloudSecretId":"","qcloudSecretKey":"","qcloudBucketName":""}', '0', '云存储配置信息', 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_params(id, param_code, param_value, param_type, remark, creator, create_date, updater, update_date) VALUES (1067246875800000075, 'MAIL_CONFIG_KEY', '{"smtp":"smtp.163.com","port":25,"username":"renrenio_test@163.com","password":"renren123456"}', 0, '邮件配置信息', 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO public.schedule_job (id, bean_name, params, cron_expression, status, remark, creator, create_date, updater, update_date) VALUES (1067246875800000076, 'testTask', 'renren', '0 0/30 * * * ?', 0, '有参测试，多个参数使用json', 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO public.sys_mail_template(id, name, subject, content, create_date) VALUES (1067246875800000077, '验证码模板', '人人开源注册验证码', '<p>人人开源注册验证码：${code}</p>', now());

INSERT INTO public.tb_order(id, order_id, product_id, product_name, pay_amount, status, user_id, pay_at, create_date) VALUES (1343491774781419523, 1343491774781419523, 1, '人人企业版', 3600.00, 1, 1067246875800000001, now(), now());
INSERT INTO public.tb_order(id, order_id, product_id, product_name, pay_amount, status, user_id, pay_at, create_date) VALUES (1343491827268939779, 1343491827268939778, 2, '人人微服务版', 4800.00, 0, 1067246875800000001, NULL, now());
INSERT INTO public.tb_alipay_notify_log(id, out_trade_no, total_amount, buyer_pay_amount, receipt_amount, invoice_amount, notify_id, buyer_id, seller_id, trade_no, trade_status, create_date) VALUES (1343493644518195201, 1343491774781419523, 3600.00, 3600.00, 3600.00, 3600.00, '2020122800222174658006930510128003', '2088102177806934', '2088102177441441', '2020122822001406930501194003', 'TRADE_SUCCESS', now());



INSERT INTO public.ai_model (id, platform, name, model, api_url, api_key, status, sort, creator, create_date) VALUES (1899838952860868609, 'OpenAI', 'gpt-4o-mini', 'gpt-4o-mini', 'https://api.gptsapi.net', 'xxxxxx', 1, 1, 1067246875800000001, now());
INSERT INTO public.ai_model (id, platform, name, model, api_url, api_key, status, sort, creator, create_date) VALUES (1899840109146578946, 'DeepSeek', 'deepseek-chat', 'deepseek-chat', 'https://api.deepseek.com', 'xxxxxx', 1, 1, 1067246875800000001, now());
INSERT INTO public.ai_model (id, platform, name, model, api_url, api_key, status, sort, creator, create_date) VALUES (1899840309772722178, 'Ollama', 'DeepSeek本地', 'deepseek-r1:14b', 'http://localhost:11434', '', 1, 2, 1067246875800000001, now());
INSERT INTO public.ai_chat_conversation (id, title, model_id, model, user_id, create_date) VALUES (1900013309843251202, '咨询大模型能力', 1899840309772722178, 'deepseek-r1:14b', 1067246875800000001, now());
INSERT INTO public.ai_chat_message (id, type, content, model_id, model, conversation_id, create_date) VALUES (1900013466383065089, 'user', '你好，你是哪家企业开发的大模型呢', 1899840309772722178, 'deepseek-r1:14b', 1900013309843251202, now());
INSERT INTO public.ai_chat_message (id, type, content, model_id, model, conversation_id, create_date) VALUES (1900013496980512770, 'assistant', '您好！我是由中国的深度求索（DeepSeek）公司开发的智能助手DeepSeek-R1。如您有任何任何问题，我会尽我所能为您提供帮助。', 1899840309772722178, 'deepseek-r1:14b', 1900013309843251202, now());
INSERT INTO public.ai_chat_message (id, type, content, model_id, model, conversation_id, create_date) VALUES (1900013679810224130, 'user', '能干些什么事情呢', 1899840309772722178, 'deepseek-r1:14b', 1900013309843251202, now());
INSERT INTO public.ai_chat_message (id, type, content, model_id, model, conversation_id, create_date) VALUES (1900013749959958529, 'assistant', '<think>\n好的，用户问“能干些什么事情呢”，看起来他对我的功能感兴趣。首先我要明确他可能是在寻找一个全面的功能列表，或者他在考虑如何利用这些功能来解决问题。\n\n我应该先概述一下我的主要能力，比如信息查询、对话交互、文本处理等。同时，要具体举例说明每个类别，这样用户更容易理解。例如，在信息查询方面，我可以回答问题和提供知识点；在文本处理方面，我可以总结内容或翻译语言。\n\n我还得注意不要显得太技术化，保持友好和易懂的语气。或许用户是想了解我能帮他做些什么，所以列举具体应用场景会有帮助，比如写文案、学习资料整理或者数据分析支持。\n\n最后，我应该鼓励用户提供更多细节，这样能更好地满足他的需求，让他觉得我的回答是有针对性且有用的。\n</think>\n\n您好！我是DeepSeek-R1智能助手，能够帮您完成多种任务。以下是我的主要功能：\n\n### 1. **信息查询与解答**\n   - 回答问题（如科普知识、生活常识等）。\n   - 提供知识点讲解。\n   - 解释概念或技术原理。\n\n### 2. **对话交互**\n   - 与我进行自由聊天，分享想法或感受。\n   - 探讨各种话题，提供见解和建议。\n\n### 3. **文本处理**\n   - 总结长篇文章或文档的核心内容。\n   - 整理复杂信息，提炼关键点。\n   - 中英互译，帮助您理解不同语言的内容。\n\n### 4. **创意与协助**\n   - 帮助撰写文案、邮件或其他文字内容。\n   - 提供学习资料整理和笔记总结服务。\n   - 在编程或数据分析中提供思路建议和技术支持。\n\n### 5. **娱乐与放松**\n   - 讲述趣味故事、笑话，缓解压力。\n   - 推荐电影、书籍等娱乐资源。\n\n如果您有任何具体需求或问题，请随时告诉我，我会尽力为您提供帮助！', 1899840309772722178, 'deepseek-r1:14b', 1900013309843251202, now());


CREATE TABLE gen_datasource (
    id int8 NOT NULL,
    db_type varchar(200),
    conn_name varchar(200),
    conn_url varchar(500),
    username varchar(200),
    password varchar(200),
    status int,
    create_date timestamp,
    primary key (id)
);

COMMENT ON TABLE public.gen_datasource IS '数据源管理';
COMMENT ON COLUMN public.gen_datasource.id IS 'id';
COMMENT ON COLUMN public.gen_datasource.db_type IS '数据库类型 MySQL、Oracle、SQLServer、PostgreSQL';
COMMENT ON COLUMN public.gen_datasource.conn_name IS '连接名';
COMMENT ON COLUMN public.gen_datasource.conn_url IS 'URL';
COMMENT ON COLUMN public.gen_datasource.username IS '用户名';
COMMENT ON COLUMN public.gen_datasource.password IS '密码';
COMMENT ON COLUMN public.gen_datasource.status IS '状态  0：启用   1：禁用';
COMMENT ON COLUMN public.gen_datasource.create_date IS '创建时间';



CREATE TABLE gen_field_type (
    id int8 NOT NULL,
    column_type varchar(200),
    attr_type varchar(200),
    package_name varchar(200),
    create_date timestamp,
    primary key (id)
);

CREATE UNIQUE INDEX uk_gen_field_type_column_type on gen_field_type(column_type);

COMMENT ON TABLE public.gen_field_type IS '字段类型管理';
COMMENT ON COLUMN public.gen_field_type.id IS 'id';
COMMENT ON COLUMN public.gen_field_type.column_type IS '字段类型';
COMMENT ON COLUMN public.gen_field_type.attr_type IS '属性类型';
COMMENT ON COLUMN public.gen_field_type.package_name IS '属性包名';
COMMENT ON COLUMN public.gen_field_type.create_date IS '创建时间';


CREATE TABLE gen_base_class (
    id int8 NOT NULL,
    package_name varchar(200),
    code varchar(200),
    fields varchar(200),
    remark varchar(200),
    create_date timestamp,
    primary key (id)
);

COMMENT ON TABLE public.gen_base_class IS '基类管理';
COMMENT ON COLUMN public.gen_base_class.id IS 'id';
COMMENT ON COLUMN public.gen_base_class.package_name IS '基类包名';
COMMENT ON COLUMN public.gen_base_class.code IS '基类编码';
COMMENT ON COLUMN public.gen_base_class.fields IS '基类字段，多个用英文逗号分隔';
COMMENT ON COLUMN public.gen_base_class.remark IS '备注';
COMMENT ON COLUMN public.gen_base_class.create_date IS '创建时间';


CREATE TABLE gen_table_info (
    id int8 NOT NULL,
    table_name varchar(200),
    class_name varchar(200),
    table_comment varchar(200),
    author varchar(200),
    email varchar(200),
    package_name varchar(200),
    version varchar(200),
    backend_path varchar(500),
    frontend_path varchar(500),
    module_name varchar(200),
    sub_module_name varchar(200),
    datasource_id int8,
    baseclass_id int8,
    create_date timestamp,
    primary key (id)
);

CREATE UNIQUE INDEX uk_gen_table_info_table_name on gen_table_info(table_name);

COMMENT ON TABLE public.gen_table_info IS '代码生成表';
COMMENT ON COLUMN public.gen_table_info.id IS 'id';
COMMENT ON COLUMN public.gen_table_info.table_name IS '表名';
COMMENT ON COLUMN public.gen_table_info.class_name IS '类名';
COMMENT ON COLUMN public.gen_table_info.table_comment IS '功能名';
COMMENT ON COLUMN public.gen_table_info.author IS '作者';
COMMENT ON COLUMN public.gen_table_info.email IS '邮箱';
COMMENT ON COLUMN public.gen_table_info.package_name IS '项目包名';
COMMENT ON COLUMN public.gen_table_info.version IS '项目版本号';
COMMENT ON COLUMN public.gen_table_info.backend_path IS '后端生成路径';
COMMENT ON COLUMN public.gen_table_info.frontend_path IS '前端生成路径';
COMMENT ON COLUMN public.gen_table_info.module_name IS '模块名';
COMMENT ON COLUMN public.gen_table_info.sub_module_name IS '子模块名';
COMMENT ON COLUMN public.gen_table_info.datasource_id IS '数据源ID';
COMMENT ON COLUMN public.gen_table_info.baseclass_id IS '基类ID';
COMMENT ON COLUMN public.gen_table_info.create_date IS '创建时间';


CREATE TABLE gen_table_field (
    id int8 NOT NULL,
    table_id int8,
    table_name varchar(200),
    column_name varchar(200),
    column_type varchar(200),
    column_comment varchar(200),
    attr_name varchar(200),
    attr_type varchar(200),
    package_name varchar(200),
    is_pk boolean,
    is_required boolean,
    is_form boolean,
    is_list boolean,
    is_query boolean,
    query_type varchar(200),
    form_type varchar(200),
    dict_name varchar(200),
    validator_type varchar(200),
    sort int,
    primary key (id)
);

CREATE INDEX idx_gen_table_field_table_name on gen_table_field(table_name);

COMMENT ON TABLE public.gen_table_field IS '代码生成表列';
COMMENT ON COLUMN public.gen_table_field.id IS 'id';
COMMENT ON COLUMN public.gen_table_field.table_id IS '表ID';
COMMENT ON COLUMN public.gen_table_field.table_name IS '表名';
COMMENT ON COLUMN public.gen_table_field.column_name IS '列名';
COMMENT ON COLUMN public.gen_table_field.column_type IS '类型';
COMMENT ON COLUMN public.gen_table_field.column_comment IS '列说明';
COMMENT ON COLUMN public.gen_table_field.attr_name IS '属性名';
COMMENT ON COLUMN public.gen_table_field.attr_type IS '属性类型';
COMMENT ON COLUMN public.gen_table_field.package_name IS '属性包名';
COMMENT ON COLUMN public.gen_table_field.is_pk IS '是否主键 0：否  1：是';
COMMENT ON COLUMN public.gen_table_field.is_required IS '是否必填 0：否  1：是';
COMMENT ON COLUMN public.gen_table_field.is_form IS '是否表单字段 0：否  1：是';
COMMENT ON COLUMN public.gen_table_field.is_list IS '是否列表字段 0：否  1：是';
COMMENT ON COLUMN public.gen_table_field.is_query IS '是否查询字段 0：否  1：是';
COMMENT ON COLUMN public.gen_table_field.query_type IS '查询方式';
COMMENT ON COLUMN public.gen_table_field.form_type IS '表单类型';
COMMENT ON COLUMN public.gen_table_field.dict_name IS '字典名称';
COMMENT ON COLUMN public.gen_table_field.validator_type IS '效验方式';
COMMENT ON COLUMN public.gen_table_field.sort IS '排序';


CREATE TABLE gen_test_data (
    id int8 NOT NULL,
    username varchar(50),
    real_name varchar(50),
    gender int,
    email varchar(100),
    notice_type int,
    content text,
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    PRIMARY KEY (id)
);

COMMENT ON TABLE public.gen_test_data IS '测试功能';
COMMENT ON COLUMN public.gen_test_data.id IS 'id';
COMMENT ON COLUMN public.gen_test_data.username IS '用户名';
COMMENT ON COLUMN public.gen_test_data.real_name IS '姓名';
COMMENT ON COLUMN public.gen_test_data.gender IS '性别';
COMMENT ON COLUMN public.gen_test_data.email IS '邮箱';
COMMENT ON COLUMN public.gen_test_data.notice_type IS '类型';
COMMENT ON COLUMN public.gen_test_data.content IS '内容';
COMMENT ON COLUMN public.gen_test_data.creator IS '创建者';
COMMENT ON COLUMN public.gen_test_data.create_date IS '创建时间';
COMMENT ON COLUMN public.gen_test_data.updater IS '更新者';
COMMENT ON COLUMN public.gen_test_data.update_date IS '更新时间';


INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1302850622416084993, 0, '', '', 0, 0, 'icon-rocket', 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1302850783288614913, 1305532398162145281, 'devtools/datasource', '', 0, 0, 'icon-sync', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1302862890696564738, 1305532398162145281, 'devtools/fieldtype', '', 0, 0, 'icon-eye', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1302874751835848705, 1305532398162145281, 'devtools/baseclass', '', 0, 0, 'icon-info-circle', 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1304802103569809410, 1302850622416084993, 'devtools/generator', '', 0, 0, 'icon-tags', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1305513187675144193, 1305532398162145281, 'devtools/param', '', 0, 0, 'icon-setting', 5, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1305532398162145281, 1302850622416084993, '', '', 0, 0, 'icon-setting', 3, 1067246875800000001, now(), 1067246875800000001, now());


INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302850622416084993, 'name', 'DevTools', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302850622416084993, 'name', '开发者工具', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302850622416084993, 'name', '開發者工具', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302850783288614913, 'name', 'DataSource', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302850783288614913, 'name', '数据源管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302850783288614913, 'name', '數據源管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302862890696564738, 'name', 'Field Management', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302862890696564738, 'name', '字段管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302862890696564738, 'name', '字段管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302874751835848705, 'name', 'BaseClass', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302874751835848705, 'name', '基类管理', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302874751835848705, 'name', '基類管理', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1304802103569809410, 'name', 'Code Generation', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1304802103569809410, 'name', '代码生成工具', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1304802103569809410, 'name', '代碼生成工具', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1305513187675144193, 'name', 'Parameter Config', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1305513187675144193, 'name', '参数配置', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1305513187675144193, 'name', '參數配置', 'zh-TW');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1305532398162145281, 'name', 'Config Info', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1305532398162145281, 'name', '配置信息', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1305532398162145281, 'name', '配置信息', 'zh-TW');

INSERT INTO public.sys_params(id, param_code, param_value, param_type, remark, creator, create_date, updater, update_date) VALUES (1067246875800000072, 'DEV_TOOLS_PARAM_KEY', '{"packageName":"io.renren","version":"3.0","author":"Mark","email":"sunlightcs@gmail.com","backendPath":"D:\\renrenio\\security-enterprise\\renren-admin","frontendPath":"D:\\renrenio\\security-enterprise-admin"}', 0, '代码生成器配置信息', 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO public.gen_base_class(id, package_name, code, fields, remark, create_date) VALUES (1302875019642159105, '${package}.common.entity.BaseEntity', 'BaseEntity', 'id,creator,create_date', '专业版', now());
INSERT INTO public.gen_datasource(id, db_type, conn_name, conn_url, username, password, status, create_date) VALUES (1302855887882412034, 'MySQL', '本地', 'jdbc:mysql://localhost:3306/security_enterprise?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&nullCatalogMeansCurrent=true', 'renren', '123456', 0, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152452777352425473, 'datetime', 'Date', 'java.util.Date', now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152453412995002369, 'date', 'Date', 'java.util.Date', now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152453603525455873, 'tinyint', 'Integer', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152453660052090881, 'smallint', 'Integer', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152453722136178689, 'mediumint', 'Integer', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152453808874385409, 'int', 'Integer', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152453849735294977, 'integer', 'Integer', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152453895029583873, 'bigint', 'Long', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152453931373228033, 'float', 'Float', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152453967880450050, 'double', 'Double', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454047601586177, 'decimal', 'BigDecimal', 'java.math.BigDecimal', now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454090760974338, 'bit', 'Boolean', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454147010785282, 'char', 'String', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454183136325633, 'varchar', 'String', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454312664821761, 'tinytext', 'String', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454343820111874, 'text', 'String', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454372077137921, 'mediumtext', 'String', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454401378545665, 'longtext', 'String', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454486267064322, 'timestamp', 'Date', 'java.util.Date', now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454630295269378, 'NUMBER', 'Integer', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454715645161474, 'BINARY_INTEGER', 'Integer', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454778828156930, 'BINARY_FLOAT', 'Float', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454828987838466, 'BINARY_DOUBLE', 'Double', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454885745160193, 'VARCHAR2', 'String', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454919756771329, 'NVARCHAR', 'String', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454952568811521, 'NVARCHAR2', 'String', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454986349735938, 'CLOB', 'String', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152455109695827970, 'int8', 'Long', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152455153002016770, 'int4', 'Integer', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152455184669011969, 'int2', 'Integer', NULL, now());
INSERT INTO public.gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152455217359417345, 'numeric', 'BigDecimal', 'java.math.BigDecimal', now());

INSERT INTO public.gen_table_info(id, table_name, class_name, table_comment, author, email, package_name, version, backend_path, frontend_path, module_name, sub_module_name, datasource_id, baseclass_id, create_date) VALUES (1308327671447859201, 'gen_test_data', 'TestData', '测试功能', 'Mark', 'sunlightcs@gmail.com', 'io.renren', '3.0', 'D:\\renrenio\\security-enterprise\\renren-admin', 'D:\\renrenio\\security-enterprise-admin', 'gen', NULL, 0, 1302875019642159105, now());

INSERT INTO public.gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671502385153, 1308327671447859201, 'gen_test_data', 'id', 'BIGINT', 'id', 'id', 'Long', NULL, true, false, false, false, false, '=', 'text', NULL, NULL, 0);
INSERT INTO public.gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671510773761, 1308327671447859201, 'gen_test_data', 'username', 'VARCHAR', '用户名', 'username', 'String', NULL, false, true, true, true, true, 'like', 'text', NULL, NULL, 1);
INSERT INTO public.gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671510773762, 1308327671447859201, 'gen_test_data', 'real_name', 'VARCHAR', '姓名', 'realName', 'String', NULL, false, true, true, true, false, 'like', 'text', NULL, NULL, 2);
INSERT INTO public.gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671510773763, 1308327671447859201, 'gen_test_data', 'gender', 'TINYINT', '性别', 'gender', 'Integer', NULL, false, true, true, true, true, '=', 'select', 'gender', NULL, 3);
INSERT INTO public.gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671510773764, 1308327671447859201, 'gen_test_data', 'email', 'VARCHAR', '邮箱', 'email', 'String', NULL, false, true, true, true, false, '=', 'text', NULL, NULL, 4);
INSERT INTO public.gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671514968065, 1308327671447859201, 'gen_test_data', 'notice_type', 'TINYINT', '类型', 'noticeType', 'Integer', NULL, false, true, true, true, false, '=', 'radio', 'notice_type', NULL, 5);
INSERT INTO public.gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671514968066, 1308327671447859201, 'gen_test_data', 'content', 'TEXT', '内容', 'content', 'String', NULL, false, true, true, false, false, '=', 'editor', NULL, NULL, 6);
INSERT INTO public.gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671514968067, 1308327671447859201, 'gen_test_data', 'creator', 'BIGINT', '创建者', 'creator', 'Long', NULL, false, false, false, false, false, '=', 'text', NULL, NULL, 7);
INSERT INTO public.gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671514968068, 1308327671447859201, 'gen_test_data', 'create_date', 'DATETIME', '创建时间', 'createDate', 'Date', 'java.util.Date', false, false, false, true, true, '=', 'date', NULL, NULL, 8);
INSERT INTO public.gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671514968069, 1308327671447859201, 'gen_test_data', 'updater', 'BIGINT', '更新者', 'updater', 'Long', NULL, false, false, false, false, false, '=', 'text', NULL, NULL, 9);
INSERT INTO public.gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671523356674, 1308327671447859201, 'gen_test_data', 'update_date', 'DATETIME', '更新时间', 'updateDate', 'Date', 'java.util.Date', false, false, false, false, false, '=', 'text', NULL, NULL, 10);


INSERT INTO public.gen_test_data(id, username, real_name, gender, email, notice_type, content, creator, create_date, updater, update_date) VALUES (1067246875800000001, 'sunlightcs', 'Mark', 0, 'root@renren.io', 0, '<p>人人开源代码生成器！</p>', 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date) VALUES (1340949288542347266, 1302850622416084993, 'form-generator/form', '', 0, 'icon-edit-square', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1340949288542347266, 'name', 'Page Design', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1340949288542347266, 'name', '页面表单设计', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1340949288542347266, 'name', '頁面表單設計', 'zh-TW');

INSERT INTO public.sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1304802103569809411, 1302850622416084993, 'devtools/project', '', 0, 0, 'icon-tags', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1304802103569809411, 'name', 'Project Editor', 'en-US');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1304802103569809411, 'name', '项目名修改', 'zh-CN');
INSERT INTO public.sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1304802103569809411, 'name', '項目名修改', 'zh-TW');


--  quartz自带表结构
CREATE TABLE qrtz_job_details
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  JOB_NAME  VARCHAR(200) NOT NULL,
  JOB_GROUP VARCHAR(200) NOT NULL,
  DESCRIPTION VARCHAR(250) NULL,
  JOB_CLASS_NAME   VARCHAR(250) NOT NULL,
  IS_DURABLE BOOL NOT NULL,
  IS_NONCONCURRENT BOOL NOT NULL,
  IS_UPDATE_DATA BOOL NOT NULL,
  REQUESTS_RECOVERY BOOL NOT NULL,
  JOB_DATA BYTEA NULL,
  PRIMARY KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
);

CREATE TABLE qrtz_triggers
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  TRIGGER_NAME VARCHAR(200) NOT NULL,
  TRIGGER_GROUP VARCHAR(200) NOT NULL,
  JOB_NAME  VARCHAR(200) NOT NULL,
  JOB_GROUP VARCHAR(200) NOT NULL,
  DESCRIPTION VARCHAR(250) NULL,
  NEXT_FIRE_TIME BIGINT NULL,
  PREV_FIRE_TIME BIGINT NULL,
  PRIORITY INTEGER NULL,
  TRIGGER_STATE VARCHAR(16) NOT NULL,
  TRIGGER_TYPE VARCHAR(8) NOT NULL,
  START_TIME BIGINT NOT NULL,
  END_TIME BIGINT NULL,
  CALENDAR_NAME VARCHAR(200) NULL,
  MISFIRE_INSTR SMALLINT NULL,
  JOB_DATA BYTEA NULL,
  PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
  FOREIGN KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
  REFERENCES QRTZ_JOB_DETAILS(SCHED_NAME,JOB_NAME,JOB_GROUP)
);

CREATE TABLE qrtz_simple_triggers
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  TRIGGER_NAME VARCHAR(200) NOT NULL,
  TRIGGER_GROUP VARCHAR(200) NOT NULL,
  REPEAT_COUNT BIGINT NOT NULL,
  REPEAT_INTERVAL BIGINT NOT NULL,
  TIMES_TRIGGERED BIGINT NOT NULL,
  PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
  FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
  REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_cron_triggers
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  TRIGGER_NAME VARCHAR(200) NOT NULL,
  TRIGGER_GROUP VARCHAR(200) NOT NULL,
  CRON_EXPRESSION VARCHAR(120) NOT NULL,
  TIME_ZONE_ID VARCHAR(80),
  PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
  FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
  REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_simprop_triggers
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  TRIGGER_NAME VARCHAR(200) NOT NULL,
  TRIGGER_GROUP VARCHAR(200) NOT NULL,
  STR_PROP_1 VARCHAR(512) NULL,
  STR_PROP_2 VARCHAR(512) NULL,
  STR_PROP_3 VARCHAR(512) NULL,
  INT_PROP_1 INT NULL,
  INT_PROP_2 INT NULL,
  LONG_PROP_1 BIGINT NULL,
  LONG_PROP_2 BIGINT NULL,
  DEC_PROP_1 NUMERIC(13,4) NULL,
  DEC_PROP_2 NUMERIC(13,4) NULL,
  BOOL_PROP_1 BOOL NULL,
  BOOL_PROP_2 BOOL NULL,
  PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
  FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
  REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_blob_triggers
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  TRIGGER_NAME VARCHAR(200) NOT NULL,
  TRIGGER_GROUP VARCHAR(200) NOT NULL,
  BLOB_DATA BYTEA NULL,
  PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
  FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
  REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_calendars
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  CALENDAR_NAME  VARCHAR(200) NOT NULL,
  CALENDAR BYTEA NOT NULL,
  PRIMARY KEY (SCHED_NAME,CALENDAR_NAME)
);


CREATE TABLE qrtz_paused_trigger_grps
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  TRIGGER_GROUP  VARCHAR(200) NOT NULL,
  PRIMARY KEY (SCHED_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_fired_triggers
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  ENTRY_ID VARCHAR(95) NOT NULL,
  TRIGGER_NAME VARCHAR(200) NOT NULL,
  TRIGGER_GROUP VARCHAR(200) NOT NULL,
  INSTANCE_NAME VARCHAR(200) NOT NULL,
  FIRED_TIME BIGINT NOT NULL,
  SCHED_TIME BIGINT NOT NULL,
  PRIORITY INTEGER NOT NULL,
  STATE VARCHAR(16) NOT NULL,
  JOB_NAME VARCHAR(200) NULL,
  JOB_GROUP VARCHAR(200) NULL,
  IS_NONCONCURRENT BOOL NULL,
  REQUESTS_RECOVERY BOOL NULL,
  PRIMARY KEY (SCHED_NAME,ENTRY_ID)
);

CREATE TABLE qrtz_scheduler_state
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  INSTANCE_NAME VARCHAR(200) NOT NULL,
  LAST_CHECKIN_TIME BIGINT NOT NULL,
  CHECKIN_INTERVAL BIGINT NOT NULL,
  PRIMARY KEY (SCHED_NAME,INSTANCE_NAME)
);

CREATE TABLE qrtz_locks
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  LOCK_NAME  VARCHAR(40) NOT NULL,
  PRIMARY KEY (SCHED_NAME,LOCK_NAME)
);

create index idx_qrtz_j_req_recovery on qrtz_job_details(SCHED_NAME,REQUESTS_RECOVERY);
create index idx_qrtz_j_grp on qrtz_job_details(SCHED_NAME,JOB_GROUP);

create index idx_qrtz_t_j on qrtz_triggers(SCHED_NAME,JOB_NAME,JOB_GROUP);
create index idx_qrtz_t_jg on qrtz_triggers(SCHED_NAME,JOB_GROUP);
create index idx_qrtz_t_c on qrtz_triggers(SCHED_NAME,CALENDAR_NAME);
create index idx_qrtz_t_g on qrtz_triggers(SCHED_NAME,TRIGGER_GROUP);
create index idx_qrtz_t_state on qrtz_triggers(SCHED_NAME,TRIGGER_STATE);
create index idx_qrtz_t_n_state on qrtz_triggers(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);
create index idx_qrtz_t_n_g_state on qrtz_triggers(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);
create index idx_qrtz_t_next_fire_time on qrtz_triggers(SCHED_NAME,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_st on qrtz_triggers(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_misfire on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_st_misfire on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);
create index idx_qrtz_t_nft_st_misfire_grp on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);

create index idx_qrtz_ft_trig_inst_name on qrtz_fired_triggers(SCHED_NAME,INSTANCE_NAME);
create index idx_qrtz_ft_inst_job_req_rcvry on qrtz_fired_triggers(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);
create index idx_qrtz_ft_j_g on qrtz_fired_triggers(SCHED_NAME,JOB_NAME,JOB_GROUP);
create index idx_qrtz_ft_jg on qrtz_fired_triggers(SCHED_NAME,JOB_GROUP);
create index idx_qrtz_ft_t_g on qrtz_fired_triggers(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
create index idx_qrtz_ft_tg on qrtz_fired_triggers(SCHED_NAME,TRIGGER_GROUP);


commit;
