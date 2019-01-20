carldav [![Build Status](https://api.travis-ci.org/ksokol/carldav.png?branch=master)](https://travis-ci.org/ksokol/carldav/) [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=carldav%3Acarldav&metric=alert_status)](https://sonarcloud.io/dashboard/index/carldav:carldav)
=======

A lightweight caldav/carddav server for personal use powered by [Spring Boot](http://projects.spring.io/spring-boot/).

Supported clients
-----------------

So far caldav has been tested with the following clients:
- Mozilla Thunderbird (up to version 38.6.0) in conjunction with [Inverse SOGo Connector](http://www.sogo.nu/files/downloads/SOGo/Thunderbird/sogo-connector-31.0.2.xpi) (up to version 31.0.2)
- Evolution (on Fedora 23)
- [DAVdroid](https://play.google.com/store/apps/details?id=at.bitfire.davdroid) (up to version 1.0.8) in conjunction with [OpenTasks](https://play.google.com/store/apps/details?id=org.dmfs.tasks) (up to version 1.1.8.2)
- iOS iCalendar (up to version 4)

Installation
------------

**Dependencies**

- Java 8

**Build and package**

- run `./mvnw package` or `mvnw.cmd` on Windows
- you will find a fat jar (Spring Boot application) int the `target` folder
- run `java -jar target/carldav.jar`

Configuration
-------------

If not specified carldav will create a random admin password on every startup. Generated admin password can be found in the logs:
`19:08:41.678 [main] INFO  carldav.bootstrap.AdminUserCreator - admin user 'root@localhost:test'`

If you want to set your own persistent admin password create `config/application.properties` file in the same directory as `carldav.jar`.
Add `carldav.admin.password` with your desired password to `application.properties`, for example.: `carldav.admin.password=4b033fad-db09-4aa3-852b-87aa2b2598ea`

Admin user name is set to `root@localhost`. You can change it with the property `carldav.admin.name`.

Add a user
----------

In the current state of development caldav doesn't support a web ui. Therefore you'll need to issue a HTTP request by hand in order to create a user. For example:

`curl -i --user root@localhost:4b033fad-db09-4aa3-852b-87aa2b2598ea -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d '{"email": "you@localhost", "password": "password"}' http://localhost:1984/carldav/user`

Connect your client to carldav
------------------------------

- On Mozilla Thunderbird and [Inverse SOGo Connector](http://www.sogo.nu/files/downloads/SOGo/Thunderbird/sogo-connector-31.0.2.xpi) point Thunderbird for calendar and tasks to `http://localhost:1984/carldav/dav/you@localhost/calendar` and Inverse SOGo Connector for contacts to `http://localhost:1984/carldav/dav/you@localhost/contacts`.
- On Evolution point calendar, tasks and memo to `http://localhost:1984/carldav/dav/you@localhost/calendar` and contacts to `http://localhost:1984/carldav/dav/you@localhost/contacts`. In addition, you can use `VJOURNAL` calendar entries (Evolution Memo) as defined in [RFC 4791](https://tools.ietf.org/html/rfc4791).
- On Android first install [OpenTasks](https://play.google.com/store/apps/details?id=org.dmfs.tasks) amd then [DAVdroid](https://play.google.com/store/apps/details?id=at.bitfire.davdroid). Point DAVdroid for  calendar, tasks and contacts to `http://localhost:1984/carldav/dav/you@localhost/calendar`. Contact sync will be configured automatically.
- On iOS, ehm, you know it.

Constrains
----------

- caldav doesn't support additional calendar resoures yet
- caldav doesn't support additional addressbook resoures yet
- caldav doesn't fully comply with various RFC's regarding caldav/carddav                                       
- caldav doesn't support calendar sharing yet


Help needed
-----------

- Testing on different clients and platforms, especially iOS
