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
package org.unitedinternet.cosmo.dav.servlet;

import carldav.service.generator.IdGenerator;
import org.apache.abdera.util.EntityTag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocatorFactory;
import org.unitedinternet.cosmo.dav.DavResponse;
import org.unitedinternet.cosmo.dav.MethodNotAllowedException;
import org.unitedinternet.cosmo.dav.NotModifiedException;
import org.unitedinternet.cosmo.dav.PreconditionFailedException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.impl.DavCalendarCollection;
import org.unitedinternet.cosmo.dav.impl.DavCalendarResource;
import org.unitedinternet.cosmo.dav.impl.DavCardCollection;
import org.unitedinternet.cosmo.dav.impl.DavCollectionBase;
import org.unitedinternet.cosmo.dav.impl.StandardDavRequest;
import org.unitedinternet.cosmo.dav.impl.StandardDavResponse;
import org.unitedinternet.cosmo.dav.provider.CalendarResourceProvider;
import org.unitedinternet.cosmo.dav.provider.CollectionProvider;
import org.unitedinternet.cosmo.dav.provider.DavProvider;
import org.unitedinternet.cosmo.dav.provider.FileProvider;
import org.unitedinternet.cosmo.server.ServerConstants;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * An implementation of the Spring {@link HttpRequestHandler} that
 * services WebDAV requests. Finds the resource being acted upon, checks that
 * conditions are right for the request and resource, chooses a provider
 * based on the resource type, and then delegates to a specific provider
 * method based on the request method.
 * </p>
 */
public class StandardRequestHandler extends AbstractController implements ServerConstants {

    private static final Log LOG = LogFactory.getLog(StandardRequestHandler.class);

    private final DavResourceLocatorFactory locatorFactory;
    private final DavResourceFactory resourceFactory;
    private final IdGenerator idGenerator;

    public StandardRequestHandler(final DavResourceLocatorFactory locatorFactory, final DavResourceFactory resourceFactory, final IdGenerator idGenerator) {
        super.setSupportedMethods(null);
        Assert.notNull(locatorFactory, "locatorFactory is null");
        Assert.notNull(resourceFactory, "resourceFactory is null");
        Assert.notNull(idGenerator, "idGenerator is null");
        this.locatorFactory = locatorFactory;
        this.resourceFactory = resourceFactory;
        this.idGenerator = idGenerator;
    }
    /**
     * <p>
     * Processes the request and returns a response. Calls
     * {@link DavResourceFactory.createResource(DavResourceLocator, DavRequest, DavResponse)}
     * to find the targeted resource. Calls {@link #preconditions(DavRequest, DavResponse, WebDavResource)}
     * to verify preconditions. Calls {@link #process(DavRequest, DavResponse, WebDavResource)}
     * to execute the verified request.
     * </p>
     * <p>
     * Invalid preconditions and processing exceptions are handled by
     * sending a response with the appropriate error status and message and
     * an entity describing the error.
     * </p>
     */
    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final DavRequest wreq = createDavRequest(request);
        final DavResponse wres = createDavResponse(response);

