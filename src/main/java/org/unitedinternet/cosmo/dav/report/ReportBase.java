package org.unitedinternet.cosmo.dav.report;

import carldav.jackrabbit.webdav.version.report.Report;
import carldav.jackrabbit.webdav.version.report.ReportInfo;

import jakarta.servlet.http.HttpServletResponse;

import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

import static carldav.jackrabbit.webdav.DavConstants.DEPTH_0;
import static carldav.jackrabbit.webdav.DavConstants.DEPTH_1;

public abstract class ReportBase implements Report {

  private WebDavResource resource;
  private ReportInfo info;
  private HashSet<WebDavResource> results;

  public void init(WebDavResource resource, ReportInfo info) {
    this.resource = resource;
    this.info = info;
    this.results = new HashSet<>();
    parseReport(info);
  }

  public void run(HttpServletResponse response) {
    runQuery();
    output(response);
  }

  protected abstract void parseReport(ReportInfo info);

  protected void runQuery() {
    doQuerySelf(resource);

    if (info.getDepth() == DEPTH_0) {
      return;
    }

    if (!(resource instanceof DavCollection collection)) {
      throw new BadRequestException("Report may not be run with depth " + info.getDepth() + " against a non-collection resource");
    }

    doQueryChildren(collection);
    if (info.getDepth() == DEPTH_1) {
      return;
    }

    doQueryDescendents(collection);
  }

  protected abstract void output(HttpServletResponse response);

  protected abstract void doQuerySelf(WebDavResource resource);

  protected abstract void doQueryChildren(DavCollection collection);

  protected void doQueryDescendents(DavCollection collection) {
    for (var member : collection.getCollectionMembers()) {
      if (member.isCollection()) {
        var dc = (DavCollection) member;
        doQuerySelf(dc);
        doQueryChildren(dc);
        doQueryDescendents(dc);
      }
    }
  }

  protected static Element getReportElementFrom(ReportInfo reportInfo) {
    return reportInfo != null ? reportInfo.getDocumentElement() : null;

  }

  public WebDavResource getResource() {
    return resource;
  }

  public ReportInfo getInfo() {
    return info;
  }

  public Set<WebDavResource> getResults() {
    return results;
  }
}
