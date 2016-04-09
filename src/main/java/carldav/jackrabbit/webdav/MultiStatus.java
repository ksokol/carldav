package carldav.jackrabbit.webdav;

import static carldav.CarldavConstants.caldav;
import static carldav.jackrabbit.webdav.DavConstants.XML_MULTISTATUS;

import carldav.jackrabbit.webdav.property.DavPropertyNameSet;
import carldav.jackrabbit.webdav.xml.DomUtils;
import carldav.jackrabbit.webdav.xml.XmlSerializable;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MultiStatus implements XmlSerializable {

    private final Map<String, MultiStatusResponse> responses = new TreeMap<>();

    @Override
    public Element toXml(Document document) {
        final Element multiStatus = DomUtils.createElement(document, XML_MULTISTATUS, caldav(XML_MULTISTATUS));
        for (Map.Entry<String, MultiStatusResponse> resp : responses.entrySet()) {
            multiStatus.appendChild(resp.getValue().toXml(document));
        }
        return multiStatus;
    }

    public void addResponse(MultiStatusResponse response) {
        responses.put(response.getHref(), response);
    }

    public void addResourceProperties(WebDavResource resource, DavPropertyNameSet propNameSet, int propFindType, int depth) {
        addResponse(new MultiStatusResponse(resource, propNameSet, propFindType));
        if (depth > 0 && resource.isCollection()) {
            final List<WebDavResource> members2 = resource.getMembers();

            for (final WebDavResource webDavResource : members2) {
                addResourceProperties(webDavResource, propNameSet, propFindType, depth - 1);
            }
        }
    }
}
