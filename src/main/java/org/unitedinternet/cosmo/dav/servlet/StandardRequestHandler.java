package org.unitedinternet.cosmo.dav.servlet;

import carldav.exception.resolver.ExceptionResolverHandler;
import carldav.exception.resolver.ResponseUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.abdera.util.EntityTag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocatorFactory;
import org.unitedinternet.cosmo.dav.MethodNotAllowedException;
import org.unitedinternet.cosmo.dav.NotModifiedException;
import org.unitedinternet.cosmo.dav.PreconditionFailedException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.impl.DavCalendarResource;
import org.unitedinternet.cosmo.dav.provider.BaseProvider;
import org.unitedinternet.cosmo.dav.provider.CalendarResourceProvider;
import org.unitedinternet.cosmo.server.ServerConstants;

import java.io.IOException;

@Transactional
public class StandardRequestHandler extends AbstractController implements ServerConstants {

  private static final Log LOG = LogFactory.getLog(StandardRequestHandler.class);

  private final DavResourceLocatorFactory locatorFactory;
  private final DavResourceFactory resourceFactory;
  private final ExceptionResolverHandler exceptionResolverHandler;

  public StandardRequestHandler(DavResourceLocatorFactory locatorFactory, DavResourceFactory resourceFactory, ExceptionResolverHandler exceptionResolverHandler) {
    super.setSupportedMethods(null);
    Assert.notNull(locatorFactory, "locatorFactory is null");
    Assert.notNull(locatorFactory, "locatorFactory is null");
    Assert.notNull(resourceFactory, "resourceFactory is null");
    Assert.notNull(exceptionResolverHandler, "exceptionResolverHandler is null");
    this.locatorFactory = locatorFactory;
    this.resourceFactory = resourceFactory;
    this.exceptionResolverHandler = exceptionResolverHandler;
  }

  @Override
  protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) {
    try {
      var resource = resolveTarget(request);
      preconditions(request, response, resource);
      process(request, response, resource);
    } catch (Exception e) {
      var de = exceptionResolverHandler.resolve(e);

      // We need a way to differentiate exceptions that are "expected" so that the
      // logs don't get too polluted with errors.  For example, OptimisticLockingFailureException
      // is expected and should be handled by the retry logic that is one layer above.
      // Although not ideal, for now simply check for this type and log at a different level.
      LOG.error("error (" + de.getErrorCode() + "): " + de.getMessage(), de);

      ResponseUtils.sendDavError(de, response);
    }
    return null;
  }

  protected void preconditions(HttpServletRequest request, HttpServletResponse response, WebDavResource resource) throws CosmoDavException {
    ifMatch(request, response, resource);
    ifNoneMatch(request, response, resource);
    ifModifiedSince(request, resource);
    ifUnmodifiedSince(request, resource);
  }

  protected void process(HttpServletRequest request, HttpServletResponse response, WebDavResource resource) throws IOException, CosmoDavException {
    var provider = createProvider(resource);

    if (request.getMethod().equals("OPTIONS")) {
      options(response, resource);
    } else if (request.getMethod().equals("GET")) {
      provider.get(request, response, resource);
    } else if (request.getMethod().equals("HEAD")) {
      provider.head(request, response, resource);
    } else if (request.getMethod().equals("PROPFIND")) {
      provider.propfind(request, response, resource);
    } else if (request.getMethod().equals("DELETE")) {
      provider.delete(request, response, resource);
    } else if (request.getMethod().equals("REPORT")) {
      provider.report(request, response, resource);
    } else {
      if (resource.isCollection()) {
        throw new MethodNotAllowedException(request.getMethod() + " not allowed for a collection");
      } else {
        if (request.getMethod().equals("PUT")) {
          provider.put(request, response, resource);
        } else {
          throw new MethodNotAllowedException(request.getMethod() + " not allowed for a non-collection resource");
        }
      }
    }
  }

  protected BaseProvider createProvider(WebDavResource resource) {
    if (resource instanceof DavCalendarResource) {
      return new CalendarResourceProvider(resourceFactory);
    }
    return new BaseProvider(resourceFactory);
  }

  protected WebDavResource resolveTarget(HttpServletRequest request) throws CosmoDavException {
    return resourceFactory.resolve(locatorFactory.createResourceLocatorFromRequest(request), request);
  }

  private void ifMatch(HttpServletRequest request, HttpServletResponse response, WebDavResource resource) throws CosmoDavException {
    var requestEtags = EntityTag.parseTags(request.getHeader("If-Match"));
    if (requestEtags.length == 0) {
      return;
    }

    var resourceEtag = etag(resource);
    if (resourceEtag == null) {
      return;
    }

    if (EntityTag.matchesAny(resourceEtag, requestEtags)) {
      return;
    }

    response.setHeader("ETag", resourceEtag.toString());

    throw new PreconditionFailedException("If-Match disallows conditional request");
  }

  private void ifNoneMatch(HttpServletRequest request, HttpServletResponse response, WebDavResource resource) throws CosmoDavException {
    var requestEtags = EntityTag.parseTags(request.getHeader("If-None-Match"));
    if (requestEtags.length == 0) {
      return;
    }

    var resourceEtag = etag(resource);
    if (resourceEtag == null) {
      return;
    }

    if (!EntityTag.matchesAny(resourceEtag, requestEtags)) {
      return;
    }

    response.addHeader("ETag", resourceEtag.toString());

    if (deservesNotModified(request)) {
      throw new NotModifiedException();
    }

    throw new PreconditionFailedException("If-None-Match disallows conditional request");
  }

  private void ifModifiedSince(HttpServletRequest request, WebDavResource resource) throws CosmoDavException {
    var mod = resource.getModificationTime();
    if (mod == -1) {
      return;
    }
    mod = mod / 1000 * 1000;

    var since = request.getDateHeader("If-Modified-Since");
    if (since == -1) {
      return;
    }

    if (mod > since) {
      return;
    }

    throw new NotModifiedException();
  }

  private void ifUnmodifiedSince(HttpServletRequest request, WebDavResource resource) throws CosmoDavException {
    var mod = resource.getModificationTime();
    if (mod == -1) {
      return;
    }
    mod = mod / 1000 * 1000;

    var since = request.getDateHeader("If-Unmodified-Since");
    if (since == -1) {
      return;
    }

    if (mod <= since) {
      return;
    }

    throw new PreconditionFailedException("If-Unmodified-Since disallows conditional request");
  }

  private EntityTag etag(WebDavResource resource) {
    var etag = resource.getETag();
    if (etag == null) {
      return null;
    }
    //TODO resource etags have doublequotes wrapped around them
    //if (etag.startsWith("\"")) {
    etag = etag.substring(1, etag.length() - 1);
    //}
    return new EntityTag(etag);
  }

  private boolean deservesNotModified(HttpServletRequest request) {
    return "GET".equals(request.getMethod()) || "HEAD".equals(request.getMethod());
  }

  private void options(HttpServletResponse response, WebDavResource resource) {
    response.setStatus(200);
    response.addHeader("Allow", resource.getSupportedMethods());
    response.addHeader("DAV", resource.getComplianceClass());
  }

  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    checkRequest(request);
    prepareResponse(response);
    return handleRequestInternal(request, response);
  }
}
