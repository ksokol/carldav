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

import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;

import java.util.ArrayList;
import java.util.HashMap;
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
    private HibCollectionItem parent = null;
    private FilterCriteria uid = null;
    private Integer maxResults = null;

    private List<StampFilter> stampFilters = new ArrayList<>();
    private Map<String, String> filterProperties = new HashMap<>();
    private List<FilterOrder> order = new ArrayList<>();

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

    public HibCollectionItem getParent() {
        return parent;
    }

    /**
     * Match items by parent
     * @param parent parent to match
     */
    public void setParent(HibCollectionItem parent) {
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
    
    /**
     * Get a filter property
     * @param key key
     * @return value of property
     */
    public String getFilterProperty(String key) {
        return filterProperties.get(key);
    }
    
    /**
     * <p>
     * Add an item property to order results by.  For now, this is only 
     * really useful when used with setMaxResults(), where you
     * only want to return the top X items, where the items are
     * in a certain order before the top X are chosen.
     * </p>
     *
     * @param fo
     */
    public void addOrderBy(FilterOrder fo) {
        order.add(fo);
    }
    
    public List<FilterOrder> getOrders() {
        return order;
    }

    public Integer getMaxResults() {
        return maxResults;
    }
}
