-- begin PSWDPLUS_PASSWORD_HISTORY
create table PSWDPLUS_PASSWORD_HISTORY (
    ID varchar(32),
    VERSION integer not null,
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    --
    USER_ID varchar(32),
    CREATED_AT datetime(3),
    PASSWORD_HASH varchar(255),
    --
    primary key (ID)
)^
-- end PSWDPLUS_PASSWORD_HISTORY
