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

import org.apache.jackrabbit.webdav.WebdavRequestImpl;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.util.BufferedServletInputStream;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

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

    private boolean bufferRequestContent = false;
    private long bufferedContentLength = -1;
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
    public StandardDavRequest(HttpServletRequest request) {
        this(request, false);
    }

    /**
     * 
     * @param request HttpServletRequest
     * @param bufferRequestContent boolean
     */
    public StandardDavRequest(HttpServletRequest request,
                              boolean bufferRequestContent) {
        super(request, null);
        originalHttpServletRequest = request;
        this.bufferRequestContent = bufferRequestContent;
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
