# --- !Ups

alter table csv_file add column metadata_id bigint;

create table csv_metadata (
  id                        bigint auto_increment not null,
  column_names              TEXT,
  constraint pk_csv_metadata primary key (id))
;

create index ix_csv_file_user_1 on csv_file (owner_id);

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table csv_metadata;

alter table csv_file drop column metadata_id;

SET FOREIGN_KEY_CHECKS=1;

