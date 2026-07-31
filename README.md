[ ![Download](https://api.bintray.com/packages/arapoport/main/cuba-pswdplus/images/download.svg?version=1.0.3) ](https://bintray.com/arapoport/main/cuba-pswdplus/_latestVersion)
[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)


## What is this component for ? 
**cuba-pswdplus** is a CUBA Platform component that provides "out of the box" password history validation and password expiration to an existing application. It allows the app administrator to activate and configure both features, so a CUBA application can be deployed in corporate environments with strict password requirements.

Both features are built on top of the same password log table: the history validation compares new passwords against the stored hashes, and the expiration process uses the date of the most recent log record as the "last password change" date. **This means password expiration requires password history to be enabled** — see [Password expiration](#password-expiration) below.


## Installation
1. `cuba-pswdplus` is available in the [CUBA marketplace](https://www.cuba-platform.com/marketplace)
2. This version was built and tested against platform version 7.2.x

The latest version is:[ ![Download](https://api.bintray.com/packages/arapoport/main/cuba-pswdplus/images/download.svg?version=1.0.3) ](https://bintray.com/arapoport/main/cuba-pswdplus/_latestVersion)

To install the component, you have to add the repository and the component in CUBA Studio or in a `build.gradle` file. The complete add-ons installation guide is explained in [CUBA Platform documentation](https://doc.cuba-platform.com/manual-latest/app_components_usage.html)

Add a custom application component to your project:

* Artifact group: `ar.com.osmosys.pswdplus`
* Artifact name: `pswdplus-global`
* Version: *add-on version*

## Configuration

Once you've installed the component and run your app for the first time, open the Application Properties screen in the Administrator menu. You will see a new entry named **pswdplus**:

![Screenshot Application Properties](https://github.com/pakuda/pswdplus/blob/master/img/appProperties.png)

There are four properties to configure:

* *usePswdHistory*: boolean value, `true` if you want to enable password history. Setting it to `false` will not delete already stored passwords, but no new password will be logged from that moment on. Since the password log is also the source of the "last password change" date, this property must be `true` for password expiration to work.

* *pswdHistoryLength*: size of the password log kept in the database for each user. The log follows a FIFO rule: once a new password is stored, the oldest record is hard deleted. Note that the new record is counted before the pruning takes place, so the log stabilizes at `pswdHistoryLength - 1` records; with `pswdHistoryLength=10`, a new password is validated against the last 9 used passwords for that user.

* *usePswdExpiration*: boolean value, `true` if you want to enable password expiration. It has no effect unless *usePswdHistory* is also `true`.

* *daysToPswdExpiration*: password validity, expressed in days. See [Password expiration](#password-expiration) for the exact meaning.

## Password expiration

Expiration is not evaluated at login time. It is performed by the `pswdplus_ExpireService` service, which the application administrator must invoke from a CUBA scheduled task:

* Bean name: `pswdplus_ExpireService`
* Method name: `expireOldPasswords`
* Recommended period: once a day, outside business hours

On each run, the service goes through all the users that are **not** already flagged with *Change password at next logon*, reads the date of their most recent password log record, and — if the password is expired — sets the *Change password at next logon* flag. The user keeps working with the current password until their next login, at which point CUBA forces the password change. The service returns the comma-separated list of the logins it has flagged.

### How the validity period is counted

The service compares the run timestamp against the last password change using **whole elapsed days**, and flags the password when the elapsed days are **greater than** `daysToPswdExpiration`. In practice a password becomes expired once `daysToPswdExpiration + 1` full days (24 h periods) have elapsed since it was set.

For example, with `daysToPswdExpiration=1` and the scheduled task running on 31/07 at 12:00:

| Last password change | Elapsed | Flagged on this run ? |
|---|---|---|
| 30/07 11:00 | 25 h | no |
| 29/07 13:00 | 47 h | no |
| 29/07 12:00 | 48 h | yes |
| 28/07 or earlier | more than 48 h | yes |

Because the flag is only applied when the scheduled task runs, the effective moment of expiration also depends on how often the task is scheduled. With a daily task, a password is forced to be changed at the first run that takes place after `daysToPswdExpiration + 1` full days.

### Users without a password log record

A user with no record in the password log has no "last password change" date, and is therefore never flagged as expired. This is the case for users created before the component was installed, and for users whose password was only changed while *usePswdHistory* was `false`.

When enabling expiration on an existing application, insert one record per user in `PSWDPLUS_PASSWORD_HISTORY` so that the validity period starts at the same date for everybody, instead of forcing a password change on the first run of the scheduled task. The date read by the service is the `CREATED_AT` column (not `CREATE_TS`), and storing the current password hash adds no restriction, since the current password is already rejected by the change password dialog. For MySQL:

```sql
insert into PSWDPLUS_PASSWORD_HISTORY
    (ID, VERSION, CREATE_TS, CREATED_BY, USER_ID, CREATED_AT, PASSWORD_HASH)
select replace(uuid(), '-', ''), 1, now(), 'admin', u.ID, now(), u.PASSWORD
from SEC_USER u
where u.DELETE_TS is null
  and not exists (select 1 from PSWDPLUS_PASSWORD_HISTORY ph
                  where ph.USER_ID = u.ID and ph.DELETE_TS is null);
```

## In Action
The validation is performed when the user changes his password himself or it is changed by the admin. If the entered new password is already in the password history log, then an alert is shown on the screen.

![Screenshot Alert](https://github.com/pakuda/pswdplus/blob/master/img/alertMessage.png)

## Localization
At this version, only English and Spanish messages are supported.

## Database support
At this version, MySQL and MS SQL Server are supported out of the box. Scripts for any other database engine should be created by the user.

## Log table
The last n passwords are stored in a table named `PSWDPLUS_PASSWORD_HISTORY`. As said before, there is no need to maintain the table, since it follows a FIFO rule. Once a new record for a user is created, the component deletes the oldest one.

Each record keeps the password that has just been replaced, in `PASSWORD_HASH`, and the date of the change, in `CREATED_AT`. Records are only created while *usePswdHistory* is `true`, and the most recent `CREATED_AT` of a user is what the expiration process reads as their last password change, so the table should not be truncated while password expiration is in use.








