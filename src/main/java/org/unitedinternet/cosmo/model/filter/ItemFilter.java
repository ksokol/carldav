/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.model.filter;

import carldav.entity.Item;
import org.hibernate.jpa.criteria.path.RootImpl;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a filter that matches a set of criteria to all items.
 * The set of criteria is essentially "ANDed" together.
 * For example if displayName and parent are set, then the filter
 * will match all items that match the displayName set AND belong to
 * the parent set.
 */
public class ItemFilter {
   
    private FilterCriteria displayName = null;
    private Long parent = null;
    private FilterCriteria uid = null;
    private Integer maxResults = null;
    private FilterCriteria icalUid = null;
    private FilterCriteria reminderTime = null;
    private FilterCriteria modifiedSince = null;

    private List<StampFilter> stampFilters = new ArrayList<>();

    /**
     * Return a specific StampFilter instance
     * @param clazz StampFilter class
     * @return StampFilter instance that matches the given class
     */
    public StampFilter getStampFilter(Class<?> clazz) {
        for(StampFilter sf: stampFilters) {
            if(sf.getClass().equals(clazz)) {
                return sf;
            }
        }
        return null;
    }
    
    /**
     * List of StampFilters.  If there are multiple stamp filters,
     * each filter must match for an item to match the ItemFilter.
     * @return list of stamp filters
     */
    public List<StampFilter> getStampFilters() {
        return stampFilters;
    }

    public FilterCriteria getDisplayName() {
        return displayName;
    }

    /**
     * Match items by item displayName
     * @param displayName displayName to match
     */
    public void setDisplayName(FilterCriteria displayName) {
        this.displayName = displayName;
    }

    public Long getParent() {
        return parent;
    }

    /**
     * Match items by parent
     * @param parent parent to match
     */
    public void setParent(Long parent) {
        this.parent = parent;
    }

    public FilterCriteria getUid() {
        return uid;
    }

    /**
     * Match item by uid
     * @param uid uid to match
     */
    public void setUid(FilterCriteria uid) {
        this.uid = uid;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public FilterCriteria getIcalUid() {
        return icalUid;
    }

    /**
     * Match notes with an specific icalUid
     * @param icalUid
     */
    public void setIcalUid(FilterCriteria icalUid) {
        this.icalUid = icalUid;
    }

    public FilterCriteria getReminderTime() {
        return reminderTime;
    }

    /**
     * Matches notes with reminderTime matching the specified criteria.
     * @param reminderTime
     */
    public void setReminderTime(FilterCriteria reminderTime) {
        this.reminderTime = reminderTime;
    }

    public FilterCriteria getModifiedSince() {
        return modifiedSince;
    }

    public void setModifiedSince(FilterCriteria modifiedSince) {
        this.modifiedSince = modifiedSince;
    }

    public void bind(EntityManager entityManager, Root<Item> root, CriteriaQuery<Item> query, CriteriaBuilder builder, StringBuffer selectBuf, Map<String, Object> params) {
        final StringBuffer whereBuf = new StringBuffer();
        root.alias("i");

        // filter on parent
        if (getParent() != null) {
            final Join<Object, Object> collection = root.join("collection");

            collection.alias("pd");
            final Path<Long> collectionId = collection.get("id");

            CriteriaQuery<Item> select = query.select(root);

            select.where(builder.equal(collectionId, 999666L));

            selectBuf.append(entityManager.createQuery(query).unwrap(org.hibernate.Query.class).getQueryString().replace("999666L", ":parent"));

            params.put("parent", getParent());
            selectBuf.append(" ");
            appendWhere(whereBuf, "");
        } else {
            selectBuf.append(entityManager.createQuery(query).unwrap(org.hibernate.Query.class).getQueryString());
            appendWhere(whereBuf, "  ");
        }

        // filter on uid
        if (getUid() != null) {
            formatExpression(whereBuf, params, "i.uid", getUid());
        }


        if (getDisplayName() != null) {
            formatExpression(whereBuf, params, "i.displayName", getDisplayName());
        }

        // filter by icaluid
        if (getIcalUid() != null) {
            formatExpression(whereBuf, params, "i.uid", getIcalUid());
        }

        // filter by reminderTime
        if (getReminderTime() != null) {
            formatExpression(whereBuf, params, "i.remindertime", getReminderTime());
        }

        if(getModifiedSince() != null){
            formatExpression(whereBuf, params, "i.modifiedDate", getModifiedSince());
        }

        for (StampFilter stampFilter : getStampFilters()) {
            stampFilter.bind(whereBuf, params);
        }

        selectBuf.append(whereBuf);
    }

    private void formatExpression(StringBuffer whereBuf,
                                  Map<String, Object> params, String propName,
                                  FilterCriteria fc) {

        StringBuffer expBuf = new StringBuffer();
        FilterExpression exp = (FilterExpression) fc;
        exp.bind(expBuf, propName, params);
        appendWhere(whereBuf, expBuf.toString());
    }


    private void appendWhere(StringBuffer whereBuf, String toAppend) {
        if ("   ".equals(whereBuf.toString())) {
            whereBuf.append(" where " + toAppend);
        } else if ("".equals(whereBuf.toString())) {
            whereBuf.append(" " + toAppend);
        } else {
            whereBuf.append(" and " + toAppend);
        }
    }
}