        try {
            final WebDavResource resource = resolveTarget(wreq);
            preconditions(wreq, wres, resource);
            process(wreq, wres, resource);
        } catch (Exception e) {
            CosmoDavException de = ExceptionMapper.map(e, request);

            // We need a way to differentiate exceptions that are "expected" so that the
            // logs don't get too polluted with errors.  For example, OptimisticLockingFailureException
            // is expected and should be handled by the retry logic that is one layer above.
            // Although not ideal, for now simply check for this type and log at a different level.
            LOG.error("error (" + de.getErrorCode() + "): " + de.getMessage(), de);

            wres.sendDavError(de);
        }
        return null;
    }

    // our methods

    /**
     * <p>
     * Validates preconditions that must exist before the request may be
     * executed. If a precondition is specified but is not met, the
     * appropriate response is set and <code>false</code> is returned.
     * </p>
     * <p>
     * These preconditions are checked:
     * </p>
     * <ul>
     * <li>The <code>If-Match</code> request header</li>
     * <li>The <code>If-None-Match</code> request header</li>
     * <li>The <code>If-Modified-Since</code> request header</li>
     * <li>The <code>If-Unmodified-Since</code> request header</li>
     * </ul>
     */
    protected void preconditions(DavRequest request, DavResponse response, WebDavResource resource) throws CosmoDavException, IOException {
        ifMatch(request, response, resource);
        ifNoneMatch(request, response, resource);
        ifModifiedSince(request, resource);
        ifUnmodifiedSince(request, resource);
    }

    /**
     * <p>
     * Hands the request off to a provider method for handling. The provider
     * is created by calling {@link #createProvider(WebDavResource)}. The
     * specific provider method is chosen by examining the request method.
     * </p>
     */
    protected void process(DavRequest request, DavResponse response, WebDavResource resource) throws IOException, CosmoDavException {
        DavProvider provider = createProvider(resource);

        if (request.getMethod().equals("OPTIONS")) {
            options(response, resource);
        }
        else if (request.getMethod().equals("GET")) {
            provider.get(request, response, resource);
        }
        else if (request.getMethod().equals("HEAD")) {
            provider.head(request, response, resource);
        }
        else if (request.getMethod().equals("PROPFIND")) {
            provider.propfind(request, response, resource);
        }
        else if (request.getMethod().equals("DELETE")) {
            provider.delete(request, response, resource);
        }
        else if (request.getMethod().equals("REPORT")) {
            provider.report(request, response, resource);
        }
        else {
            if (resource.isCollection()) {
                throw new MethodNotAllowedException(request.getMethod() + " not allowed for a collection");
            } else {
                if (request.getMethod().equals("PUT")) {
                    provider.put(request, response, (DavContent)resource);
                }
                else {
                    throw new MethodNotAllowedException(request.getMethod() + " not allowed for a non-collection resource");
                }
            }
        }
    }

    /**
     * <p>
     * Creates an instance of @{link Provider}. The specific provider class
     * is chosen based on the type of resource:
     * </p>
     * <ul>
     * <li> calendar collection: {@link CollectionProvider}</li>
     * <li> collection: {@link CollectionProvider}</li>
     * <li> calendar resource: {@link CalendarResourceProvider}</li>
     * <li> file resource: {@link FileProvider}</li>
     * </ul>
     */
    protected DavProvider createProvider(WebDavResource resource) {
        if (resource instanceof DavCalendarCollection) {
            return new CollectionProvider(resourceFactory, idGenerator);
        }
        if (resource instanceof DavCardCollection) {
            return new CollectionProvider(resourceFactory, idGenerator);
        }
        if (resource instanceof DavCollectionBase) {
            return new CollectionProvider(resourceFactory, idGenerator);
        }
        if (resource instanceof DavCalendarResource) {
            return new CalendarResourceProvider(resourceFactory, idGenerator);
        }
        return new FileProvider(resourceFactory, idGenerator);
    }

    /**
     * <p>
     * Creates an instance of <code>DavRequest</code> based on the
     * provided <code>HttpServletRequest</code>.
     * </p>
     */
    protected DavRequest createDavRequest(HttpServletRequest request) {
        // Create buffered request if method is PUT so we can retry
        // on concurrency exceptions
        if (request.getMethod().equals("PUT")) {
            return new StandardDavRequest(request, locatorFactory, true);
        }
        else {
            return new StandardDavRequest(request, locatorFactory);
        }
    }

    /**
     * <p>
     * Creates an instance of <code>DavResponse</code> based on the
     * provided <code>HttpServletResponse</code>.
     * </p>
     */
    protected DavResponse createDavResponse(HttpServletResponse response) {
        return new StandardDavResponse(response);
    }

    /**
     * <p>
     * Creates an instance of <code>WebDavResource</code> representing the
     * resource targeted by the request.
     * </p>
     */
    protected WebDavResource resolveTarget(DavRequest request) throws CosmoDavException {
        return resourceFactory.resolve(request.getResourceLocator(), request);
    }

    private void ifMatch(DavRequest request, DavResponse response, WebDavResource resource) throws CosmoDavException, IOException {
        EntityTag[] requestEtags = request.getIfMatch();
        if (requestEtags.length == 0) {
            return;
        }

        EntityTag resourceEtag = etag(resource);
        if (resourceEtag == null) {
            return;
        }

        if (EntityTag.matchesAny(resourceEtag, requestEtags)) {
            return;
        }

        response.setHeader("ETag", resourceEtag.toString());

        throw new PreconditionFailedException("If-Match disallows conditional request");
    }

    private void ifNoneMatch(DavRequest request, DavResponse response, WebDavResource resource) throws CosmoDavException, IOException {
        EntityTag[] requestEtags = request.getIfNoneMatch();
        if (requestEtags.length == 0) {
            return;
        }

        EntityTag resourceEtag = etag(resource);
        if (resourceEtag == null) {
            return;
        }

        if (! EntityTag.matchesAny(resourceEtag, requestEtags)) {
            return;
        }

        response.addHeader("ETag", resourceEtag.toString());

        if (deservesNotModified(request)) {
            throw new NotModifiedException();
        }

        throw new PreconditionFailedException("If-None-Match disallows conditional request");
    }

    private void ifModifiedSince(DavRequest request, WebDavResource resource) throws CosmoDavException, IOException {
        long mod = resource.getModificationTime();
        if (mod == -1) {
            return;
        }
        mod = mod / 1000 * 1000;

        long since = request.getDateHeader("If-Modified-Since");
        if (since == -1) {
            return;
        }

        if (mod > since) {
            return;
        }

        throw new NotModifiedException();
    }

    private void ifUnmodifiedSince(DavRequest request, WebDavResource resource) throws CosmoDavException, IOException {

        long mod = resource.getModificationTime();
        if (mod == -1) {
            return;
        }
        mod = mod / 1000 * 1000;

        long since = request.getDateHeader("If-Unmodified-Since");
        if (since == -1) {
            return;
        }

        if (mod <= since) {
            return;
        }

        throw new PreconditionFailedException("If-Unmodified-Since disallows conditional request");
    }

    private EntityTag etag(WebDavResource resource) {
        String etag = resource.getETag();
        if (etag == null) {
            return null;
        }
        //TODO resource etags have doublequotes wrapped around them
        //if (etag.startsWith("\"")) {
        etag = etag.substring(1, etag.length()-1);
        //}
        return new EntityTag(etag);
    }

    private boolean deservesNotModified(DavRequest request) {
        return "GET".equals(request.getMethod()) || "HEAD".equals(request.getMethod());
    }

    private void options(DavResponse response, WebDavResource resource) {
        response.setStatus(200);
        response.addHeader("Allow", resource.getSupportedMethods());
        response.addHeader("DAV", resource.getComplianceClass());
    }

    @Override
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        checkRequest(request);
        prepareResponse(response);
        return handleRequestInternal(request, response);
    }
}
