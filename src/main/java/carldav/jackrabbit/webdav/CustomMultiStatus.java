package carldav.jackrabbit.webdav;

import static carldav.CarldavConstants.caldav;
import static carldav.jackrabbit.webdav.CustomDavConstants.XML_MULTISTATUS;

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
        final Element multiStatus = CustomDomUtils.createElement(document, XML_MULTISTATUS, caldav(XML_MULTISTATUS));
        for (Map.Entry<String, CustomMultiStatusResponse> resp : responses.entrySet()) {
            multiStatus.appendChild(resp.getValue().toXml(document));
        }
        return multiStatus;
    }

    public void addResponse(CustomMultiStatusResponse response) {
        responses.put(response.getHref(), response);
    }

    public void addResourceProperties(WebDavResource resource, CustomDavPropertyNameSet propNameSet, int propFindType, int depth) {
        addResponse(new CustomMultiStatusResponse(resource, propNameSet, propFindType));
        if (depth > 0 && resource.isCollection()) {
            final List<WebDavResource> members2 = resource.getMembers();

            for (final WebDavResource webDavResource : members2) {
                addResourceProperties(webDavResource, propNameSet, propFindType, depth - 1);
            }
        }
    }
}
