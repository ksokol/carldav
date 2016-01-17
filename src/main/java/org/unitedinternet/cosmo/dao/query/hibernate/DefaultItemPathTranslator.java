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
package org.unitedinternet.cosmo.dao.query.hibernate;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.abdera.i18n.text.UrlEncoding;
import org.hibernate.Query;
import org.hibernate.Session;
import org.unitedinternet.cosmo.dao.hibernate.AbstractDaoImpl;
import org.unitedinternet.cosmo.dao.query.ItemPathTranslator;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;

/**
 * Default implementation for ItempPathTranslator. This implementation expects
 * paths to be of the format: /username/parent1/parent2/itemname
 */
public class DefaultItemPathTranslator extends AbstractDaoImpl implements ItemPathTranslator {

    
   
    
    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.dao.query.ItemPathTranslator#findItemByPath(org.hibernate.Session,
     *      java.lang.String)
     */

    /**
     * Finds item by path.
     *
     * @param path The given path.
     * @return item The expected item.
     */
    public HibItem findItemByPath(final String path) {

        return (HibItem) findItemByPath(getSession(), path);

    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.query.ItemPathTranslator#findItemByPath(java.lang.String, org.unitedinternet.cosmo.model.CollectionItem)
     */

    /**
     * Finds item by the given path.
     *
     * @param path The given path.
     * @param root The collection item.
     * @return The expected item.
     */
    public HibItem findItemByPath(final String path, final HibCollectionItem root) {
        return (HibItem) findItemByPath(getSession(), path, root);
    }

    /**
     * {@inheritDoc}
     */
    public HibItem findItemParent(String path) {
        if (path == null) {
            return null;
        }

        int lastIndex = path.lastIndexOf("/");
        if (lastIndex == -1) {
            return null;
        }

        if ((lastIndex + 1) >= path.length()) {
            return null;
        }

        String parentPath = path.substring(0, lastIndex);

        return findItemByPath(parentPath);
    }

    /**
     * {@inheritDoc}
     */
    public String getItemName(String path) {
        if (path == null) {
            return null;
        }

        int lastIndex = path.lastIndexOf("/");
        if (lastIndex == -1) {
            return null;
        }

        if ((lastIndex + 1) >= path.length()) {
            return null;
        }

        return path.substring(lastIndex + 1);
    }

    /**
     * Finds item by the given path.
     *
     * @param session The current session.
     * @param path    The given path.
     * @return The expected item.
     */
    protected HibItem findItemByPath(Session session, String path) {

        if (path == null || "".equals(path)) {
            return null;
        }

        if (path.charAt(0) == '/') {
            path = path.substring(1, path.length());
        }

        String[] segments = path.split("/");

        if (segments.length == 0) {
            return null;
        }
        String username = decode(segments[0]);

        String rootName = username;
        HibItem rootHibItem = findRootItemByOwnerAndName(session, username,
                rootName);

        // If parent item doesn't exist don't go any further
        if (rootHibItem == null) {
            return null;
        }

        HibItem parentHibItem = rootHibItem;
        for (int i = 1; i < segments.length; i++) {
            HibItem nextHibItem = findItemByParentAndName(session, parentHibItem,
                    decode(segments[i]));
            parentHibItem = nextHibItem;
            // if any parent item doesn't exist then bail now
            if (parentHibItem == null) {
                return null;
            }
        }

        return parentHibItem;
    }

    /**
     * Finds item by path.
     *
     * @param session The current session.
     * @param path    The given path.
     * @param root    The collection root.
     * @return The expected item.
     */
    protected HibItem findItemByPath(Session session, String path, HibCollectionItem root) {

        if (path == null || "".equals(path)) {
            return null;
        }

        if (path.charAt(0) == '/') {
            path = path.substring(1, path.length());
        }

        String[] segments = path.split("/");

        if (segments.length == 0) {
            return null;
        }

        HibItem parentHibItem = root;
        for (int i = 0; i < segments.length; i++) {
            HibItem nextHibItem = findItemByParentAndName(session, parentHibItem,
                    decode(segments[i]));
            parentHibItem = nextHibItem;
            // if any parent item doesn't exist then bail now
            if (parentHibItem == null) {
                return null;
            }
        }

        return parentHibItem;
    }

    protected HibItem findRootItemByOwnerAndName(Session session,
                                              String username, String name) {
        Query hibQuery = session.getNamedQuery(
                "item.by.ownerName.name.nullParent").setParameter("email",
                username).setParameter("name", name);

        List<?> results = hibQuery.list();
        if (results.size() > 0) {
            return (HibItem) results.get(0);
        } else {
            return null;
        }
    }

    protected HibItem findItemByParentAndName(Session session, HibItem parent,
                                           String name) {
        Query hibQuery = session.getNamedQuery("item.by.parent.name")
                .setParameter("parent", parent).setParameter("name", name);

        List<?> results = hibQuery.list();
        if (results.size() > 0) {
            return (HibItem) results.get(0);
        } else {
            return null;
        }
    }
    private static String decode(String urlPath){
        try {
            return  UrlEncoding.decode(urlPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}