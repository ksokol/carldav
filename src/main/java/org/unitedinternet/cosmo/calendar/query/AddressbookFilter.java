package org.unitedinternet.cosmo.calendar.query;

import org.springframework.util.Assert;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.w3c.dom.Element;

import java.text.ParseException;

public class AddressbookFilter implements CaldavConstants {

    private final ComponentFilter filter = null;

    public AddressbookFilter(Element element) throws ParseException {
        Assert.notNull(element, "element is null");
        //TODO filter not in use
        // //new ComponentFilter(element, timezone)
    }

    public ComponentFilter getFilter() {
        return filter;
    }
}
