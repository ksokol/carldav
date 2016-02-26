/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav.property;

import static carldav.CarldavConstants.GET_LAST_MODIFIED;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class LastModified extends StandardDavProperty {

    private static final DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;

    public LastModified(Date date) {
        super(GET_LAST_MODIFIED, dateFormatLocal(date), false);
    }

    private static String dateFormatLocal(Date date) {
        ZonedDateTime d;

        if (date == null) {
            d = ZonedDateTime.now();
        } else {
            d = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault());
        }

        return formatter.format(d);
    }
}
