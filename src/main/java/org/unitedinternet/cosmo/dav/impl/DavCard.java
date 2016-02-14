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
package org.unitedinternet.cosmo.dav.impl;

import static org.unitedinternet.cosmo.icalendar.ICalendarConstants.CARD_MEDIA_TYPE;

import carldav.service.generator.IdGenerator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.property.ContentType;
import org.unitedinternet.cosmo.model.hibernate.HibCardItem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DavCard extends DavContentBase {
    private static final Log LOG = LogFactory.getLog(DavCard.class);

    public DavCard(HibCardItem item,
                   DavResourceLocator locator,
                   DavResourceFactory factory,
                   IdGenerator idGenerator)
        throws CosmoDavException {
        super(item, locator, factory, idGenerator);

        registerLiveProperty(DavPropertyName.GETCONTENTLENGTH);
        registerLiveProperty(DavPropertyName.GETCONTENTTYPE);
    }

    /** */
    public DavCard(DavResourceLocator locator,
                   DavResourceFactory factory,
                   IdGenerator idGenerator)
        throws CosmoDavException {
        this(new HibCardItem(), locator, factory, idGenerator);
    }

    // WebDavResource

    public void writeTo(OutputContext outputContext)
        throws CosmoDavException, IOException {
        if (! exists()) {
            throw new IllegalStateException("cannot spool a nonexistent resource");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("spooling file " + getResourcePath());
        }

        HibCardItem content = (HibCardItem) getItem();
        final byte[] calendar = content.getCalendar().getBytes(StandardCharsets.UTF_8);

        outputContext.setContentType(CARD_MEDIA_TYPE);
        outputContext.setContentLength(calendar.length);
        outputContext.setModificationTime(getModificationTime());
        outputContext.setETag(getETag());

        if (! outputContext.hasStream()) {
            return;
        }
        if (content == null) {
            return;
        }

        IOUtil.spool(new ByteArrayInputStream(calendar), outputContext.getOutputStream());
    }

    
    /** */
    protected void populateItem(InputContext inputContext)
        throws CosmoDavException {
        super.populateItem(inputContext);

        HibCardItem file = (HibCardItem) getItem();

        try {
            InputStream content = inputContext.getInputStream();
            if (content != null) {
                file.setCalendar(IOUtils.toString(content));
            }
        } catch (IOException e) {
            throw new CosmoDavException(e);
        }
    }

    /** */
    protected void loadLiveProperties(DavPropertySet properties) {
        super.loadLiveProperties(properties);

        HibCardItem content = (HibCardItem) getItem();
        if (content == null) {
            return;
        }

        properties.add(new ContentType(CARD_MEDIA_TYPE, null));
    }

    /** */
    protected void removeLiveProperty(DavPropertyName name)
        throws CosmoDavException {
        super.removeLiveProperty(name);

        HibCardItem content = (HibCardItem) getItem();
        if (content == null) {
            return;
        }
    }

    @Override
    public boolean isCollection() {
        return false;
    }
}
