-- begin PSWDPLUS_PASSWORD_HISTORY
alter table PSWDPLUS_PASSWORD_HISTORY add constraint FK_PSWDPLUS_PASSWORD_HISTORY_ON_USER foreign key (USER_ID) references SEC_USER(ID)^
create index IDX_PSWDPLUS_PASSWORD_HISTORY_ON_USER on PSWDPLUS_PASSWORD_HISTORY (USER_ID)^
-- end PSWDPLUS_PASSWORD_HISTORY
