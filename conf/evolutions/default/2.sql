create table csv_file (
  id                        bigint not null,
  file_name                 varchar(255),
  owner_id                  bigint,
  constraint pk_csv_file primary key (id))
;

create sequence csv_file_seq;

drop table if exists csv_file;

drop sequence if exists csv_file_seq;

