package carldav.jackrabbit.webdav;

import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Kamill Sokol
 */
public class CustomMultiStatus extends MultiStatus {

    /*
     * sort nodes based on href in order to allow deterministic unit tests
     */
    @Override
    public Element toXml(Document document) {
        final Element multistatus = DomUtil.createElement(document, XML_MULTISTATUS, NAMESPACE);
        final MultiStatusResponse[] responses = getResponses();
        final Map<String, MultiStatusResponse> sorted = new TreeMap<>();

        for (final MultiStatusResponse response : responses) {
            sorted.put(response.getHref(), response);
        }

        for (Map.Entry<String, MultiStatusResponse> resp : sorted.entrySet()) {
            multistatus.appendChild(resp.getValue().toXml(document));
        }
        if (getResponseDescription() != null) {
            Element respDesc = DomUtil.createElement(document, XML_RESPONSEDESCRIPTION, NAMESPACE, getResponseDescription());
            multistatus.appendChild(respDesc);
        }
        return multistatus;
    }
}
