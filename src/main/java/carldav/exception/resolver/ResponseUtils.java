package carldav.exception.resolver;

import carldav.jackrabbit.webdav.CustomDomUtils;
import carldav.jackrabbit.webdav.xml.CustomXmlSerializable;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Kamill Sokol
 */
public final class ResponseUtils {

    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();

    public static void sendDavError(CosmoDavException e, HttpServletResponse response) {
        response.setStatus(e.getErrorCode());
        if (! e.hasContent()) {
            return;
        }

        XMLStreamWriter writer = null;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(out);
            writer.writeStartDocument();
            e.writeTo(writer);
            writer.writeEndDocument();

            response.setContentType("text/xml; charset=UTF-8");
            byte[] bytes = out.toByteArray();
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (XMLStreamException exception) {
                    //ignore me
                }
            }
        }
    }

    public static void sendXmlResponse(HttpServletResponse httpResponse, CustomXmlSerializable serializable, int status) {
        httpResponse.setStatus(status);

        if (serializable != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                Document doc = CustomDomUtils.createDocument();
                doc.appendChild(serializable.toXml(doc));

                // JCR-2636: Need to use an explicit OutputStreamWriter
                // instead of relying on the built-in UTF-8 serialization
                // to avoid problems with surrogate pairs on Sun JRE 1.5.
                Writer writer = new OutputStreamWriter(out, "UTF-8");
                DomUtil.transformDocument(doc, writer);
                writer.flush();

                // TODO: Should this be application/xml? See JCR-1621
                httpResponse.setContentType("text/xml; charset=UTF-8");
                httpResponse.setContentLength(out.size());
                out.writeTo(httpResponse.getOutputStream());
            } catch (Exception e) {
                throw new CosmoDavException(e);
            }
        }
    }
}
