package carldav.exception.resolver;

import org.unitedinternet.cosmo.dav.CosmoDavException;

import java.io.ByteArrayOutputStream;

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
}
