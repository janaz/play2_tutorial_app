# --- !Ups

alter table csv_file add column state varchar(50);

create index ix_csv_file_state on csv_file (state);


# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

alter table csv_file drop column state;

SET FOREIGN_KEY_CHECKS=1;

