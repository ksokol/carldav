INSERT INTO users(id,locked,email,password,role) VALUES (1,0,'root@localhost','098f6bcd4621d373cade4e832627b4f6','ROLE_ADMIN');
INSERT INTO users(id,locked,email,password,role) VALUES (2,0,'test01@localhost.de','098f6bcd4621d373cade4e832627b4f6','ROLE_USER');
INSERT INTO users(id,locked,email,password,role) VALUES (3,0,'test02@localhost.de','098f6bcd4621d373cade4e832627b4f6','ROLE_USER');

INSERT INTO item (collectionid, itemtype, id, createdate, etag, modifydate, clientcreatedate, clientmodifieddate, displayname, itemname, uid, version,  icaluid, contentEncoding, contentLanguage, contentType, hasmodifications, ownerid, modifiesitemid)
VALUES (null, 'homecollection', 1, 1448140260056, 'ghFexXxxU+9KC/of1jmJ82wMFig=', 1448140260056, null, null, null, 'test01@localhost.de', 'de359448-1ee0-4151-872d-eea0ee462bc6', 0, null, null, null, null, null, 2, null);

INSERT INTO item (collectionid, itemtype, id, createdate, etag, modifydate, clientcreatedate, clientmodifieddate, displayname, itemname, uid, version, icaluid, contentEncoding, contentLanguage, contentType, hasmodifications, ownerid, modifiesitemid)
VALUES (1, 'collection', 2, 1448140260072, 'NVy57RJot0LhdYELkMDJ9gQZjOM=', 1448140260072, null, null, 'calendarDisplayName', 'calendar', 'a172ed34-0106-4616-bb40-a416a8305465', 0, null, null, null, null, null, 2, null);

INSERT INTO item (collectionid, itemtype, id, createdate, etag, modifydate, clientcreatedate, clientmodifieddate, displayname, itemname, uid, version, icaluid, contentEncoding, contentLanguage, contentType, hasmodifications, ownerid, modifiesitemid)
VALUES (1, 'collection', 3, 1448140260072, 'njy57RJot0LhdYELkMDJ9gQZiOM=', 1448140260072, null, null, 'contactDisplayName', 'contacts', 'a112ed14-0106-4616-bb40-a416a8305465', 0, null, null, null, null, null, 2, null);

INSERT INTO stamp (stamptype, id, createdate, etag, modifydate, itemid) VALUES ('calendar', 1, 1448140260087, '', 1448140260087, 2);
INSERT INTO stamp (stamptype, id, createdate, etag, modifydate, itemid) VALUES ('card', 2, 1448140260087, '', 1448140260087, 3);