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

import carldav.jackrabbit.webdav.CustomReportInfo;
import org.apache.abdera.util.EntityTag;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.WebdavRequestImpl;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.DavResourceLocatorFactory;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.UnsupportedMediaTypeException;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.server.ServerConstants;
import org.unitedinternet.cosmo.util.BufferedServletInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

public class StandardDavRequest extends WebdavRequestImpl implements DavRequest, ExtendedDavConstants, CaldavConstants {

    private static final MimeType APPLICATION_XML = registerMimeType("application/xml");
    private static final MimeType TEXT_XML = registerMimeType("text/xml");
    
    /**
     * 
     * @param s mimeType as String
     * @return MimeType 
     */
    private static final MimeType registerMimeType(String s) {
        try {
            return new MimeType(s);
        } catch (MimeTypeParseException e) {
            throw new RuntimeException("Can't register MIME type " + s, e);
        }
    }

    private int propfindType = PROPFIND_ALL_PROP;
    private DavPropertyNameSet propfindProps;
    private ReportInfo reportInfo;
    private boolean bufferRequestContent = false;
    private long bufferedContentLength = -1;
    private DavResourceLocatorFactory locatorFactory;
    private DavResourceLocator locator;
    private HttpServletRequest originalHttpServletRequest;
    
    /**
     * 
     * {@inheritDoc}
     */
    public Enumeration<String> getAttributeNames() {
        return originalHttpServletRequest.getAttributeNames();
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    public String getContentType() {
        return originalHttpServletRequest.getContentType();
    }

    /**
     * 
     * {@inheritDoc}
     */
    public Enumeration<String> getHeaders(String name) {
        return originalHttpServletRequest.getHeaders(name);
    }

    public Enumeration<String> getHeaderNames() {
        return originalHttpServletRequest.getHeaderNames();
    }

    public Map<String, String[]> getParameterMap() {
        return originalHttpServletRequest.getParameterMap();
    }

    public Enumeration<Locale> getLocales() {
        return originalHttpServletRequest.getLocales();
    }

    public String getLocalName() {
        return originalHttpServletRequest.getLocalName();
    }

    public String getLocalAddr() {
        return originalHttpServletRequest.getLocalAddr();
    }

    public int getLocalPort() {
        return originalHttpServletRequest.getLocalPort();
    }

    public Enumeration<String> getParameterNames() {
        return originalHttpServletRequest.getParameterNames();
    }
    


    public int getRemotePort() {
        return originalHttpServletRequest.getRemotePort();
    }

    public boolean isRequestedSessionIdFromCookie() {
        return originalHttpServletRequest.isRequestedSessionIdFromCookie();
    }

    public boolean isRequestedSessionIdFromURL() {
        return originalHttpServletRequest.isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdFromUrl() {
        return originalHttpServletRequest.isRequestedSessionIdFromUrl();
    }

    /**
     *
     * @param request HttpServletRequest
     * @param factory DavResourceLocatorFactory
     */
    public StandardDavRequest(HttpServletRequest request,
            DavResourceLocatorFactory factory) {
        this(request, factory, false);
    }

    /**
     * 
     * @param request HttpServletRequest
     * @param factory DavResourceLocatorFactory
     * @param bufferRequestContent boolean
     */
    public StandardDavRequest(HttpServletRequest request,
            DavResourceLocatorFactory factory,
            boolean bufferRequestContent) {
        super(request, null);
        originalHttpServletRequest = request;
        this.locatorFactory = factory;
        this.bufferRequestContent = bufferRequestContent;
    }

    // DavRequest methods

    public EntityTag[] getIfMatch() {
        return EntityTag.parseTags(getHeader("If-Match"));
    }

    /**
     * 
     * {@inheritDoc}
     */
    public EntityTag[] getIfNoneMatch() {
        return EntityTag.parseTags(getHeader("If-None-Match"));
    }

    /**
     * 
     * {@inheritDoc}
     */
    public int getPropFindType() throws CosmoDavException {
        if (propfindProps == null) {
            parsePropFindRequest();
        }
        return propfindType;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public DavPropertyNameSet getPropFindProperties() throws CosmoDavException {
        if (propfindProps == null) {
            parsePropFindRequest();
        }
        return propfindProps;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public DavResourceLocator getResourceLocator() {
        if (locator == null) {
            URL context = null;
            try {

                String basePath = getContextPath() + "/" + ServerConstants.SVC_DAV;
                context = new URL(getScheme(), getServerName(),
                        getServerPort(), basePath);

                locator = locatorFactory.createResourceLocatorByUri(context,
                        getRequestURI());
            } catch (CosmoDavException e) {
                throw new RuntimeException(e);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        return locator;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public ReportInfo getReportInfo() throws CosmoDavException {
        if (reportInfo == null) {
            reportInfo = parseReportRequest();
        }
        return reportInfo;
    }

    // private methods
    /**
     * 
     * @return Document
     * @throws CosmoDavException 
     */
    private Document getSafeRequestDocument() throws CosmoDavException {
        return getSafeRequestDocument(true);
    }
    /**
     * 
     * @param requireDocument boolean 
     * @return Document
     * @throws CosmoDavException 
     */
    private Document getSafeRequestDocument(boolean requireDocument)
            throws CosmoDavException {
        try {
            if (StringUtils.isBlank(getContentType()) && requireDocument) {
                throw new BadRequestException("No Content-Type specified");
            }
            MimeType mimeType = new MimeType(getContentType());
            if (!(mimeType.match(APPLICATION_XML) || mimeType.match(TEXT_XML))) {
                throw new UnsupportedMediaTypeException(
                        "Expected Content-Type " + APPLICATION_XML + " or "
                                + TEXT_XML);
            }

            return getRequestDocument();

        } catch (MimeTypeParseException e) {
            throw new UnsupportedMediaTypeException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throwBadRequestExceptionFrom(e);
        } catch (DavException e) {
            throwBadRequestExceptionFrom(e);
        }

        return null;
    }
    
    /**
     * 
     * @param e 
     * @throws BadRequestException 
     */
    private void throwBadRequestExceptionFrom(Exception e)
            throws BadRequestException {
        Throwable cause = e.getCause();
        String msg = cause != null ? cause.getMessage()
                : "Unknown error parsing request document";
        throw new BadRequestException(msg);
    }

    /**
     * 
     * @throws CosmoDavException 
     */
    private void parsePropFindRequest() throws CosmoDavException {
        Document requestDocument = getSafeRequestDocument();
        if (requestDocument == null) {
            // treat as allprop
            propfindType = PROPFIND_ALL_PROP;
            propfindProps = new DavPropertyNameSet();
            return;
        }

        Element root = requestDocument.getDocumentElement();
        if (!DomUtil.matches(root, XML_PROPFIND, NAMESPACE)) {
            throw new BadRequestException("Expected " + QN_PROPFIND
                    + " root element");
        }

        Element prop = DomUtil.getChildElement(root, XML_PROP, NAMESPACE);
        if (prop != null) {
            propfindType = PROPFIND_BY_PROPERTY;
            propfindProps = new DavPropertyNameSet(prop);
            return;
        }

        if (DomUtil.getChildElement(root, XML_PROPNAME, NAMESPACE) != null) {
            propfindType = PROPFIND_PROPERTY_NAMES;
            propfindProps = new DavPropertyNameSet();
            return;
        }

        if (DomUtil.getChildElement(root, XML_ALLPROP, NAMESPACE) != null) {
            propfindType = PROPFIND_ALL_PROP;
            propfindProps = new DavPropertyNameSet();

            Element include = DomUtil.getChildElement(root, "include",
                    NAMESPACE);
            if (include != null) {
                ElementIterator included = DomUtil.getChildren(include);
                while (included.hasNext()) {
                    DavPropertyName name = DavPropertyName
                            .createFromXml(included.nextElement());
                    propfindProps.add(name);
                }
            }

            return;
        }

        throw new BadRequestException("Expected one of " + XML_PROP + ", "
                + XML_PROPNAME + ", or " + XML_ALLPROP + " as child of "
                + QN_PROPFIND);
    }

    /**
     *  
     * @return ReportInfo
     * @throws CosmoDavException 
     */
    private ReportInfo parseReportRequest() throws CosmoDavException {
        Document requestDocument = getSafeRequestDocument();
        if (requestDocument == null) { // reports with no bodies are supported
                                        // for collections
            return null;
        }

        try {
            return new CustomReportInfo(requestDocument.getDocumentElement(),
                    getDepth(DEPTH_0));
        } catch (DavException e) {
            throw new CosmoDavException(e);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (!bufferRequestContent) {
            return super.getInputStream();
        }

        BufferedServletInputStream is = new BufferedServletInputStream(
                super.getInputStream());
        bufferedContentLength = is.getLength();

        long contentLength = getContentLength();
        if (contentLength != -1 && contentLength != bufferedContentLength) {
            throw new IOException("Read only " + bufferedContentLength + " of "
                    + contentLength + " bytes");
        }

        return is;
    }

    @Override
    public boolean authenticate(HttpServletResponse response)
            throws IOException, ServletException {
        return originalHttpServletRequest.authenticate(response);
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return originalHttpServletRequest.getPart(name);
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return originalHttpServletRequest.getParts();
    }

    @Override
    public void login(String username, String password) throws ServletException {
        originalHttpServletRequest.login(username, password);
    }

    @Override
    public void logout() throws ServletException {
        originalHttpServletRequest.logout();
    }

    @Override
    public AsyncContext getAsyncContext() {
        return originalHttpServletRequest.getAsyncContext();
    }

    @Override
    public DispatcherType getDispatcherType() {
        return originalHttpServletRequest.getDispatcherType();
    }

    @Override
    public ServletContext getServletContext() {
        return originalHttpServletRequest.getServletContext();
    }

    @Override
    public boolean isAsyncStarted() {
        return originalHttpServletRequest.isAsyncStarted();
    }

    @Override
    public boolean isAsyncSupported() {
        return originalHttpServletRequest.isAsyncSupported();
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return originalHttpServletRequest.startAsync();
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest,
            ServletResponse servletResponse) throws IllegalStateException {
        return originalHttpServletRequest.startAsync(servletRequest,
                servletResponse);
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }
    
    
}
