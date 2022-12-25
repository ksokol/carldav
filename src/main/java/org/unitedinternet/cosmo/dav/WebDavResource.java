package org.unitedinternet.cosmo.dav;

import carldav.jackrabbit.webdav.DavConstants;
import carldav.jackrabbit.webdav.property.DavPropertyName;
import carldav.jackrabbit.webdav.property.DavPropertySet;
import carldav.jackrabbit.webdav.version.report.Report;
import carldav.jackrabbit.webdav.version.report.ReportInfo;
import org.unitedinternet.cosmo.dav.impl.DavCollectionBase;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface WebDavResource {

  /**
   * String constant representing the WebDAV 1 compliance
   * class as well as the Cosmo extended classes.
   */
  // see bug 5137 for why we don't include class 2
  String COMPLIANCE_CLASS = "1, 3, addressbook, calendar-access";

  String getComplianceClass();

  String getSupportedMethods();

  boolean exists();

  boolean isCollection();

  String getDisplayName();

  /**
   * Returns the path of the hierarchy element defined by this <code>DavResource</code>.
   * This method is a shortcut for <code>DavResource.getLocator().getResourcePath()</code>.
   *
   * @return path of the element defined by this <code>DavResource</code>.
   */
  String getResourcePath();

  /**
   * Return the time of the last modification or -1 if the modification time
   * could not be retrieved.
   *
   * @return time of last modification or -1.
   */
  long getModificationTime();

  DavCollection getParent() throws CosmoDavException;

  void removeItem(WebDavResource member);

  default void removeCollection(DavCollectionBase member) {
  }

  /**
   * Returns the absolute href of this resource as returned in the
   * multistatus response body.
   *
   * @return href
   */
  String getHref();

  /**
   * Returns all webdav properties present on this resource that will be
   * return upon a {@link DavConstants#PROPFIND_ALL_PROP} request. The
   * implementation may in addition expose other (protected or calculated)
   * properties which should be marked accordingly (see also
   * {@link WebDavProperty#isInvisibleInAllprop()}.
   *
   * @return a {@link DavPropertySet} containing at least all properties
   * of this resource that are exposed in 'allprop' PROPFIND request.
   */
  DavPropertySet getProperties();

  /**
   * Return the webdav property with the specified name.
   *
   * @param name name of the webdav property
   * @return the {@link WebDavProperty} with the given name or <code>null</code>
   * if the property does not exist.
   */
  WebDavProperty<?> getProperty(DavPropertyName name);

  /**
   * Returns an array of all {@link DavPropertyName property names} available
   * on this resource.
   *
   * @return an array of property names.
   */
  DavPropertyName[] getPropertyNames();

  void writeHead(HttpServletResponse response) throws IOException;

  void writeBody(HttpServletResponse response) throws IOException;

  /**
   * @param info The given report info.
   * @return Return the report that matches the given report info if it is
   * supported by this resource.
   * @throws CosmoDavException - if something is wrong this exception is thrown.
   */
  Report getReport(ReportInfo info) throws CosmoDavException;

  DavResourceLocator getResourceLocator();

  String getETag();

  List<WebDavResource> getMembers();

  String getName();
}
