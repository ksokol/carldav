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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.WebdavResponseImpl;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class StandardDavResponse extends WebdavResponseImpl implements DavResponse, DavConstants {

    private static final Log LOG = LogFactory.getLog(StandardDavResponse.class);

    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();

    private HttpServletResponse originalHttpServletResponse;

    public StandardDavResponse(HttpServletResponse response) {
        super(response);
        originalHttpServletResponse = response;
    }

    public String getContentType() {
        return originalHttpServletResponse.getContentType();
    }

    public void setCharacterEncoding(String charset) {
        originalHttpServletResponse.setCharacterEncoding(charset);
    }

    @Override
    public String getHeader(String name) {
        return originalHttpServletResponse.getHeader(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return originalHttpServletResponse.getHeaderNames();
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return originalHttpServletResponse.getHeaders(name);
    }

    @Override
    public int getStatus() {
        return originalHttpServletResponse.getStatus();
    }

    public void sendDavError(CosmoDavException e)
        throws IOException {
        setStatus(e.getErrorCode());
        if (! e.hasContent()) {
            return;
        }

        XMLStreamWriter writer = null;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(out);
            writer.writeStartDocument();
            e.writeTo(writer);
            writer.writeEndDocument();

            setContentType("text/xml; charset=UTF-8");
            byte[] bytes = out.toByteArray();
            setContentLength(bytes.length);
            getOutputStream().write(bytes);
        } catch (Exception e2) {
            LOG.error("Error writing XML", e2);
            LOG.error("Original exception", e);
            setStatus(500);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (XMLStreamException e2) {
                    LOG.warn("Unable to close XML writer", e2);
                }
            }
        }
    }

    @Override
    public void setContentLengthLong(long len) {
    }
}
