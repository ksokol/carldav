package org.unitedinternet.cosmo.dav.provider;

import carldav.exception.resolver.ResponseUtils;
import carldav.jackrabbit.webdav.DavConstants;
import carldav.jackrabbit.webdav.MultiStatus;
import carldav.jackrabbit.webdav.io.DavInputContext;
import carldav.jackrabbit.webdav.property.DavPropertyName;
import carldav.jackrabbit.webdav.property.DavPropertyNameSet;
import carldav.jackrabbit.webdav.version.report.ReportInfo;
import carldav.jackrabbit.webdav.xml.DomUtils;
import carldav.jackrabbit.webdav.xml.ElementIterator;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.ConflictException;
import org.unitedinternet.cosmo.dav.ContentLengthRequiredException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.NotFoundException;
import org.unitedinternet.cosmo.dav.UnsupportedMediaTypeException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.impl.DavCollectionBase;
import org.unitedinternet.cosmo.dav.impl.DavItemResourceBase;
import org.unitedinternet.cosmo.dav.report.ReportBase;
import org.w3c.dom.Document;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;

import static carldav.CarldavConstants.caldav;

public class BaseProvider implements DavConstants {

  private static final MediaType APPLICATION_XML = MediaType.APPLICATION_XML;
  private static final MediaType TEXT_XML = MediaType.TEXT_XML;

  private final DavResourceFactory resourceFactory;
  private int propfindType = PROPFIND_ALL_PROP;
  private DavPropertyNameSet propfindProps;
  private ReportInfo reportInfo;

  public BaseProvider(DavResourceFactory resourceFactory) {
    this.resourceFactory = resourceFactory;
  }

  public void get(HttpServletRequest request, HttpServletResponse response, WebDavResource resource) throws CosmoDavException, IOException {
    spool(request, response, resource, true);
  }

  public void head(HttpServletRequest request, HttpServletResponse response, WebDavResource resource) throws CosmoDavException, IOException {
    spool(request, response, resource, false);
  }

  public void propfind(HttpServletRequest request, HttpServletResponse response, WebDavResource resource) throws CosmoDavException, IOException {
    if (!resource.exists()) {
      throw new NotFoundException();
    }
    var depth = getDepth(request);
    if (depth != DEPTH_0 && !resource.isCollection()) {
      throw new BadRequestException("Depth must be 0 for non-collection resources");
    }

    var props = getPropFindProperties(request);
    var type = getPropFindType(request);
    var ms = new MultiStatus();
    ms.addResourceProperties(resource, props, type, depth);

    ResponseUtils.sendXmlResponse(response, ms, 207);
  }

  public void put(HttpServletRequest request, HttpServletResponse response, WebDavResource content) throws CosmoDavException, IOException {
    if (!content.getParent().exists()) {
      throw new ConflictException("One or more intermediate collections must be created");
    }

    if (!(content instanceof DavItemResourceBase)) {
      throw new IllegalArgumentException("Expected type for 'content' is: [" + DavItemResourceBase.class.getName() + "]");
    }
    var status = content.exists() ? 204 : 201;
    content.getParent().addContent(content, createInputContext(request));
    response.setStatus(status);
    response.setHeader("ETag", content.getETag());
  }

  public void delete(HttpServletRequest request, HttpServletResponse response, WebDavResource resource) throws CosmoDavException, IOException {
    if (!resource.exists()) {
      response.setStatus(204);
      return;
    }
    checkNoRequestBody(request);

    var depth = getDepth(request);
    if (depth != DEPTH_INFINITY) {
      throw new BadRequestException("Depth for DELETE must be infinity");
    }

    if (resource instanceof DavCollectionBase collection) {
      collection.getParent().removeCollection(collection);
    } else if (resource instanceof DavItemResourceBase) {
      resource.getParent().removeItem(resource);
    } else {
      throw new IllegalArgumentException(String.format("Expected 'member' as instance of: [%s or %s]", DavItemResourceBase.class.getName(), DavCollectionBase.class.getName()));
    }


    response.setStatus(204);
  }

