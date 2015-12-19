/*
 * Copyright 2006 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Type Definitions for custom hibernate data types.
 */
@TypeDefs({

    @TypeDef(
            name="calendar_clob",
            typeClass = org.unitedinternet.cosmo.hibernate.CalendarClobType.class
    ),

    @TypeDef(
            name="long_timestamp",
            typeClass = org.unitedinternet.cosmo.hibernate.LongTimestampType.class
    )
})

@NamedQueries({
    // Item Queries
        @NamedQuery(name = "homeCollection.by.ownerId", query = "from HibHomeCollectionItem where owner.id=:ownerid"),
        @NamedQuery(name = "collections.children.by.parent", query = "select item from HibItem item join"
                + " item.parentDetails pd where pd.primaryKey.collection=:parent and item.class=HibCollectionItem"),
        @NamedQuery(name = "itemId.by.parentId.name", query = "select item.id from HibItem item join"
                + " item.parentDetails pd where pd.primaryKey.collection.id=:parentid and item.name=:name"),
        @NamedQuery(name = "itemid.by.uid", query = "select i.id from HibItem i where i.uid=:uid"),
        @NamedQuery(name = "item.by.parent.name", query = "select item from HibItem item join"
                + " item.parentDetails pd where pd.primaryKey.collection=:parent and item.name=:name"),
        // FIXME stfl .and.nullparent is not the correct name anymore!
        // FIXME check on class == HibCollectionItem  or select from HibCollectionItem
        @NamedQuery(name = "item.by.ownerName.name.nullParent", query = "select i from "
                + "HibCollectionItem i, HibUser u where i.owner=u and u.email=:username and" + " i.name=:name"),
        @NamedQuery(name = "noteItemId.by.parent.icaluid", query = "select item.id from HibNoteItem item"
                + " join item.parentDetails pd where pd.primaryKey.collection.id=:parentid and "
                + "item.icalUid=:icaluid and item.modifies is null"),
        @NamedQuery(name = "icalendarItem.by.parent.icaluid", query = "select item.id from "
                + "HibICalendarItem item join item.parentDetails pd where"
                + " pd.primaryKey.collection.id=:parentid and item.icalUid=:icaluid"),
        @NamedQuery(name = "contentItem.by.owner", query = "from HibContentItem i where i.owner=:owner"),
    
    // User Queries
    @NamedQuery(name="user.byUsername", query="from HibUser where username=:username"),
    @NamedQuery(name="user.byEmail.ignorecase", query="from HibUser where lower(email)=lower(:email)"),
        @NamedQuery(name = "user.all", query = "from HibUser"),

        // Event Queries
        @NamedQuery(name = "event.by.calendar.icaluid", query = "select i from HibNoteItem i join "
                + "i.parentDetails pd join i.stamps stamp where pd.primaryKey.collection=:calendar and "
                + "stamp.class=HibEventStamp and i.icalUid=:uid")
    
})
package org.unitedinternet.cosmo.model.hibernate;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

