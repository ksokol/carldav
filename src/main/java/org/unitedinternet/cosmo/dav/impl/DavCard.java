package org.unitedinternet.cosmo.dav.impl;

import carldav.entity.Item;
import carldav.jackrabbit.webdav.io.DavInputContext;
import carldav.jackrabbit.webdav.property.DavPropertySet;
import org.apache.commons.io.IOUtils;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.caldav.property.AddressData;
import org.unitedinternet.cosmo.dav.property.ContentType;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpHeaders.LAST_MODIFIED;
import static org.unitedinternet.cosmo.icalendar.ICalendarConstants.CARD_MEDIA_TYPE;

public class DavCard extends DavItemResourceBase {

  public DavCard(Item item, DavResourceLocator locator, DavResourceFactory factory) {
    super(item, locator, factory);
  }

  public DavCard(DavResourceLocator locator, DavResourceFactory factory) {
    this(new Item(Item.Type.VCARD.toString()), locator, factory);
  }

  public void writeHead(HttpServletResponse response) {
    var content = getItem();
    var calendar = content.getCalendar().getBytes(StandardCharsets.UTF_8);

    response.setContentType(content.getMimetype());
    response.setContentLength(calendar.length);
    if (getModificationTime() >= 0) {
      response.addDateHeader(LAST_MODIFIED, getModificationTime());
    }
    if (getETag() != null) {
      response.setHeader(ETAG, getETag());
    }
  }

  public void writeBody(final HttpServletResponse response) throws IOException {
    var content = getItem();
    var calendar = content.getCalendar().getBytes(StandardCharsets.UTF_8);
    IOUtils.copy(new ByteArrayInputStream(calendar), response.getOutputStream());
  }

  @Override
  protected void populateItem(DavInputContext inputContext) {
    super.populateItem(inputContext);

    var file = getItem();
    var scanner = new Scanner(inputContext.getInputStream(), inputContext.getCharset()).useDelimiter("\\A");
    file.setCalendar(scanner.next());
    converter.convertCard(file);
  }

  @Override
  protected void loadLiveProperties(DavPropertySet properties) {
    super.loadLiveProperties(properties);
    properties.add(new ContentType(CARD_MEDIA_TYPE, null));
    properties.add(new AddressData(null));
  }
}
