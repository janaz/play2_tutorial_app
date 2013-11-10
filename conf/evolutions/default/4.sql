# --- !Ups

alter table csv_file add column uploaded_at bigint(20);


# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

alter table csv_file drop column uploaded_at;

SET FOREIGN_KEY_CHECKS=1;

