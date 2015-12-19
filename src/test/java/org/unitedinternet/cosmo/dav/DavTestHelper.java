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
package org.unitedinternet.cosmo.dav;

import org.junit.Before;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.MockHelper;
import org.unitedinternet.cosmo.dav.impl.DavCalendarCollection;
import org.unitedinternet.cosmo.dav.impl.DavEvent;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.NoteItem;

import java.net.URL;

/**
 * DavTestHelper.
 */
public class DavTestHelper extends MockHelper implements ExtendedDavConstants {
    private StandardResourceFactory resourceFactory;
    private StandardResourceLocatorFactory locatorFactory;
    private DavResourceLocator homeLocator;

    private URL baseUrl;

    /**
     * Constructor.
     */
    public DavTestHelper() {
        super();

        resourceFactory =
            new StandardResourceFactory(getContentService(),
                                        getSecurityManager(),
                                        getEntityFactory()
            );
        locatorFactory = new StandardResourceLocatorFactory();
        try {
            baseUrl = new URL("http", "localhost", -1, "/dav");
        } catch (Exception e) {
            throw new CosmoException(e);
        }
    }

    /**
     * SetUp.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        String path = "/" + getUser().getEmail();
        homeLocator =
            locatorFactory.createResourceLocatorByPath(baseUrl, path);
    }

    /**
     * Gets resource factory.
     * @return Dav resource factory.
     */
    public DavResourceFactory getResourceFactory() {
        return resourceFactory;
    }

    /**
     * Gets resource locator factory.
     * @return The dav resource locator factory.
     */
    public DavResourceLocatorFactory getResourceLocatorFactory() {
        return locatorFactory;
    }

    /**
     * Gets home locator.
     * @return The dav resource locator.
     */
    public DavResourceLocator getHomeLocator() {
        return homeLocator;
    }

    /**
     * Creates member locator.
     * @param locator The locator.
     * @param segment The segment.
     * @return The dav resource locator.
     */
    public DavResourceLocator createMemberLocator(DavResourceLocator locator, String segment) {
        String path = locator.getPath() + "/" + segment;
        return locatorFactory.
            createResourceLocatorByPath(locator.getContext(), path);
    }

    /**
     * Initializes dav calendar collection.
     * @param name The name.
     * @return The dav calendar collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public DavCalendarCollection initializeDavCalendarCollection(String name)
        throws Exception {
        CollectionItem collection = (CollectionItem)
            getHomeCollection().getChildByName(name);
        if (collection == null) {
            collection = makeAndStoreDummyCalendarCollection(name);
        }
        DavResourceLocator locator =  createMemberLocator(homeLocator, collection.getName());
        return new DavCalendarCollection(collection, locator,
                                         resourceFactory, getEntityFactory());
    }

    /**
     * Initializes dav event.
     * @param parent The parent.
     * @param name The name.
     * @return Dav event.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public DavEvent initializeDavEvent(DavCalendarCollection parent,
                                       String name)
        throws Exception {
        CollectionItem collection = (CollectionItem) parent.getItem();
        NoteItem item = (NoteItem) collection.getChildByName(name);
        if (item == null) {
            item = makeAndStoreDummyItem(collection, name);
        }
        DavResourceLocator locator =
            createMemberLocator(parent.getResourceLocator(), item.getName());
        return new DavEvent(item, locator, resourceFactory, getEntityFactory());
    }
}
