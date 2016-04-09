package carldav.jackrabbit.webdav;

import carldav.jackrabbit.webdav.xml.DomUtils;
import carldav.jackrabbit.webdav.xml.XmlSerializable;
import org.springframework.http.HttpStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static carldav.CarldavConstants.XML_STATUS;
import static carldav.CarldavConstants.caldav;

public class Status implements DavConstants, XmlSerializable {

    private final String version;
    private final int code;
    private final String phrase;

    public Status(int code) {
        version = "HTTP/1.1";
        this.code = code;
        phrase = HttpStatus.valueOf(code).getReasonPhrase();
    }

    public Element toXml(Document document) {
        String statusLine = version + " " + code + " " + phrase;
        Element e = DomUtils.createElement(document, XML_STATUS, caldav(XML_STATUS));
        DomUtils.setText(e, statusLine);
        return e;
    }
}
