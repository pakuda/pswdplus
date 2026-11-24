[ ![Download](https://api.bintray.com/packages/arapoport/main/cuba-pswdplus/images/download.svg?version=1.0.3) ](https://bintray.com/arapoport/main/cuba-pswdplus/_latestVersion)
[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)


## What is this component for ? 
**cuba-pswdplus** is a CUBA Platform component that provides "out of the box" password history validation to an existing application. It allows the app administrator to activate and configure the validation.  

A coming version will add support for password expiration and thus, it will allow a CUBA application to be deployed in corporate environments with strict password requirements.


## Installation
1. `cuba-pswdplus` is available in the [CUBA marketplace](https://www.cuba-platform.com/marketplace)
2. The component was tested only in platform version 6.10.x

The latest version is:[ ![Download](https://api.bintray.com/packages/arapoport/main/cuba-pswdplus/images/download.svg?version=1.0.3) ](https://bintray.com/arapoport/main/cuba-pswdplus/_latestVersion)

To install the component, you have to add the repository and the component in CUBA Studio or in a `build.gradle` file. The complete add-ons installation guide is explained in [CUBA Platform documentation](https://doc.cuba-platform.com/manual-latest/app_components_usage.html)

Add a custom application component to your project:

* Artifact group: `ar.com.osmosys.pswdplus`
* Artifact name: `pswdplus-global`
* Version: *add-on version*

## Configuration

Once you've installed the component and run your app for the first time, open the Application Properties screen in the Administrator menu. You will see a new entry named **pswdplus**:

![Screenshot Application Properties](https://github.com/pakuda/pswdplus/blob/master/img/appProperties.png)

There are two properties to configure:

* *usePswdHistory*: boolean value, `true` if you want to enable password history. Setting it to `false` will not delete already stored passwords.

* *pswdHistoryLength*: number of used passwords that will be stored in the database for a each user. For instance, if `pswdHistoryLength=10` the component will validate a new password against the last 10 used passwords for that user. The password log follows a FIFO rule: once a new password is stored in the log, every password beyond the last `pswdHistoryLength` ones is hard deleted. If the value is decreased, the new length is applied to the validation right away, and the exceeding passwords of each user are deleted the next time that user changes the password.

## In Action
The validation is performed when the user changes his password himself or it is changed by the admin. If the entered new password is already in the password history log, then an alert is shown on the screen.

![Screenshot Alert](https://github.com/pakuda/pswdplus/blob/master/img/alertMessage.png)

## Localization
At this version, only English and Spanish messages are supported.

## Database support
At this version, only MySQL database is supported out of the box. Scripts for any other database engine should be created by the user.

## Log table
The last n passwords are stored in a table named `PSWDPLUS_PASSWORD_HISTORY`. As said before, there is no need to maintain the table, since it follows a FIFO rule. Whenever a new record for a user is created, the component deletes every record of that user beyond the last n ones.








