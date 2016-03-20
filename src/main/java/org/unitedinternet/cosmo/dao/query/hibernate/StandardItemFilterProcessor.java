/*
 * Copyright 2005-2007 Open Source Applications Foundation
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

import carldav.entity.Item;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.dao.query.ItemFilterProcessor;
import org.unitedinternet.cosmo.model.filter.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import static java.util.Locale.ENGLISH;

/**
 * Standard Implementation of <code>ItemFilterProcessor</code>.
 * Translates filter into HQL Query, executes
 * query and processes the results.
 */
public class StandardItemFilterProcessor implements ItemFilterProcessor {

    private static final CalendarFilterConverter filterConverter = new CalendarFilterConverter();

    @PersistenceContext
    private EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Item> processFilter(CalendarFilter filter) {
        final ItemFilter itemFilter = filterConverter.translateToItemFilter(filter);
        Query hibQuery = buildQuery(entityManager, itemFilter);
        return hibQuery.getResultList();
    }

    /**
     * Build Hibernate Query from ItemFilter using HQL.
     * The query returned is essentially the first pass at
     * retrieving the matched items.  A second pass is required in
     * order determine if any recurring events match a timeRange
     * in the filter.  This is due to the fact that recurring events
     * may have complicated recurrence rules that are extremely
     * hard to match using HQL.
     *
     * @param session session
     * @param filter  item filter
     * @return hibernate query built using HQL
     */
    public Query buildQuery(EntityManager session, ItemFilter filter) {
        StringBuffer selectBuf = new StringBuffer();
        StringBuffer whereBuf = new StringBuffer();
        StringBuffer orderBuf = new StringBuffer();

        Map<String, Object> params = new TreeMap<>();

        selectBuf.append("select i from Item i");

        // filter on parent
        if (filter.getParent() != null) {
            selectBuf.append(" join i.collection pd");
            appendWhere(whereBuf, "pd.id=:parent");
            params.put("parent", filter.getParent());
        }

        handleItemFilter(whereBuf, params, filter);

        selectBuf.append(whereBuf);
        selectBuf.append(orderBuf);

        Query hqlQuery = session.createQuery(selectBuf.toString());

        for (Entry<String, Object> param : params.entrySet()) {
            hqlQuery.setParameter(param.getKey(), param.getValue());
        }

        if (filter.getMaxResults() != null) {
            hqlQuery.setMaxResults(filter.getMaxResults());
        }

        return hqlQuery;
    }

    private void handleItemFilter(StringBuffer whereBuf, Map<String, Object> params,
                                  ItemFilter filter) {

        // filter on uid
        if (filter.getUid() != null) {
            formatExpression(whereBuf, params, "i.uid", filter.getUid());
        }


        if (filter.getDisplayName() != null) {
            formatExpression(whereBuf, params, "i.displayName", filter.getDisplayName());
        }

        handleStampFilters(whereBuf, filter, params);

        // filter by icaluid
        if (filter.getIcalUid() != null) {
            formatExpression(whereBuf, params, "i.uid", filter.getIcalUid());
        }

        // filter by reminderTime
        if (filter.getReminderTime() != null) {
            formatExpression(whereBuf, params, "i.remindertime", filter.getReminderTime());
        }

        if(filter.getModifiedSince() != null){
            formatExpression(whereBuf, params, "i.modifiedDate", filter.getModifiedSince());
        }
    }

    private void handleStampFilters(StringBuffer whereBuf,
                                    ItemFilter filter,
                                    Map<String, Object> params) {
        for (StampFilter stampFilter : filter.getStampFilters()) {
                handleStampFilter(whereBuf, stampFilter, params);
        }
    }

    private void handleStampFilter(StringBuffer whereBuf,
                                   StampFilter filter,
                                   Map<String, Object> params) {

        if(filter.getType() != null) {
            appendWhere(whereBuf, "i.type=:type");
            params.put("type", filter.getType());
        }

        // handle recurring event filter
        if (filter.getIsRecurring() != null) {
            appendWhere(whereBuf, "(i.recurring=:recurring)");
            params.put("recurring", filter.getIsRecurring());
        }

        if (filter.getPeriod() != null) {
            whereBuf.append(" and ( ");
            whereBuf.append("(i.startDate < :endDate)");
            whereBuf.append(" and i.endDate > :startDate)");

            // edge case where start==end
            whereBuf.append(" or (i.startDate=i.endDate and (i.startDate=:startDate or i.startDate=:endDate))");

            whereBuf.append(")");

            params.put("startDate", filter.getStart());
            params.put("endDate", filter.getEnd());
        }
    }

    private void appendWhere(StringBuffer whereBuf, String toAppend) {
        if ("".equals(whereBuf.toString())) {
            whereBuf.append(" where " + toAppend);
        } else {
            whereBuf.append(" and " + toAppend);
        }
    }

    private String formatForLike(String toFormat) {
        return "%" + toFormat + "%";
    }

    private void formatExpression(StringBuffer whereBuf,
                                  Map<String, Object> params, String propName,
                                  FilterCriteria fc) {

        StringBuffer expBuf = new StringBuffer();

        FilterExpression exp = (FilterExpression) fc;

        if (exp instanceof NullExpression) {
            expBuf.append(propName);
            if (exp.isNegated()) {
                expBuf.append(" is not null");
            } else {
                expBuf.append(" is null");
            }
        } else if (exp instanceof BetweenExpression) {
            BetweenExpression be = (BetweenExpression) exp;
            expBuf.append(propName);
            if (exp.isNegated()) {
                expBuf.append(" not");
            }

            String param = "param" + params.size();
            expBuf.append(" between :" + param);
            params.put(param, be.getValue1());
            param = "param" + params.size();
            expBuf.append(" and :" + param);
            params.put(param, be.getValue2());
        } else {
            String param = "param" + params.size();
            if (exp instanceof EqualsExpression) {
                expBuf.append(propName);
                if (exp.isNegated()) {
                    expBuf.append("!=");
                } else {
                    expBuf.append("=");
                }

                params.put(param, exp.getValue());

            } else if (exp instanceof LikeExpression) {
                expBuf.append(propName);
                if (exp.isNegated()) {
                    expBuf.append(" not like ");
                } else {
                    expBuf.append(" like ");
                }

                params.put(param, formatForLike(exp.getValue().toString()));
            } else if (exp instanceof ILikeExpression) {
                expBuf.append("lower(" + propName + ")");
                if (exp.isNegated()) {
                    expBuf.append(" not like ");
                } else {
                    expBuf.append(" like ");
                }

                params.put(param, formatForLike(exp.getValue().toString().toLowerCase(ENGLISH)));
            }

            expBuf.append(":" + param);
        }

        appendWhere(whereBuf, expBuf.toString());
    }

}
