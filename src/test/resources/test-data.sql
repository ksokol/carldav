INSERT INTO users(id,locked,email,password,roles) VALUES (1,0,'root@localhost','098f6bcd4621d373cade4e832627b4f6','ROLE_ROOT');
INSERT INTO users(id,locked,email,password,roles) VALUES (2,0,'test01@localhost.de','098f6bcd4621d373cade4e832627b4f6','ROLE_USER');
INSERT INTO users(id,locked,email,password,roles) VALUES (3,0,'test02@localhost.de','098f6bcd4621d373cade4e832627b4f6','ROLE_USER');

INSERT INTO item (itemtype, id, createdate, etag, modifydate, clientcreatedate, clientmodifieddate, displayname, itemname, uid, version, lastmodification, lastmodifiedby, needsreply, sent, isautotriage, triagestatuscode, triagestatusrank, icaluid, hasmodifications, ownerid, modifiesitemid) VALUES
('homecollection', 1, to_timestamp('2015-11-21 09:11:00','YYYY-MM-DD HH:MI:SS'), 'ghFexXxxU+9KC/of1jmJ82wMFig=', to_timestamp('2015-11-21 09:11:00','YYYY-MM-DD HH:MI:SS'), null, null, null, 'test01@localhost.de', 'de359448-1ee0-4151-872d-eea0ee462bc6', 0, null, null, null, null, null, null, null, null, null, 2, null);
INSERT INTO item (itemtype, id, createdate, etag, modifydate, clientcreatedate, clientmodifieddate, displayname, itemname, uid, version, lastmodification, lastmodifiedby, needsreply, sent, isautotriage, triagestatuscode, triagestatusrank, icaluid, hasmodifications, ownerid, modifiesitemid) VALUES ('collection', 2, to_timestamp('2015-11-21 09:11:00','YYYY-MM-DD HH:MI:SS'), 'NVy57RJot0LhdYELkMDJ9gQZjOM=', to_timestamp('2015-11-21 09:11:00','YYYY-MM-DD HH:MI:SS'), null, null, 'calendarDisplayName', 'calendar', 'a172ed34-0106-4616-bb40-a416a8305465', 0, null, null, null, null, null, null, null, null, null, 2, null);
INSERT INTO item (itemtype, id, createdate, etag, modifydate, clientcreatedate, clientmodifieddate, displayname, itemname, uid, version, lastmodification, lastmodifiedby, needsreply, sent, isautotriage, triagestatuscode, triagestatusrank, icaluid, hasmodifications, ownerid, modifiesitemid) VALUES
('homecollection', 3, to_timestamp('2015-11-21 09:11:00','YYYY-MM-DD HH:MI:SS'), null, to_timestamp('2015-11-21 09:11:00','YYYY-MM-DD HH:MI:SS'), null, null, null, 'test02@localhost.de', '1e359448-1ee0-4151-872d-eea0ee462bc6', 0, null, null, null, null, null, null, null, null, null, 3, null);

INSERT INTO stamp (stamptype, id, createdate, etag, modifydate, itemid) VALUES ('calendar', 1, to_timestamp('2015-11-12 21:11:00','YYYY-MM-DD HH:MI:SS'), '', to_timestamp('2015-11-12 21:11:00','YYYY-MM-DD HH:MI:SS'), 2);

INSERT INTO collection_item (createdate, itemid, collectionid) VALUES (to_timestamp('2015-11-12 21:11:00','YYYY-MM-DD HH:MI:SS'), 2, 1);
