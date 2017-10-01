-- Oracle
-- Create table
create table LITTLE_ARTISAN
(
  artisan_id   VARCHAR2(64),
  artisan_name VARCHAR2(20),
  artisan_desc VARCHAR2(256)
)
tablespace TAB_CC
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );