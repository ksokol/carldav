package carldav.exception.resolver;

import carldav.jackrabbit.webdav.xml.DomUtils;
import carldav.jackrabbit.webdav.xml.XmlSerializable;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.unitedinternet.cosmo.dav.CosmoDavException;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

public final class ResponseUtils {

  private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();

  public static void sendDavError(CosmoDavException e, HttpServletResponse response) {
    response.setStatus(e.getErrorCode());
    if (!e.hasContent()) {
      return;
    }

    XMLStreamWriter writer = null;

    try {
      var out = new ByteArrayOutputStream();
      writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(out);
      writer.writeStartDocument();
      e.writeTo(writer);
      writer.writeEndDocument();

      response.setContentType("text/xml; charset=UTF-8");
      var bytes = out.toByteArray();
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

  public static void sendXmlResponse(HttpServletResponse httpResponse, XmlSerializable serializable, int status) {
    httpResponse.setStatus(status);

    if (serializable != null) {
      var out = new ByteArrayOutputStream();
      try {
        var doc = DomUtils.createDocument();
        doc.appendChild(serializable.toXml(doc));

        // JCR-2636: Need to use an explicit OutputStreamWriter
        // instead of relying on the built-in UTF-8 serialization
        // to avoid problems with surrogate pairs on Sun JRE 1.5.
        var writer = new OutputStreamWriter(out, "UTF-8");
        DomUtils.transformDocument(doc, writer);
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
