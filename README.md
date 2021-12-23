carldav 
=======

A lightweight CalDAV and CardDAV server for personal use powered by [Spring Boot](http://projects.spring.io/spring-boot/).

Supported clients
-----------------

So far caldav has been tested with the following clients:
- Mozilla Thunderbird with [TbSync](https://github.com/jobisoft/TbSync)
- Evolution (on Fedora 23)
- [DAVdroid](https://play.google.com/store/apps/details?id=at.bitfire.davdroid) and [OpenTasks](https://play.google.com/store/apps/details?id=org.dmfs.tasks)
- iOS iCalendar (version 4)

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

- Mozilla Thunderbird with TbSync: set CalDAV and CardDAV server address to `http://localhost:1984/carldav/dav/you@localhost/calendar`. Contact sync will be configured automatically.
- Evolution: set CalDAV server address to `http://localhost:1984/carldav/dav/you@localhost/calendar` and CardDAV server address to `http://localhost:1984/carldav/dav/you@localhost/contacts`. In addition, you can use `VJOURNAL` calendar entries (Evolution Memo) as defined in [RFC 4791](https://tools.ietf.org/html/rfc4791).
- Android: Set CalDAV and CardDAV server address to `http://localhost:1984/carldav/dav/you@localhost/calendar`. Contact sync will be configured automatically.
- iOS: ehm, you know it.

Constrains
----------

- caldav doesn't support additional CalDAV resources yet
- caldav doesn't support additional CardDAV resources yet
- caldav doesn't fully comply with various RFC's regarding CalDAV/CardDAV                                       
- caldav doesn't support calendar sharing yet


Help needed
-----------

- Testing on different clients and platforms, especially iOS
