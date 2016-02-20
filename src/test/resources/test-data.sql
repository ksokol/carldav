INSERT INTO users(id,locked,email,password,role) VALUES (1,0,'root@localhost','098f6bcd4621d373cade4e832627b4f6','ROLE_ADMIN');
INSERT INTO users(id,locked,email,password,role) VALUES (2,0,'test01@localhost.de','098f6bcd4621d373cade4e832627b4f6','ROLE_USER');
INSERT INTO users(id,locked,email,password,role) VALUES (3,0,'test02@localhost.de','098f6bcd4621d373cade4e832627b4f6','ROLE_USER');

INSERT INTO item (collectionid, itemtype, id, etag, modifydate, displayname, itemname, uid, ownerid)
VALUES (null, 'homecollection', 1, 'ghFexXxxU+9KC/of1jmJ82wMFig=', to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), 'homeCollection', 'test01@localhost.de', 'de359448-1ee0-4151-872d-eea0ee462bc6', 2);

INSERT INTO item (collectionid, itemtype, id, etag, modifydate, displayname, itemname, uid, ownerid)
VALUES (1, 'calendarcollection', 2, 'NVy57RJot0LhdYELkMDJ9gQZjOM=', to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), 'calendarDisplayName', 'calendar', 'a172ed34-0106-4616-bb40-a416a8305465', 2);

INSERT INTO item (collectionid, itemtype, id, etag, modifydate, displayname, itemname, uid, ownerid)
VALUES (1, 'cardcollection', 3, 'njy57RJot0LhdYELkMDJ9gQZiOM=', to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), 'contactDisplayName', 'contacts', 'a112ed14-0106-4616-bb40-a416a8305465', 2);

INSERT INTO item (collectionid, itemtype, id, etag, modifydate, displayname, itemname, uid, ownerid)
VALUES (null, 'homecollection', 4, 'ghFexXxxU+9KC/of1jmJ82wMFig=', to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), 'homeCollection', 'test02@localhost.de', 'de359448-1ee0-4151-872d-eea0ee462bc6', 3);

INSERT INTO item (collectionid, itemtype, id, etag, modifydate, displayname, itemname, uid, ownerid)
VALUES (4, 'calendarcollection', 5, 'NVy57RJot0LhdYELkMDJ9gQZjOM=', to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), 'calendarDisplayName', 'calendar', 'a172ed34-0106-4616-bb40-a416a8305465', 3);

INSERT INTO item (collectionid, itemtype, id, etag, modifydate, displayname, itemname, uid, ownerid)
VALUES (5, 'calendarcollection', 6, 'NVy57RJot0LhdYELkMDJ9gQZjOM=', to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), 'calendarDisplayName', 'subcalendar', 'a172ed34-0106-4616-bb40-a416a8305465', 3);