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

import static org.unitedinternet.cosmo.dav.ExtendedDavConstants.QN_PROPFIND;

import carldav.jackrabbit.webdav.CustomMultiStatus;
import carldav.jackrabbit.webdav.CustomReportInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.WebdavResponse;
import org.apache.jackrabbit.webdav.header.DepthHeader;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.io.OutputContextImpl;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.springframework.http.MediaType;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.ContentLengthRequiredException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.NotFoundException;
import org.unitedinternet.cosmo.dav.UnsupportedMediaTypeException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.io.DavInputContext;
import org.unitedinternet.cosmo.dav.report.ReportBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

/**
 * <p>
 * A base class for implementations of <code>DavProvider</code>.
 * </p>
 *
 * @see DavProvider
 */
public abstract class BaseProvider implements DavProvider, DavConstants {

    private static final MediaType APPLICATION_XML = MediaType.APPLICATION_XML;
    private static final MediaType TEXT_XML = MediaType.TEXT_XML;

    private DavResourceFactory resourceFactory;
    private int propfindType = PROPFIND_ALL_PROP;
    private DavPropertyNameSet propfindProps;
    private ReportInfo reportInfo;

    public BaseProvider(DavResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }

    // DavProvider methods
    /**
     * 
     * {@inheritDoc}
     */
    public void get(DavRequest request,
                    WebdavResponse response,
                    WebDavResource resource)
        throws CosmoDavException, IOException {
        spool(request, response, resource, true);
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    public void head(DavRequest request,
                     WebdavResponse response,
                     WebDavResource resource)
        throws CosmoDavException, IOException {
        spool(request, response, resource, false);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void propfind(DavRequest request,
                         WebdavResponse response,
                         WebDavResource resource)
        throws CosmoDavException, IOException {
        if (! resource.exists()){
            throw new NotFoundException();
        }
        int depth = getDepth(request);
        if (depth != DEPTH_0 && ! resource.isCollection()){
            throw new BadRequestException("Depth must be 0 for non-collection resources");
        }

        DavPropertyNameSet props = getPropFindProperties(request);
        int type = getPropFindType(request);
        MultiStatus ms = new CustomMultiStatus();
        ms.addResourceProperties(resource, props, type, depth);
        
        response.sendMultiStatus(ms);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void delete(DavRequest request,
                       WebdavResponse response,
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
                       WebdavResponse response,
                       WebDavResource resource)
        throws CosmoDavException, IOException {
        if (! resource.exists()){
            throw new NotFoundException();
        }
        try {
            ReportInfo info = getReportInfo(request);
            if (info == null){
                if(resource.isCollection()){
                    return;
                } else {
                    throw new BadRequestException("REPORT requires entity body");
                }
            }

            ((ReportBase) resource.getReport(info)).run(response);
        } catch (CosmoDavException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new CosmoDavException(exception);
        }
    }

    protected void spool(DavRequest request,
                         WebdavResponse response,
                         WebDavResource resource,
                         boolean withEntity)
        throws CosmoDavException, IOException {
        if (! resource.exists()){
            throw new NotFoundException();
        }
        checkNoRequestBody(request);
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
    protected InputContext createInputContext(final HttpServletRequest request)
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
    protected OutputContext createOutputContext(WebdavResponse response,
                                                boolean withEntity)
        throws IOException {
        OutputStream out = withEntity ? response.getOutputStream() : null;
        return new OutputContextImpl(response, out);
    }

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

    protected int getDepth(final HttpServletRequest request) {
        return DepthHeader.parse(request, DEPTH_INFINITY).getDepth();
    }

    private ReportInfo getReportInfo(final HttpServletRequest request) throws CosmoDavException {
        if (reportInfo == null) {
            reportInfo = parseReportRequest(request);
        }
        return reportInfo;
    }

    private ReportInfo parseReportRequest(final HttpServletRequest request) throws CosmoDavException {
        Document requestDocument = getSafeRequestDocument(request);
        if (requestDocument == null) { // reports with no bodies are supported
            // for collections
            return null;
        }

        try {
            return new CustomReportInfo(requestDocument.getDocumentElement(), getDepth(request));
        } catch (DavException e) {
            throw new CosmoDavException(e);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private int getPropFindType(final HttpServletRequest request) throws CosmoDavException {
        if (propfindProps == null) {
            parsePropFindRequest(request);
        }
        return propfindType;
    }

    private DavPropertyNameSet getPropFindProperties(final HttpServletRequest request) throws CosmoDavException {
        if (propfindProps == null) {
            parsePropFindRequest(request);
        }
        return propfindProps;
    }

    private void parsePropFindRequest(final HttpServletRequest request) throws CosmoDavException {
        Document requestDocument = getSafeRequestDocument(request);

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

    private Document getSafeRequestDocument(final HttpServletRequest request)
            throws CosmoDavException {
        try {
            if (StringUtils.isBlank(request.getContentType())) {
                throw new BadRequestException("No Content-Type specified");
            }

            final MediaType mediaType = MediaType.valueOf(request.getContentType());
            if (!(mediaType.equals(APPLICATION_XML) || mediaType.equals(TEXT_XML))) {
                throw new UnsupportedMediaTypeException("Expected Content-Type " + APPLICATION_XML + " or " + TEXT_XML);
            }

            return getRequestDocument(request);

        } catch (IllegalArgumentException|DavException e) {
            throwBadRequestExceptionFrom(e);
        }

        return null;
    }

    private Document getRequestDocument(final HttpServletRequest request) throws DavException {
        Document requestDocument = null;
        /*
        Don't attempt to parse the body if the content length header is 0.
        NOTE: a value of -1 indicates that the length is unknown, thus we have
        to parse the body. Note that http1.1 request using chunked transfer
        coding will therefore not be detected here.
        */
        if (request.getContentLength() == 0) {
            return requestDocument;
        }
        // try to parse the request body
        try {
            InputStream in = request.getInputStream();
            if (in != null) {
                // use a buffered input stream to find out whether there actually
                // is a request body
                InputStream bin = new BufferedInputStream(in);
                bin.mark(1);
                boolean isEmpty = -1 == bin.read();
                bin.reset();
                if (!isEmpty) {
                    requestDocument = DomUtil.parseDocument(bin);
                }
            }
        } catch (IOException|SAXException e) {
            throw new DavException(DavServletResponse.SC_BAD_REQUEST);
        } catch (ParserConfigurationException e) {
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return requestDocument;
    }

    private void throwBadRequestExceptionFrom(Exception e)
            throws BadRequestException {
        Throwable cause = e.getCause();
        String msg = cause != null ? cause.getMessage()
                : "Unknown error parsing request document";
        throw new BadRequestException(msg);
    }

    public DavResourceFactory getResourceFactory() {
        return resourceFactory;
    }
}