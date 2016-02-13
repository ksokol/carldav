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
        )

})

/*
 * Named Queries
 */
@NamedQueries({
        // Item Queries
        @NamedQuery(name = "homeCollection.by.ownerId", query = "from HibHomeCollectionItem where owner.id=:ownerid"),
        @NamedQuery(name = "item.by.ownerId.parentId.name", query = "select item from HibItem item join"
                + " item.collection pd where item.owner.id=:ownerid and "
                + "pd.id=:parentid and item.name=:name"),
        @NamedQuery(name = "collections.children.by.parent", query = "select item from HibItem item join"
                + " item.collection pd where item.collection=:parent and item.class=HibCollectionItem"),
        @NamedQuery(name = "collections.files.by.parent", query = "select item from HibItem item join"
                + " item.collection pd where item.collection=:parent and item.class=HibFileItem"),
        @NamedQuery(name = "itemId.by.parentId.name", query = "select item.id from HibItem item join"
                + " item.collection pd where pd.id=:parentid and item.name=:name"),
        @NamedQuery(name = "item.by.uid", query = "from HibItem i where i.uid=:uid"),
        @NamedQuery(name = "itemid.by.uid", query = "select i.id from HibItem i where i.uid=:uid"),
        @NamedQuery(name = "item.by.parent.name", query = "select item from HibItem item join"
                + " item.collection pd where item.collection=:parent and item.name=:name"),
        // FIXME stfl .and.nullparent is not the correct name anymore!
        // FIXME check on class == HibCollectionItem  or select from HibCollectionItem
        @NamedQuery(name = "item.by.ownerName.name.nullParent", query = "select i from "
                + "HibCollectionItem i, User u where i.owner=u and u.email=:email and i.name=:name"),
        @NamedQuery(name = "noteItemId.by.parent.icaluid", query = "select item.id from HibNoteItem item"
                + " join item.collection pd where pd.id=:parentid and item.icalUid=:icaluid"),
        @NamedQuery(name = "icalendarItem.by.parent.icaluid", query = "select item.id from "
                + "HibICalendarItem item join item.collection pd where"
                + " pd.id=:parentid and item.icalUid=:icaluid"),
        @NamedQuery(name = "contentItem.by.owner", query = "from HibItem i where i.owner=:owner"),

        // User Queries
        @NamedQuery(name="user.byEmail", query="from User where email=:email"),
        @NamedQuery(name="user.byEmail.ignorecase", query="from User where lower(email)=lower(:email)"),
        @NamedQuery(name = "user.byUsernameOrEmail.ignorecase.ingoreId", query = "from User where"
                + " id!=:userid and lower(email)=lower(:email))"),
        @NamedQuery(name = "user.all", query = "from User")
})
package org.unitedinternet.cosmo.model.hibernate;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

