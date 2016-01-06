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
package org.unitedinternet.cosmo.model;

import java.util.Date;

public interface AuditableObject {

    Date getCreationDate();

    void setCreationDate(Date date);

    Date getModifiedDate();

    void setModifiedDate(Date date);

    /**
     * Update modifiedDate with current system time.
     */
    void updateTimestamp();

    /**
     * <p>
     * Returns a string representing the state of the object. Entity tags can
     * be used to compare two objects in remote environments where the
     * equals method is not available.
     * </p>
     */
    String getEntityTag();

    String calculateEntityTag();

    void setEntityTag(String tag);
}