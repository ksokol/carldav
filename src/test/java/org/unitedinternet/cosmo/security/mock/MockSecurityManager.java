/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.security.mock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.security.CosmoSecurityContext;
import org.unitedinternet.cosmo.security.CosmoSecurityException;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;

import java.security.Principal;

/**
 * A mock implementation of the {@link CosmoSecurityManager}
 * interface that provides a dummy {@link CosmoSecurityContext} for
 * unit mocks.
 */
public class MockSecurityManager implements CosmoSecurityManager {
   
    private static final Log LOG = LogFactory.getLog(MockSecurityManager.class);

    @SuppressWarnings("rawtypes")
    private static ThreadLocal contexts = new ThreadLocal();

    /* ----- CosmoSecurityManager methods ----- */

    /**
     * Provide a <code>CosmoSecurityContext</code> representing a
     * Cosmo user previously authenticated by the Cosmo security
     * system.
     * @return cosmo security context.
     * @throws CosmoSecurityException - if something is wrong this exception is thrown.
     */
    public CosmoSecurityContext getSecurityContext() throws CosmoSecurityException {
        CosmoSecurityContext context = (CosmoSecurityContext) contexts.get();
        if (context == null) {
            throw new CosmoSecurityException("security context not set up");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("getting security context for " + context.getName());
        }
        return context;
    }

    // for testing
    /**
     * Initiates security context.
     * @param context The cosmo security context.
     */
    @SuppressWarnings("unchecked")
    public void initiateSecurityContext(CosmoSecurityContext context) {
        contexts.set(context);
    }

    /* ----- our methods ----- */

    /**
     * Sets up mock security context.
     * @param principal The principal.
     * @return The cosmo security context.
     */
    @SuppressWarnings("unchecked")
    public CosmoSecurityContext setUpMockSecurityContext(Principal principal) {
        CosmoSecurityContext context = createSecurityContext(principal);
        contexts.set(context);
        if (LOG.isDebugEnabled()) {
            LOG.debug("setting up security context for " + context.getName());
        }
        return context;
    }

    /**
     * Creates security context.
     * @param principal The principal.
     * @return The cosmo security context.
     */
    protected CosmoSecurityContext createSecurityContext(Principal principal) {
        return new MockSecurityContext(principal);
    }


}
