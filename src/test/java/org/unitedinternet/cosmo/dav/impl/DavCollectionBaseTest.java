/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav.impl;


import org.junit.Before;
import org.junit.Test;
import org.unitedinternet.cosmo.dav.BaseDavTestCase;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.caldav.report.MultigetReport;

/**
 * Test case for <code>DavCollectionBase</code>.
 */
public class DavCollectionBaseTest extends BaseDavTestCase
    implements ExtendedDavConstants {

    /**
     * Tests caldav report types.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCaldavReportTypes() throws Exception {
        DavCollectionBase test = new DavCollectionBase(null, null, testHelper.getEntityFactory());

        assert(test.getReportTypes().contains(MultigetReport.REPORT_TYPE_CALDAV_MULTIGET));
    }

    /**
     * SetUp.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        testHelper.logIn();
    }
}
