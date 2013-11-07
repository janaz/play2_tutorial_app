# --- !Ups

create table csv_file (
  id                        bigint auto_increment not null,
  file_name                 varchar(255),
  owner_id                  bigint,
  constraint pk_csv_file primary key (id))
;



# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table csv_file;

SET FOREIGN_KEY_CHECKS=1;

