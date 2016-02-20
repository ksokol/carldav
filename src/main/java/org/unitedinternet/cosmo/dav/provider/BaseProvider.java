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
package org.unitedinternet.cosmo.dav.provider;

import carldav.jackrabbit.webdav.CustomMultiStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.io.OutputContextImpl;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.ContentLengthRequiredException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResponse;
import org.unitedinternet.cosmo.dav.NotFoundException;
import org.unitedinternet.cosmo.dav.UnsupportedMediaTypeException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.io.DavInputContext;
import org.unitedinternet.cosmo.dav.report.ReportBase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>
 * A base class for implementations of <code>DavProvider</code>.
 * </p>
 *
 * @see DavProvider
 */
public abstract class BaseProvider implements DavProvider, DavConstants {

    private static final Log LOG = LogFactory.getLog(BaseProvider.class);

    private DavResourceFactory resourceFactory;
    public BaseProvider(DavResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }

    // DavProvider methods
    /**
     * 
     * {@inheritDoc}
     */
    public void get(DavRequest request,
                    DavResponse response,
                    WebDavResource resource)
        throws CosmoDavException, IOException {
        spool(request, response, resource, true);
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    public void head(DavRequest request,
                     DavResponse response,
                     WebDavResource resource)
        throws CosmoDavException, IOException {
        spool(request, response, resource, false);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void propfind(DavRequest request,
                         DavResponse response,
                         WebDavResource resource)
        throws CosmoDavException, IOException {
        if (! resource.exists()){
            throw new NotFoundException();
        }
        int depth = getDepth(request);
        if (depth != DEPTH_0 && ! resource.isCollection()){
            throw new BadRequestException("Depth must be 0 for non-collection resources");
        }

        DavPropertyNameSet props;
        int type;
        
        try{
            
            props = request.getPropFindProperties();
            
            type =request.getPropFindType();
            
        }catch(DavException de){
            throw new CosmoDavException(de);
        }

        MultiStatus ms = new CustomMultiStatus();
        ms.addResourceProperties(resource, props, type, depth);
        
        response.sendMultiStatus(ms);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void delete(DavRequest request,
                       DavResponse response,
                       WebDavResource resource)
        throws CosmoDavException, IOException {
        if (! resource.exists()){
            response.setStatus(204);
            return;
        }
        checkNoRequestBody(request);

        int depth = getDepth(request);
        if (depth != DEPTH_INFINITY){
            throw new BadRequestException("Depth for DELETE must be infinity");
        }

        try {
            resource.getParent().removeMember(resource);
            response.setStatus(204);
        } catch (DavException e) {
            throw new CosmoDavException(e);
        }
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void report(DavRequest request,
                       DavResponse response,
                       WebDavResource resource)
        throws CosmoDavException, IOException {
        if (! resource.exists()){
            throw new NotFoundException();
        }
        try {
            ReportInfo info = request.getReportInfo();
            if (info == null){
                if(resource.isCollection()){
                    return;
                } else {
                    throw new BadRequestException("REPORT requires entity body");
                }
            }

            ((ReportBase) resource.getReport(info)).run(response);
        } catch (DavException e) {
            if (e instanceof CosmoDavException){
                throw (CosmoDavException) e;
            }
            throw new CosmoDavException(e);
        }
    }

    // our methods
    /**
     * 
     * @param request 
     * @param response 
     * @param resource 
     * @param withEntity 
     * @throws CosmoDavException 
     * @throws IOException 
     */
    protected void spool(DavRequest request,
                         DavResponse response,
                         WebDavResource resource,
                         boolean withEntity)
        throws CosmoDavException, IOException {
        if (! resource.exists()){
            throw new NotFoundException();
        }
        checkNoRequestBody(request);

        if (LOG.isDebugEnabled()){
            LOG.debug("spooling resource " + resource.getResourcePath());
        }
        resource.writeTo(createOutputContext(response, withEntity));
        response.flushBuffer();
    }
    /**
     * 
     * @param request 
     * @return InputContext 
     * @throws CosmoDavException 
     * @throws IOException 
     */
    protected InputContext createInputContext(DavRequest request)
        throws CosmoDavException, IOException {
        String xfer = request.getHeader("Transfer-Encoding");
        boolean chunked = xfer != null && xfer.equals("chunked");
        if (xfer != null && ! chunked){
            throw new BadRequestException("Unknown Transfer-Encoding " + xfer);
        }
        if (chunked && request.getContentLength() <= 0){
            throw new ContentLengthRequiredException();
        }

        InputStream in = request.getContentLength() > 0 || chunked ?
            request.getInputStream() : null;
        return new DavInputContext(request, in);
    }
    
    /**
     * 
     * @param response DavResponse
     * @param withEntity boolean 
     * @return OutputContext
     * @throws IOException 
     */
    protected OutputContext createOutputContext(DavResponse response,
                                                boolean withEntity)
        throws IOException {
        OutputStream out = withEntity ? response.getOutputStream() : null;
        return new OutputContextImpl(response, out);
    }

    /**
     * 
     * @param request DavRequest
     * @throws CosmoDavException 
     */
    protected void checkNoRequestBody(DavRequest request) throws CosmoDavException {
        boolean hasBody;
        try {
            hasBody = request.getRequestDocument() != null;
        } catch (IllegalArgumentException e) {
            // parse error indicates that there was a body to parse
            hasBody = true;
        } catch (DavException e) {
            throw new CosmoDavException(e);
        }
        
        if (hasBody){
            throw new UnsupportedMediaTypeException("Body not expected for method " + request.getMethod());
        }
    }

    /**
     * 
     * @param request DavRequest
     * @return depth
     * @throws CosmoDavException 
     */
    protected int getDepth(DavRequest request)
        throws CosmoDavException {
        try {
            return request.getDepth();
        } catch (IllegalArgumentException e) {    
            throw new BadRequestException(e.getMessage());
        }
    }

    public DavResourceFactory getResourceFactory() {
        return resourceFactory;
    }
}