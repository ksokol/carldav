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
        @NamedQuery(name = "item.findByOwnerAndName", query = "select item from HibItem item where item.owner.email = :owner and item.name = :name"),
        @NamedQuery(name = "item.findByCollectionIdAndType", query = "select item from HibItem item join item.collection pd where item.collection.id=:parent and item.type=:type"),

        @NamedQuery(name = "collection.findByOwnerAndName", query = "select item from HibCollectionItem item where item.owner.email = :owner and item.name = :name"),

        // User Queries
        @NamedQuery(name = "user.byEmail.ignorecase", query="from User where lower(email)=lower(:email)"),
        @NamedQuery(name = "user.all", query = "from User")
})
package org.unitedinternet.cosmo.model.hibernate;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