  public void report(HttpServletRequest request, HttpServletResponse response, WebDavResource resource) throws CosmoDavException, IOException {
    if (!resource.exists()) {
      throw new NotFoundException();
    }
    try {
      var info = getReportInfo(request);
      if (info == null) {
        if (resource.isCollection()) {
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

  protected void spool(HttpServletRequest request, HttpServletResponse response, WebDavResource resource, boolean withEntity) throws CosmoDavException, IOException {
    if (!resource.exists()) {
      throw new NotFoundException();
    }
    checkNoRequestBody(request);
    resource.writeHead(response);
    if (withEntity) {
      resource.writeBody(response);
    }
    response.flushBuffer();
  }

  protected DavInputContext createInputContext(final HttpServletRequest request) throws CosmoDavException, IOException {
    var xfer = request.getHeader("Transfer-Encoding");
    var chunked = xfer != null && xfer.equals("chunked");
    if (xfer != null && !chunked) {
      throw new BadRequestException("Unknown Transfer-Encoding " + xfer);
    }
    if (chunked && request.getContentLength() <= 0) {
      throw new ContentLengthRequiredException();
    }

    var in = request.getContentLength() > 0 || chunked ? request.getInputStream() : null;
    return new DavInputContext(request, in);
  }

  protected void checkNoRequestBody(HttpServletRequest request) throws CosmoDavException {
    var hasBody = getRequestDocument(request) != null;
    if (hasBody) {
      throw new UnsupportedMediaTypeException("Body not expected for method " + request.getMethod());
    }
  }

  protected int getDepth(final HttpServletRequest request) {
    return parseDepth(request);
  }

  private static int parseDepth(HttpServletRequest request) {
    var headerValue = request.getHeader(HEADER_DEPTH);
    int depth;
    if (headerValue == null || "".equals(headerValue)) {
      depth = DEPTH_INFINITY;
    } else {
      depth = depthToInt(headerValue);
    }
    if (depth == DEPTH_0 || depth == DEPTH_1 || depth == DEPTH_INFINITY) {
      return depth;
    }
    throw new IllegalArgumentException("Invalid depth: " + depth);
  }

  private static int depthToInt(String depth) {
    int d;
    if (depth.equalsIgnoreCase(DEPTH_INFINITY_S)) {
      d = DavConstants.DEPTH_INFINITY;
    } else if (depth.equals(DavConstants.DEPTH_0 + "")) {
      d = DavConstants.DEPTH_0;
    } else if (depth.equals(DavConstants.DEPTH_1 + "")) {
      d = DavConstants.DEPTH_1;
    } else {
      throw new IllegalArgumentException("Invalid depth value: " + depth);
    }
    return d;
  }

  private ReportInfo getReportInfo(final HttpServletRequest request) throws CosmoDavException {
    if (reportInfo == null) {
      reportInfo = parseReportRequest(request);
    }
    return reportInfo;
  }

  private ReportInfo parseReportRequest(final HttpServletRequest request) throws CosmoDavException {
    var requestDocument = getSafeRequestDocument(request);
    if (requestDocument == null) { // reports with no bodies are supported
      // for collections
      return null;
    }

    try {
      return new ReportInfo(requestDocument.getDocumentElement(), getDepth(request));
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
    var requestDocument = getSafeRequestDocument(request);

    if (requestDocument == null) {
      // treat as allprop
      propfindType = PROPFIND_ALL_PROP;
      propfindProps = new DavPropertyNameSet();
      return;
    }

    var root = requestDocument.getDocumentElement();
    if (!DomUtils.matches(root, XML_PROPFIND, caldav(XML_PROPFIND))) {
      throw new BadRequestException("Expected " + XML_PROPFIND + " root element");
    }

    var prop = DomUtils.getChildElement(root, caldav(XML_PROP));
    if (prop != null) {
      propfindType = PROPFIND_BY_PROPERTY;
      propfindProps = new DavPropertyNameSet(prop);
      return;
    }

    if (DomUtils.getChildElement(root, caldav(XML_PROPNAME)) != null) {
      propfindType = PROPFIND_PROPERTY_NAMES;
      propfindProps = new DavPropertyNameSet();
      return;
    }

    if (DomUtils.getChildElement(root, caldav(XML_ALLPROP)) != null) {
      propfindType = PROPFIND_ALL_PROP;
      propfindProps = new DavPropertyNameSet();

      var include = DomUtils.getChildElement(root, caldav("include"));
      if (include != null) {
        ElementIterator included = DomUtils.getChildren(include);
        while (included.hasNext()) {
          var name = DavPropertyName.createFromXml(included.nextElement());
          propfindProps.add(name);
        }
      }

      return;
    }

    throw new BadRequestException("Expected one of " + XML_PROP + ", " + XML_PROPNAME + ", or " + XML_ALLPROP + " as child of " + XML_PROPFIND);
  }

  private Document getSafeRequestDocument(final HttpServletRequest request) {
    if (StringUtils.isBlank(request.getContentType())) {
      throw new BadRequestException("No Content-Type specified");
    }

    var mediaType = MediaType.valueOf(request.getContentType());
    if (!(mediaType.isCompatibleWith(APPLICATION_XML) || mediaType.isCompatibleWith(TEXT_XML))) {
      throw new UnsupportedMediaTypeException("Expected Content-Type " + APPLICATION_XML + " or " + TEXT_XML);
    }

    return getRequestDocument(request);
  }

  private Document getRequestDocument(final HttpServletRequest request) {
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
      var in = request.getInputStream();
      if (in != null) {
        // use a buffered input stream to find out whether there actually
        // is a request body
        var bin = new BufferedInputStream(in);
        bin.mark(1);
        var isEmpty = -1 == bin.read();
        bin.reset();
        if (!isEmpty) {
          requestDocument = DomUtils.parseDocument(bin);
        }
      }
    } catch (Exception e) {
      throw new BadRequestException(e.getMessage());
    }
    return requestDocument;
  }

  public DavResourceFactory getResourceFactory() {
    return resourceFactory;
  }
}
