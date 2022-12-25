package org.unitedinternet.cosmo.dav.report;

import carldav.exception.resolver.ResponseUtils;
import carldav.jackrabbit.webdav.MultiStatus;
import carldav.jackrabbit.webdav.MultiStatusResponse;
import carldav.jackrabbit.webdav.property.DavPropertyNameSet;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jakarta.servlet.http.HttpServletResponse;

import static carldav.jackrabbit.webdav.DavConstants.PROPFIND_ALL_PROP;

public abstract class MultiStatusReport extends ReportBase {

  private final MultiStatus multistatus = new MultiStatus();
  protected int propfindType = PROPFIND_ALL_PROP;
  private DavPropertyNameSet propfindProps;

  protected void output(HttpServletResponse response) {
    try {
      buildMultistatus();
      ResponseUtils.sendXmlResponse(response, multistatus, 207);
    } catch (Exception e) {
      throw new CosmoDavException(e);
    }
  }

  public final void buildMultistatus() {
    var resultProps = createResultPropSpec();

    for (var result : getResults()) {
      var msr = buildMultiStatusResponse(result, resultProps);
      multistatus.addResponse(msr);
    }
  }

  protected DavPropertyNameSet createResultPropSpec() {
    return new DavPropertyNameSet(propfindProps);
  }

  protected MultiStatusResponse buildMultiStatusResponse(WebDavResource resource, DavPropertyNameSet props) {
    if (props.isEmpty()) {
      var href = resource.getResourceLocator().getHref(resource.isCollection());
      return new MultiStatusResponse(href, 200);
    }
    return new MultiStatusResponse(resource, props, propfindType);
  }

  protected MultiStatus getMultiStatus() {
    return multistatus;
  }

  public final Element toXml(Document document) {
    runQuery();
    return multistatus.toXml(document);
  }

  public void setPropFindType(int type) {
    this.propfindType = type;
  }

  public DavPropertyNameSet getPropFindProps() {
    return propfindProps;
  }

  public void setPropFindProps(DavPropertyNameSet props) {
    this.propfindProps = props;
  }
}
