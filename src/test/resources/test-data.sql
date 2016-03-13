INSERT INTO users(id,locked,email,password,role) VALUES (1,0,'root@localhost','098f6bcd4621d373cade4e832627b4f6','ROLE_ADMIN');
INSERT INTO users(id,locked,email,password,role) VALUES (2,0,'test01@localhost.de','098f6bcd4621d373cade4e832627b4f6','ROLE_USER');
INSERT INTO users(id,locked,email,password,role) VALUES (3,0,'test02@localhost.de','098f6bcd4621d373cade4e832627b4f6','ROLE_USER');

INSERT INTO collection (collectionid, itemtype, id, etag, modifydate, displayname, itemname, ownerid)
VALUES (null, 'homecollection', 1, 'ghFexXxxU+9KC/of1jmJ82wMFig=', to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), 'homeCollection', 'test01@localhost.de', 2);

INSERT INTO collection (collectionid, itemtype, id, etag, modifydate, displayname, itemname, ownerid)
VALUES (1, 'calendarcollection', 2, 'NVy57RJot0LhdYELkMDJ9gQZjOM=', to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), 'calendarDisplayName', 'calendar', 2);

INSERT INTO collection (collectionid, itemtype, id, etag, modifydate, displayname, itemname, ownerid)
VALUES (1, 'collection', 3, 'njy57RJot0LhdYELkMDJ9gQZiOM=', to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), 'contactDisplayName', 'contacts', 2);

INSERT INTO collection (collectionid, itemtype, id, etag, modifydate, displayname, itemname, ownerid)
VALUES (null, 'homecollection', 4, 'ghFexXxxU+9KC/of1jmJ82wMFig=', to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), 'homeCollection', 'test02@localhost.de', 3);

INSERT INTO collection (collectionid, itemtype, id, etag, modifydate, displayname, itemname, ownerid)
VALUES (4, 'calendarcollection', 5, 'NVy57RJot0LhdYELkMDJ9gQZjOM=', to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), 'calendarDisplayName', 'calendar', 3);

INSERT INTO collection (collectionid, itemtype, id, etag, modifydate, displayname, itemname, ownerid)
VALUES (5, 'calendarcollection', 6, 'NVy57RJot0LhdYELkMDJ9gQZjOM=', to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), 'subcalendarDisplayName', 'subcalendar', 3);