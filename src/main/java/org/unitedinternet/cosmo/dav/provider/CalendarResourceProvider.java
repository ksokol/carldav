package org.unitedinternet.cosmo.dav.provider;

import carldav.entity.Item;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import org.unitedinternet.cosmo.dav.ConflictException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.SupportedCalendarComponentException;
import org.unitedinternet.cosmo.dav.impl.DavCalendarResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CalendarResourceProvider extends BaseProvider {

  private static final List<String> SUPPORTED_COMPONENT_TYPES = new ArrayList<>() {{
    add(Component.VEVENT);
    add(Component.VTODO);
    add(Component.VJOURNAL);
  }};

  public CalendarResourceProvider(DavResourceFactory resourceFactory) {
    super(resourceFactory);
  }

  public void put(HttpServletRequest request, HttpServletResponse response, WebDavResource content) throws CosmoDavException, IOException {

    // do content.getCollection() check only for normal auth only, ticket auth is on the item only, not its parent.
    if (!content.getParent().exists()) {
      throw new ConflictException("One or more intermediate collections must be created");
    }

    var status = content.exists() ? 204 : 201;
    var ctx = createInputContext(request);
    if (!content.exists()) {
      content = createCalendarResource(content.getResourceLocator(), ctx.getCalendar());
    }

    content.getParent().addContent(content, ctx);

    response.setStatus(status);
    // since the iCalendar body is parsed and re-serialized for storage,
    // it's possible that what will be served for subsequent GETs is
    // slightly different than what was provided in the PUT, so send a
    // weak etag
    response.setHeader("ETag", content.getETag());
  }

  protected WebDavResource createCalendarResource(DavResourceLocator locator, Calendar calendar) throws CosmoDavException {
    if (!calendar.getComponents(Component.VEVENT).isEmpty()) {
      var item = new Item();
      item.setType(Item.Type.VEVENT.toString());
      return new DavCalendarResource(item, locator, getResourceFactory());
    }
    if (!calendar.getComponents(Component.VTODO).isEmpty()) {
      var item = new Item();
      item.setType(Item.Type.VTODO.toString());
      return new DavCalendarResource(item, locator, getResourceFactory());
    }
    if (!calendar.getComponents(Component.VJOURNAL).isEmpty()) {
      var item = new Item();
      item.setType(Item.Type.VJOURNAL.toString());
      return new DavCalendarResource(item, locator, getResourceFactory());
    }
    throw new SupportedCalendarComponentException(SUPPORTED_COMPONENT_TYPES);
  }
}
