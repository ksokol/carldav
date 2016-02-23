package carldav.jackrabbit.webdav;

import static org.apache.jackrabbit.webdav.DavConstants.NAMESPACE;
import static org.apache.jackrabbit.webdav.DavConstants.XML_MULTISTATUS;

import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CustomMultiStatus implements XmlSerializable {

    private final Map<String, CustomMultiStatusResponse> responses = new TreeMap<>();

    @Override
    public Element toXml(Document document) {
        final Element multiStatus = DomUtil.createElement(document, XML_MULTISTATUS, NAMESPACE);
        for (Map.Entry<String, CustomMultiStatusResponse> resp : responses.entrySet()) {
            multiStatus.appendChild(resp.getValue().toXml(document));
        }
        return multiStatus;
    }

    public void addResponse(CustomMultiStatusResponse response) {
        responses.put(response.getHref(), response);
    }

    public void addResourceProperties(WebDavResource resource, DavPropertyNameSet propNameSet, int propFindType, int depth) {
        addResponse(new CustomMultiStatusResponse(resource, propNameSet, propFindType));
        if (depth > 0 && resource.isCollection()) {
            final List<WebDavResource> members2 = resource.getMembers2();

            for (final WebDavResource webDavResource : members2) {
                addResourceProperties(webDavResource, propNameSet, propFindType, depth - 1);
            }
        }
    }
}
