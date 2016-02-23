package carldav.jackrabbit.webdav;

import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Kamill Sokol
 */
public class CustomMultiStatus extends MultiStatus {

    /**
     * Map collecting the responses for this multistatus, where every href must
     * only occur one single time.
     */
    private Map<String, CustomMultiStatusResponse> responses = new LinkedHashMap<>();

    /*
     * sort nodes based on href in order to allow deterministic unit tests
     */
    @Override
    public Element toXml(Document document) {
        final Element multistatus = DomUtil.createElement(document, XML_MULTISTATUS, NAMESPACE);
        final CustomMultiStatusResponse[] responses = getResponses();
        final Map<String, CustomMultiStatusResponse> sorted = new TreeMap<>();

        for (final CustomMultiStatusResponse response : responses) {
            sorted.put(response.getHref(), response);
        }

        for (Map.Entry<String, CustomMultiStatusResponse> resp : sorted.entrySet()) {
            multistatus.appendChild(resp.getValue().toXml(document));
        }
        if (getResponseDescription() != null) {
            Element respDesc = DomUtil.createElement(document, XML_RESPONSEDESCRIPTION, NAMESPACE, getResponseDescription());
            multistatus.appendChild(respDesc);
        }
        return multistatus;
    }

    /**
     * Add a <code>MultiStatusResponse</code> element to this <code>MultiStatus</code>
     * <p>
     * This method is synchronized to avoid the problem described in
     * <a href="https://issues.apache.org/jira/browse/JCR-2755">JCR-2755</a>.
     *
     * @param response
     */
    public void addResponse2(CustomMultiStatusResponse response) {
        responses.put(response.getHref(), response);
    }

    /**
     * Returns the multistatus responses present as array.
     * <p>
     * This method is synchronized to avoid the problem described in
     * <a href="https://issues.apache.org/jira/browse/JCR-2755">JCR-2755</a>.
     *
     * @return array of all {@link CustomMultiStatusResponse responses} present in this
     * multistatus.
     */
    public CustomMultiStatusResponse[] getResponses() {
        return responses.values().toArray(new CustomMultiStatusResponse[responses.size()]);
    }

    /**
     * Add response(s) to this multistatus, in order to build a multistatus for
     * responding to a PROPFIND request.
     *
     * @param resource The resource to add property from
     * @param propNameSet The requested property names of the PROPFIND request
     * @param propFindType
     * @param depth
     */
    public void addResourceProperties(DavResource resource, DavPropertyNameSet propNameSet,
                                      int propFindType, int depth) {
        addResponse2(new CustomMultiStatusResponse(resource, propNameSet, propFindType));
        if (depth > 0 && resource.isCollection()) {
            DavResourceIterator iter = resource.getMembers();
            while (iter.hasNext()) {
                addResourceProperties(iter.nextResource(), propNameSet, propFindType, depth-1);
            }
        }
    }
}
