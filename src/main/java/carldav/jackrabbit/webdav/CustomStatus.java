package carldav.jackrabbit.webdav;

import carldav.jackrabbit.webdav.xml.CustomDomUtils;
import carldav.jackrabbit.webdav.xml.CustomXmlSerializable;
import org.springframework.http.HttpStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static carldav.CarldavConstants.XML_STATUS;
import static carldav.CarldavConstants.caldav;

/**
 * @author Kamill Sokol
 */
public class CustomStatus implements CustomDavConstants, CustomXmlSerializable {

    private final String version;
    private final int code;
    private final String phrase;

    public CustomStatus(int code) {
        version = "HTTP/1.1";
        this.code = code;
        phrase = HttpStatus.valueOf(code).getReasonPhrase();
    }

    public Element toXml(Document document) {
        String statusLine = version + " " + code + " " + phrase;
        Element e = CustomDomUtils.createElement(document, XML_STATUS, caldav(XML_STATUS));
        CustomDomUtils.setText(e, statusLine);
        return e;
    }
}
