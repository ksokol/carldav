package carldav.jackrabbit.webdav;

import static carldav.CarldavConstants.XML_STATUS;
import static carldav.CarldavConstants.caldav;

import carldav.jackrabbit.webdav.xml.CustomXmlSerializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Kamill Sokol
 */
public class CustomStatus implements CustomDavConstants, CustomXmlSerializable {

    private static Logger LOG = LoggerFactory.getLogger(CustomStatus.class);

    private final String version;
    private final int code;
    private final String phrase;

    public CustomStatus(int code) {
        version = "HTTP/1.1";
        this.code = code;
        phrase = HttpStatus.valueOf(code).getReasonPhrase();
    }

    public CustomStatus(String version, int code, String phrase) {
        this.version = version;
        this.code = code;
        this.phrase = phrase;
    }

    public int getStatusCode() {
        return code;
    }

    /**
     * @see CustomXmlSerializable#toXml(Document)
     */
    public Element toXml(Document document) {
        String statusLine = version + " " + code + " " + phrase;
        Element e = CustomDomUtils.createElement(document, XML_STATUS, caldav(XML_STATUS));
        CustomDomUtils.setText(e, statusLine);
        return e;
    }

    /**
     * Parse the given status line and return a new <code>Status</code> object.
     *
     * @param statusLine
     * @return a new <code>Status</code>
     */
    public static CustomStatus parse(String statusLine) {
        if (statusLine == null) {
            throw new IllegalArgumentException("Unable to parse status line from null xml element.");
        }
        CustomStatus status;

        // code copied from org.apache.commons.httpclient.StatusLine
        int length = statusLine.length();
        int at = 0;
        int start = 0;
        try {
            while (Character.isWhitespace(statusLine.charAt(at))) {
                ++at;
                ++start;
            }
            if (!"HTTP".equals(statusLine.substring(at, at += 4))) {
                LOG.warn("Status-Line '" + statusLine + "' does not start with HTTP");
            }
            //handle the HTTP-Version
            at = statusLine.indexOf(' ', at);
            if (at <= 0) {
                LOG.warn("Unable to parse HTTP-Version from the status line: '" + statusLine + "'");
            }
            String version = (statusLine.substring(start, at)).toUpperCase();
            //advance through spaces
            while (statusLine.charAt(at) == ' ') {
                at++;
            }
            //handle the Status-Code
            int code;
            int to = statusLine.indexOf(' ', at);
            if (to < 0) {
                to = length;
            }
            try {
                code = Integer.parseInt(statusLine.substring(at, to));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unable to parse status code from status line: '" + statusLine + "'");
            }
            //handle the Reason-Phrase
            String phrase = "";
            at = to + 1;
            if (at < length) {
                phrase = statusLine.substring(at).trim();
            }

            status = new CustomStatus(version, code, phrase);

        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Status-Line '" + statusLine + "' is not valid");
        }
        return status;
    }
}
