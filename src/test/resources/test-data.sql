INSERT INTO users(id,locked,email,password,role) VALUES (2,0,'test01@localhost.de','{MD5}098f6bcd4621d373cade4e832627b4f6','ROLE_USER');
INSERT INTO users(id,locked,email,password,role) VALUES (3,0,'test02@localhost.de','{MD5}098f6bcd4621d373cade4e832627b4f6','ROLE_USER');

INSERT INTO collection (collectionid, id, modifydate, displayname, calendar_color, itemname, ownerid)
VALUES (null, 1, to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), 'homeCollection', '#000000', 'test01@localhost.de', 2);

INSERT INTO collection (collectionid, id, modifydate, displayname, calendar_color, itemname, ownerid)
VALUES (1, 2, to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), 'calendarDisplayName', '#000000', 'calendar', 2);

INSERT INTO collection (collectionid, id, modifydate, displayname, calendar_color, itemname, ownerid)
VALUES (1, 3, to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), 'contactDisplayName', '#000000', 'contacts', 2);

INSERT INTO collection (collectionid, id, modifydate, displayname, calendar_color, itemname, ownerid)
VALUES (null, 4, to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), 'homeCollection', '#000000', 'test02@localhost.de', 3);

INSERT INTO collection (collectionid, id, modifydate, displayname, calendar_color, itemname, ownerid)
VALUES (4, 5, to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), 'calendarDisplayName', '#000000', 'calendar', 3);
