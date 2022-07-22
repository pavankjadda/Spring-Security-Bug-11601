-- Create tables
create table if not exists role
(
    id   bigint auto_increment
    primary key,
    name varchar(255) null
    );

create table if not exists user
(
    id                      bigint auto_increment
    primary key,
    account_non_expired     bit          null,
    account_non_locked      bit          null,
    credentials_non_expired bit          null,
    email                   varchar(255) null,
    first_name              varchar(255) null,
    last_name               varchar(255) null,
    password                varchar(255) null,
    username                varchar(255) null
    );

create table if not exists user_role
(
    user_id bigint not null,
    role_id bigint not null,
    constraint FKa68196081fvovjhkek5m97n3y
    foreign key (role_id) references role (id),
    constraint FKfgsgxvihks805qcq8sq26ab7c
    foreign key (user_id) references user (id)
    );

-- Insert Users and Roles
INSERT INTO spring_security_bug.user (id, account_non_expired, account_non_locked, credentials_non_expired, email, first_name, last_name, password, username)
VALUES (1, false, false, false, 'jdoe@exmaple.com', 'John', 'Doe', '$2a$12$tuyswnYZTno02b5g.dEU7exQNXq4BWL78fyZErKJILzQYIB2eG5vm', 'jdoe');
INSERT INTO spring_security_bug.role (id, name) VALUES (1, 'ROLE_READ_ONLY_USER');
INSERT INTO spring_security_bug.role (id, name) VALUES (2, 'ROL_SYS_ADMIN');
INSERT INTO spring_security_bug.role (id, name) VALUES (3, 'ROLE_API_USER');
INSERT INTO spring_security_bug.user_role (user_id, role_id) VALUES (1, 1);
