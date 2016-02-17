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

import static java.util.Locale.ENGLISH;

import org.hibernate.Query;
import org.hibernate.Session;
import org.unitedinternet.cosmo.dao.hibernate.AbstractDaoImpl;
import org.unitedinternet.cosmo.dao.query.ItemFilterProcessor;
import org.unitedinternet.cosmo.model.filter.BetweenExpression;
import org.unitedinternet.cosmo.model.filter.EqualsExpression;
import org.unitedinternet.cosmo.model.filter.EventStampFilter;
import org.unitedinternet.cosmo.model.filter.FilterCriteria;
import org.unitedinternet.cosmo.model.filter.FilterExpression;
import org.unitedinternet.cosmo.model.filter.FilterOrder;
import org.unitedinternet.cosmo.model.filter.FilterOrder.Order;
import org.unitedinternet.cosmo.model.filter.ILikeExpression;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.filter.JournalStampFilter;
import org.unitedinternet.cosmo.model.filter.LikeExpression;
import org.unitedinternet.cosmo.model.filter.NoteItemFilter;
import org.unitedinternet.cosmo.model.filter.NullExpression;
import org.unitedinternet.cosmo.model.filter.StampFilter;
import org.unitedinternet.cosmo.model.hibernate.HibItem;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * Standard Implementation of <code>ItemFilterProcessor</code>.
 * Translates filter into HQL Query, executes
 * query and processes the results.
 */
public class StandardItemFilterProcessor extends AbstractDaoImpl implements ItemFilterProcessor {

    public Set<HibItem> processFilter(ItemFilter filter) {
        Query hibQuery = buildQuery(getSession(), filter);
        List<HibItem> queryResults = hibQuery.list();
        return processResults(queryResults);
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
    public Query buildQuery(Session session, ItemFilter filter) {
        StringBuffer selectBuf = new StringBuffer();
        StringBuffer whereBuf = new StringBuffer();
        StringBuffer orderBuf = new StringBuffer();

        Map<String, Object> params = new TreeMap<>();

        if (filter instanceof NoteItemFilter) {
            handleNoteItemFilter(selectBuf, whereBuf, params, (NoteItemFilter) filter);
        } else if (filter instanceof ItemFilter) {
            handleContentItemFilter(selectBuf, whereBuf, params, filter);
        } else {
            handleItemFilter(selectBuf, whereBuf, params, filter);
        }

        selectBuf.append(whereBuf);

        for (FilterOrder fo : filter.getOrders()) {
            if (orderBuf.length() == 0) {
                orderBuf.append(" order by ");
            } else {
                orderBuf.append(", ");
            }

            orderBuf.append("i." + fo.getName());

            if (fo.getOrder().equals(Order.DESC)) {
                orderBuf.append(" desc");
            }
        }

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

    private void handleItemFilter(StringBuffer selectBuf,
                                  StringBuffer whereBuf, Map<String, Object> params,
                                  ItemFilter filter) {

        if ("".equals(selectBuf.toString())) {
            selectBuf.append("select i from HibItem i");
        }

        // filter on uid
        if (filter.getUid() != null) {
            formatExpression(whereBuf, params, "i.uid", filter.getUid());
        }


        // filter on parent
        if (filter.getParent() != null) {
            selectBuf.append(" join i.collection pd");
            appendWhere(whereBuf, "pd=:parent");
            params.put("parent", filter.getParent());
        }

        if (filter.getDisplayName() != null) {
            formatExpression(whereBuf, params, "i.displayName", filter.getDisplayName());
        }

        handleStampFilters(selectBuf, whereBuf, filter, params);

    }

    private void handleStampFilters(StringBuffer selectBuf,
                                    StringBuffer whereBuf,
                                    ItemFilter filter,
                                    Map<String, Object> params) {
        for (StampFilter stampFilter : filter.getStampFilters()) {
            if (stampFilter instanceof EventStampFilter) {
                handleStampFilter(selectBuf, whereBuf, stampFilter, params);
            } else if(stampFilter instanceof JournalStampFilter) {
                handleJournalFilter(whereBuf, stampFilter, params);
            }
        }
    }

    private void handleStampFilter(StringBuffer selectBuf,
                                   StringBuffer whereBuf,
                                   StampFilter filter,
                                   Map<String, Object> params) {

        appendWhere(whereBuf, "i.class=:clazz");
        params.put("clazz", filter.getType());

        // handle recurring event filter
        if (filter.getIsRecurring() != null) {
            if (filter.getIsRecurring() == true) {
                appendWhere(whereBuf, "(i.recurring=true)");
            } else {
                appendWhere(whereBuf, "(i.recurring=false)");
            }
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

    private void handleJournalFilter(StringBuffer whereBuf, StampFilter filter, Map<String, Object> params) {
        appendWhere(whereBuf, "i.class = 'journal'");

        // handle time range
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

    private void handleNoteItemFilter(StringBuffer selectBuf,
                                      StringBuffer whereBuf, Map<String, Object> params,
                                      NoteItemFilter filter) {
        selectBuf.append("select i from HibICalendarItem i");
        handleItemFilter(selectBuf, whereBuf, params, filter);
        handleContentItemFilter(selectBuf, whereBuf, params, filter);

        // filter by icaluid
        if (filter.getIcalUid() != null) {
            formatExpression(whereBuf, params, "i.uid", filter.getIcalUid());
        }

        // filter by body
        if (filter.getBody() != null) {
            formatExpression(whereBuf, params, "i.body", filter.getBody());
        }

        // filter by reminderTime
        if (filter.getReminderTime() != null) {
            formatExpression(whereBuf, params, "i.remindertime", filter.getReminderTime());
        }

        if(filter.getModifiedSince() != null){
            formatExpression(whereBuf, params, "i.modifiedDate", filter.getModifiedSince());
        }
    }

    private void handleContentItemFilter(StringBuffer selectBuf,
                                         StringBuffer whereBuf, Map<String, Object> params,
                                         ItemFilter filter) {

        if ("".equals(selectBuf.toString())) {
            selectBuf.append("select i from HibItem i");
            handleItemFilter(selectBuf, whereBuf, params, filter);
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

    /**
     * Because a timeRange query requires two passes: one to get the list
     * of possible events that occur in the range, and one
     * to expand recurring events if necessary.
     * This is required because we only index a start and end
     * for the entire recurrence series, and expansion is required to determine
     * if the event actually occurs, and to return individual occurences.
     */
    private HashSet<HibItem> processResults(List<HibItem> results) {
        HashSet<HibItem> processedResults = new HashSet<>();

        for (HibItem hibItem : results) {
            processedResults.add(hibItem);
        }

        return processedResults;
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
