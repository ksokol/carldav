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
 * Named Queries
 */
@NamedQueries({
        // Item Queries
        @NamedQuery(name = "collections.children.by.parent", query = "select item from HibItem item join"
                + " item.collection pd where item.collection=:parent and item.type is null"), //TODO item.class=HibCollectionItem => item.type is null
        @NamedQuery(name = "collections.files.by.parent", query = "select item from HibItem item join"
                + " item.collection pd where item.collection=:parent and item.class=HibCardItem"),
        @NamedQuery(name = "item.by.parent.name", query = "select item from HibItem item join"
                + " item.collection pd where item.collection=:parent and item.name=:name"),
        // FIXME stfl .and.nullparent is not the correct name anymore!
        // FIXME check on class == HibCollectionItem  or select from HibCollectionItem
        @NamedQuery(name = "item.by.ownerName.name.nullParent", query = "select i from "
                + "HibCollectionItem i, User u where i.owner=u and u.email=:email and i.name=:name"),

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

