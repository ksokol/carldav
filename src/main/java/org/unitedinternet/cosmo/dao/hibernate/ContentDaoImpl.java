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
import org.springframework.util.Assert;
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

        if (parent == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }

        if (collection == null) {
            throw new IllegalArgumentException("collection cannot be null");
        }

        if (collection.getOwner() == null) {
            throw new IllegalArgumentException("collection must have owner");
        }

        if (getBaseModelObject(collection).getId() != null) {
            throw new IllegalArgumentException("invalid collection id (expected -1)");
        }

        setBaseItemProps(collection);
        collection.setCollection(parent);

        getSession().save(collection);
        getSession().refresh(parent);
        getSession().flush();

        return collection;
    }

    public HibItem createContent(HibCollectionItem parent, HibItem content) {
        createContentInternal(parent, content);
        getSession().flush();
        return content;
    }

    public HibItem updateContent(HibItem content) {
        updateContentInternal(content);
        getSession().flush();
        return content;
    }

    public void removeCollection(HibCollectionItem collection) {
        removeContent(collection);
    }

    public void removeContent(HibItem content) {
        Assert.notNull(content, "content is null");
        getSession().refresh(content);
        content.setCollection(null);
        getSession().delete(content);
        getSession().flush();
    }

    @Override
    public void removeItem(HibItem hibItem) {
        remove(hibItem);
    }

    @Override
    public void removeItemByPath(String path) {
        remove(this.findItemByPath(path));
    }

    @Override
    public void removeItemByUid(String uid) {
        remove(this.findItemByUid(uid));
    }

    private void remove(HibItem hibItem) {
        if (hibItem instanceof HibItem) {
            removeContent(hibItem);
        } else if (hibItem instanceof HibCollectionItem) {
            removeCollection((HibCollectionItem) hibItem);
        } else {
            super.removeItem(hibItem);
        }
    }

    protected void createContentInternal(HibCollectionItem parent, HibItem content) {

        if (parent == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }

        if (content == null) {
            throw new IllegalArgumentException("content cannot be null");
        }

        if (getBaseModelObject(content).getId() != null) {
            throw new IllegalArgumentException("invalid content id (expected -1)");
        }

        setBaseItemProps(content);

        // add parent to new content
        content.setCollection(parent);

        getSession().save(content);
        getSession().refresh(parent);
    }

    protected void updateContentInternal(HibItem content) {
        getSession().update(content);
        content.setModifiedDate(new Date());
    }
}
