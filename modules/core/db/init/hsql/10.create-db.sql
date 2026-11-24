-- begin PSWDPLUS_PASSWORD_HISTORY
create table PSWDPLUS_PASSWORD_HISTORY (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    USER_ID varchar(36),
    CREATED_AT timestamp,
    PASSWORD_HASH varchar(255),
    --
    primary key (ID)
)^
-- end PSWDPLUS_PASSWORD_HISTORY
