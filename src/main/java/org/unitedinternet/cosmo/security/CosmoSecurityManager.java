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
package org.unitedinternet.cosmo.security;

/**
 * Represents a server-wide security controller for Cosmo. It
 * provides entry points for obtaining information about the
 * authentication state of the currently executing thread or for
 * initiating authentication (or overwriting the current state).
 *
 * @see CosmoSecurityContext
 */
public interface CosmoSecurityManager {

    /**
     * Provides a <code>CosmoSecurityContext</code> representing a
     * previously authenticated principal.
     */
    CosmoSecurityContext getSecurityContext()
        throws CosmoSecurityException;
}
