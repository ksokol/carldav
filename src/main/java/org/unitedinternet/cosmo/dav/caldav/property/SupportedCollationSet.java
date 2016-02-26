/*
 * Copyright 2005-2007 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.dav.caldav.property;

import static carldav.CarldavConstants.SUPPORTED_COLLATION_SET;
import static carldav.CarldavConstants.c;

import carldav.jackrabbit.webdav.xml.CustomDomUtils;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SupportedCollationSet extends StandardDavProperty implements CaldavConstants, ICalendarConstants {
    private static final String[] SUPPORTED_COLLATIONS = { "i;ascii-casemap", "i;octet" };

    public SupportedCollationSet() {
        this(SUPPORTED_COLLATIONS);
    }

    public SupportedCollationSet(String[] collations) {
        super(SUPPORTED_COLLATION_SET, collations(collations), true);
        for (String collation : collations) {
            if (!CalendarUtils.isSupportedCollation(collation)) {
                throw new IllegalArgumentException("Invalid collation '" + collation + "'.");
            }
        }
    }

    private static HashSet<String> collations(String[] collations) {
        HashSet<String> collationSet = new HashSet<>();
        Collections.addAll(collationSet, collations);
        return collationSet;
    }

    public Set<String> getCollations() {
        return (Set<String>) getValue();
    }

    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        for (String collation : getCollations()) {
            Element e = CustomDomUtils.createElement(document, ELEMENT_CALDAV_SUPPORTEDCOLLATION, c(ELEMENT_CALDAV_SUPPORTEDCOLLATION));
            CustomDomUtils.setText(e, collation);
            name.appendChild(e);
        }

        return name;
    }
}
