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
package org.unitedinternet.cosmo.dao.hibernate.query;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import org.hibernate.jpa.internal.QueryImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.IntegrationTestSupport;
import org.unitedinternet.cosmo.dao.query.hibernate.StandardItemFilterProcessor;
import org.unitedinternet.cosmo.model.filter.*;
import carldav.entity.Item;

import javax.persistence.EntityManager;
import java.util.Calendar;
import java.util.Date;

public class StandardItemFilterProcessorTest extends IntegrationTestSupport {

    @Autowired
    private EntityManager entityManager;

    private StandardItemFilterProcessor queryBuilder;

    private TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();

    @Before
    public void before() {
        queryBuilder = new StandardItemFilterProcessor();
        queryBuilder.setEntityManager(entityManager);
    }

    /**
     * Tests uid query.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testUidQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        filter.setUid(Restrictions.eq("abc"));
        QueryImpl query = (QueryImpl) queryBuilder.buildQuery(entityManager, filter);
        Assert.assertEquals("select i from Item i where i.uid=:param0", query.getHibernateQuery().getQueryString());
    }

    @Test
    public void testModifiedSinceQuery(){
        ItemFilter filter = new ItemFilter();
        Calendar c = Calendar.getInstance();
        Date end = c.getTime();
        c.add(Calendar.YEAR, -1);
        filter.setModifiedSince(Restrictions.between(c.getTime(), end));
        QueryImpl query = (QueryImpl) queryBuilder.buildQuery(entityManager, filter);
        Assert.assertEquals("select i from Item i where i.modifiedDate between :param0 and :param1", query.getHibernateQuery().getQueryString());
    }

    /**
     * Tests display name query.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDisplayNameQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        filter.setDisplayName(Restrictions.eq("test"));
        QueryImpl query = (QueryImpl) queryBuilder.buildQuery(entityManager, filter);
        Assert.assertEquals("select i from Item i where i.displayName=:param0", query.getHibernateQuery().getQueryString());

        filter.setDisplayName(Restrictions.neq("test"));
        query = (QueryImpl) queryBuilder.buildQuery(entityManager, filter);
        Assert.assertEquals("select i from Item i where i.displayName!=:param0", query.getHibernateQuery().getQueryString());

        filter.setDisplayName(Restrictions.like("test"));
        query = (QueryImpl) queryBuilder.buildQuery(entityManager, filter);
        Assert.assertEquals("select i from Item i where i.displayName like :param0", query.getHibernateQuery().getQueryString());

        filter.setDisplayName(Restrictions.nlike("test"));
        query = (QueryImpl) queryBuilder.buildQuery(entityManager, filter);
        Assert.assertEquals("select i from Item i where i.displayName not like :param0", query.getHibernateQuery().getQueryString());

        filter.setDisplayName(Restrictions.isNull());
        query = (QueryImpl) queryBuilder.buildQuery(entityManager, filter);
        Assert.assertEquals("select i from Item i where i.displayName is null", query.getHibernateQuery().getQueryString());

        filter.setDisplayName(Restrictions.ilike("test"));
        query = (QueryImpl) queryBuilder.buildQuery(entityManager, filter);
        Assert.assertEquals("select i from Item i where lower(i.displayName) like :param0", query.getHibernateQuery().getQueryString());

        filter.setDisplayName(Restrictions.nilike("test"));
        query = (QueryImpl) queryBuilder.buildQuery(entityManager, filter);
        Assert.assertEquals("select i from Item i where lower(i.displayName) not like :param0", query.getHibernateQuery().getQueryString());

    }

    /**
     * Tests parent query.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParentQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        filter.setParent(1L);
        QueryImpl query = (QueryImpl) queryBuilder.buildQuery(entityManager, filter);
        Assert.assertEquals("select i from Item i join i.collection pd where "
                + "pd.id=:parent", query.getHibernateQuery().getQueryString());
    }

    /**
     * Tests display name and parent query.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDisplayNameAndParentQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        filter.setParent(0L);
        filter.setDisplayName(Restrictions.eq("test"));
        QueryImpl query = (QueryImpl) queryBuilder.buildQuery(entityManager, filter);
        Assert.assertEquals("select i from Item i join i.collection pd where "
                + "pd.id=:parent and i.displayName=:param1", query.getHibernateQuery().getQueryString());
    }

    /**
     * Tests basic stamp query.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testBasicStampQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        StampFilter stampFilter = new StampFilter();
        stampFilter.setStampClass(Item.class);
        filter.getStampFilters().add(stampFilter);
        QueryImpl query = (QueryImpl) queryBuilder.buildQuery(entityManager, filter);
        Assert.assertEquals("select i from Item i", query.getHibernateQuery().getQueryString());
    }

    /**
     * Tests event stamp query.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEventStampQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        StampFilter eventFilter = new StampFilter(Item.Type.VEVENT);
        filter.setParent(0L);
        filter.setDisplayName(Restrictions.eq("test"));
        filter.setIcalUid(Restrictions.eq("icaluid"));
        //filter.setBody("body");
        filter.getStampFilters().add(eventFilter);
        QueryImpl query = (QueryImpl) queryBuilder.buildQuery(entityManager, filter);
        Assert.assertEquals("select i from Item i join i.collection pd "
                + "where pd.id=:parent and "
                + "i.displayName=:param1 and i.type=:type and i.uid=:param3", query.getHibernateQuery().getQueryString());

        eventFilter.setIsRecurring(true);
        query = (QueryImpl) queryBuilder.buildQuery(entityManager, filter);
        Assert.assertEquals("select i from Item i join i.collection pd "
                + "where pd.id=:parent and i.displayName=:param1 and "
                + "i.type=:type and (i.recurring=:recurring) "
                + "and i.uid=:param4", query.getHibernateQuery().getQueryString());
    }

    /**
     * Tests event stamp time range query.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEventStampTimeRangeQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        StampFilter eventFilter = new StampFilter(Item.Type.VEVENT);
        Period period = new Period(new DateTime("20070101T100000Z"), new DateTime("20070201T100000Z"));
        eventFilter.setPeriod(period);
        eventFilter.setTimezone(registry.getTimeZone("America/Chicago"));

        filter.setParent(0L);
        filter.getStampFilters().add(eventFilter);
        QueryImpl query = (QueryImpl) queryBuilder.buildQuery(entityManager, filter);
        Assert.assertEquals("select i from Item i join i.collection pd " +
                "where pd.id=:parent and i.type=:type and ( (i.startDate < :endDate) and i.endDate > :startDate) " +
                "or (i.startDate=i.endDate and (i.startDate=:startDate or i.startDate=:endDate)))"
                ,query.getHibernateQuery().getQueryString());
    }
}
