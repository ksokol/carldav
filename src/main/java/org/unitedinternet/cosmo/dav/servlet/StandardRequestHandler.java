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

import org.apache.abdera.util.EntityTag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.util.Assert;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocatorFactory;
import org.unitedinternet.cosmo.dav.DavResponse;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.MethodNotAllowedException;
import org.unitedinternet.cosmo.dav.NotModifiedException;
import org.unitedinternet.cosmo.dav.PreconditionFailedException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.acl.NeedsPrivilegesException;
import org.unitedinternet.cosmo.dav.acl.resource.DavUserPrincipal;
import org.unitedinternet.cosmo.dav.acl.resource.DavUserPrincipalCollection;
import org.unitedinternet.cosmo.dav.caldav.CaldavExceptionForbidden;
import org.unitedinternet.cosmo.dav.impl.DavCalendarCollection;
import org.unitedinternet.cosmo.dav.impl.DavCalendarResource;
import org.unitedinternet.cosmo.dav.impl.DavCollectionBase;
import org.unitedinternet.cosmo.dav.impl.DavHomeCollection;
import org.unitedinternet.cosmo.dav.impl.DavOutboxCollection;
import org.unitedinternet.cosmo.dav.impl.StandardDavRequest;
import org.unitedinternet.cosmo.dav.impl.StandardDavResponse;
import org.unitedinternet.cosmo.dav.provider.CalendarCollectionProvider;
import org.unitedinternet.cosmo.dav.provider.CalendarResourceProvider;
import org.unitedinternet.cosmo.dav.provider.CollectionProvider;
import org.unitedinternet.cosmo.dav.provider.DavProvider;
import org.unitedinternet.cosmo.dav.provider.FileProvider;
import org.unitedinternet.cosmo.dav.provider.HomeCollectionProvider;
import org.unitedinternet.cosmo.dav.provider.OutboxCollectionProvider;
import org.unitedinternet.cosmo.dav.provider.UserPrincipalCollectionProvider;
import org.unitedinternet.cosmo.dav.provider.UserPrincipalProvider;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.security.CosmoSecurityException;
import org.unitedinternet.cosmo.security.ItemSecurityException;
import org.unitedinternet.cosmo.security.Permission;
import org.unitedinternet.cosmo.server.ServerConstants;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;

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
    private final EntityFactory entityFactory;

    public StandardRequestHandler(final DavResourceLocatorFactory locatorFactory, final DavResourceFactory resourceFactory, final EntityFactory entityFactory) {
        super.setSupportedMethods(null);
        Assert.notNull(locatorFactory, "locatorFactory is null");
        Assert.notNull(resourceFactory, "resourceFactory is null");
        Assert.notNull(entityFactory, "entityFactory is null");
        this.locatorFactory = locatorFactory;
        this.resourceFactory = resourceFactory;
        this.entityFactory = entityFactory;
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
        DavRequest wreq;
        DavResponse wres = null;
        
        try {
            wreq = createDavRequest(request);
            wres = createDavResponse(response);

            WebDavResource resource = resolveTarget(wreq);
            preconditions(wreq, wres, resource);
            process(wreq, wres, resource);
        } catch (Exception e) {
            CosmoDavException de = ExceptionMapper.map(e, request);
            
            // We need a way to differentiate exceptions that are "expected" so that the
            // logs don't get too polluted with errors.  For example, OptimisticLockingFailureException
            // is expected and should be handled by the retry logic that is one layer above.
            // Although not ideal, for now simply check for this type and log at a different level.
            if(e instanceof OptimisticLockingFailureException) {
                LOG.error("OptimisticLockingFailureException - Internal dav error", e);
            }
            else if (de.getErrorCode() >= 500) {
                LOG.error("Internal dav error", e);
            }
            else if (de.getErrorCode() >= 400 && de.getMessage() != null) {
                LOG.error("Client error (" + de.getErrorCode() + "): " + de.getMessage(), de);
            }
            if (wres != null) {
                wres.sendDavError(de);
            }
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
    protected void preconditions(DavRequest request,
                                 DavResponse response,
                                 WebDavResource resource)
        throws CosmoDavException, IOException {
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
    protected void process(DavRequest request,
                           DavResponse response,
                           WebDavResource resource)
        throws IOException, CosmoDavException {
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
        else if (request.getMethod().equals("POST")) {
            provider.post(request, response, resource);
        }
        else if (request.getMethod().equals("PROPFIND")) {
            provider.propfind(request, response, resource);
        }
        else if (request.getMethod().equals("PROPPATCH")) {
            provider.proppatch(request, response, resource);
        }
        else if (request.getMethod().equals("DELETE")) {
            provider.delete(request, response, resource);
        }
        else if (request.getMethod().equals("COPY")) {
            provider.copy(request, response, resource);
        }
        else if (request.getMethod().equals("MOVE")) {
            provider.move(request, response, resource);
        }
        else if (request.getMethod().equals("REPORT")) {
            provider.report(request, response, resource);
        }
        else if (request.getMethod().equals("MKTICKET")) {
            provider.mkticket(request, response, resource);
        }
        else if (request.getMethod().equals("DELTICKET")) {
            provider.delticket(request, response, resource);
        }
        else if (request.getMethod().equals("ACL")) {
            provider.acl(request, response, resource);
        }
        else {
            if (resource.isCollection()) {
                if (request.getMethod().equals("MKCOL")) {
                    provider.mkcol(request, response,
                                   (DavCollection)resource);
                }
                else if (request.getMethod().equals("MKCALENDAR")) {
                    provider.mkcalendar(request, response,
                                        (DavCollection)resource);
                }
                else {
                    throw new MethodNotAllowedException(request.getMethod() + " not allowed for a collection");
                }
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
     * <li> home collection: {@link HomeCollectionProvider}</li>
     * <li> calendar collection: {@link CalendarCollectionProvider}</li>
     * <li> collection: {@link CollectionProvider}</li>
     * <li> calendar resource: {@link CalendarResourceProvider}</li>
     * <li> file resource: {@link FileProvider}</li>
     * </ul>
     */
    protected DavProvider createProvider(WebDavResource resource) {
        if (resource instanceof DavHomeCollection) {
            return new HomeCollectionProvider(resourceFactory, entityFactory);
        }
        if (resource instanceof DavOutboxCollection) {
            return new OutboxCollectionProvider(resourceFactory, entityFactory);
        }
        if (resource instanceof DavCalendarCollection) {
            return new CalendarCollectionProvider(resourceFactory, entityFactory);
        }
        if (resource instanceof DavCollectionBase) {
            return new CollectionProvider(resourceFactory, entityFactory);
        }
        if (resource instanceof DavCalendarResource) {
            return new CalendarResourceProvider(resourceFactory, entityFactory);
        }
        if (resource instanceof DavUserPrincipalCollection) {
            return new UserPrincipalCollectionProvider(resourceFactory, entityFactory);
        }
        if (resource instanceof DavUserPrincipal) {
            return new UserPrincipalProvider(resourceFactory, entityFactory);
        }
        return new FileProvider(resourceFactory, entityFactory);
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
            return new StandardDavRequest(request, locatorFactory, entityFactory, true);
        }
        else {
            return new StandardDavRequest(request, locatorFactory, entityFactory);
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
    protected WebDavResource resolveTarget(DavRequest request)
        throws CosmoDavException {
        return resourceFactory.resolve(request.getResourceLocator(), request);
    }

    private void ifMatch(DavRequest request,
                         DavResponse response,
                         WebDavResource resource)
        throws CosmoDavException, IOException {
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

    private void ifNoneMatch(DavRequest request,
                             DavResponse response,
                             WebDavResource resource)
        throws CosmoDavException, IOException {
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
        else {
            throw new PreconditionFailedException("If-None-Match disallows conditional request");
        }
    }

    private void ifModifiedSince(DavRequest request,
                                 WebDavResource resource)
        throws CosmoDavException, IOException {
        if (resource == null) {
            return;
        }

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

    private void ifUnmodifiedSince(DavRequest request,
                                   WebDavResource resource)
        throws CosmoDavException, IOException {
        if (resource == null) {
            return;
        }

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
        if (resource == null) {
            return null;
        }
        String etag = resource.getETag();
        if (etag == null) {
            return null;
        }
        // resource etags have doublequotes wrapped around them
        if (etag.startsWith("\"")) {
            etag = etag.substring(1, etag.length()-1);
        }
        return new EntityTag(etag);
    }

    private boolean deservesNotModified(DavRequest request) {
        return "GET".equals(request.getMethod()) || "HEAD".equals(request.getMethod());
    }

    private void options(DavResponse response,
                         WebDavResource resource) {
        response.setStatus(200);
        response.addHeader("Allow", resource.getSupportedMethods());
        response.addHeader("DAV", resource.getComplianceClass());
    }

    private static enum ExceptionMapper {
        SECURITY_EXCEPTION_MAPPER (CosmoSecurityException.class) {
            @Override
            protected CosmoDavException doMap(Throwable t, HttpServletRequest request) {
                // handle security errors
                NeedsPrivilegesException npe = null;
                // Determine required privilege if we can and include
                // in response
                if(t instanceof ItemSecurityException) {
                    ItemSecurityException ise = (ItemSecurityException) t;
                    DavPrivilege priv = ise.getPermission()==Permission.READ ? DavPrivilege.READ : DavPrivilege.WRITE;
                    npe = new NeedsPrivilegesException(request.getRequestURI(), priv);
                } else {
                    // otherwise send generic response
                    npe = new NeedsPrivilegesException(t.getMessage());
                }

                return npe;
            }
        },        
        FORBIDDEN_EXCEPTION_MAPPER(CaldavExceptionForbidden.class),        
        DAV_EXCEPTION_MAPPER (CosmoDavException.class){
            @Override
            protected CosmoDavException doMap(Throwable t, HttpServletRequest request) {
                CosmoDavException de = (CosmoDavException)t;
                return de;
            }
        },
        VALIDATION_EXCEPTION_MAPPER (ValidationException.class);
        
        private Class<? extends Throwable> exceptionRoot;
        
        private <T extends Throwable> ExceptionMapper (Class<T> exceptionRoot){
            this.exceptionRoot = exceptionRoot;
        }
        
        boolean supports(Throwable t){
            return exceptionRoot.isInstance(t);
        }
        
        
        //Default behavior. See http://tools.ietf.org/search/rfc4791#section-1.3
        CosmoDavException doMap(Throwable t, HttpServletRequest request){
            return new ForbiddenException(t.getMessage());
        }
        
        public static CosmoDavException map(Throwable t, HttpServletRequest request){
            
            for(ExceptionMapper mapper : values()){
                if(mapper.supports(t)){
                    return mapper.doMap(t, request);
                }
            }
            
            request.setAttribute(ATTR_SERVICE_EXCEPTION, t);
            return new CosmoDavException(t);
        }
    }
}
