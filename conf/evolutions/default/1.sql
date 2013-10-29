# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table csv_file (
  id                        bigint not null,
  file_name                 varchar(255),
  owner_id                  bigint,
  constraint pk_csv_file primary key (id))
;

create table task (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_task primary key (id))
;

create sequence csv_file_seq;

create sequence task_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists csv_file;

drop table if exists task;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists csv_file_seq;

drop sequence if exists task_seq;

