/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.model.mock;

import org.unitedinternet.cosmo.model.Stamp;


/**
 * Represents a Task Stamp.
 */
public class MockTaskStamp extends MockStamp implements java.io.Serializable, Stamp {

    private static final long serialVersionUID = -6197756070431706553L;

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTaskStamp#getType()
     */
    /**
     * Gets type.
     * @return The type.
     */
    public String getType() {
        return "task";
    }

    /**
     * Copy.
     * {@inheritDoc}
     * @return The stamp.
     */
    public Stamp copy() {
        return new MockTaskStamp();
    }

    @Override
    public String calculateEntityTag() {
       return "";
    }
}
