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
package org.unitedinternet.cosmo.dao.hibernate;

import carldav.service.generator.IdGenerator;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.query.ItemPathTranslator;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;

import java.util.Date;

public class ContentDaoImpl extends ItemDaoImpl implements ContentDao {

    public ContentDaoImpl(final IdGenerator idGenerator, final ItemPathTranslator itemPathTranslator) {
        super(idGenerator, itemPathTranslator);
    }

    public HibCollectionItem createCollection(HibCollectionItem parent,
                                              HibCollectionItem collection) {

        setBaseItemProps(collection);
        collection.setCollection(parent);

        getSession().save(collection);
        getSession().refresh(parent);
        getSession().flush();

        return collection;
    }

    public HibItem createContent(HibCollectionItem parent, HibItem content) {
        setBaseItemProps(content);

        // add parent to new content
        content.setCollection(parent);

        getSession().save(content);
        getSession().refresh(parent);
        getSession().flush();
        return content;
    }
}
