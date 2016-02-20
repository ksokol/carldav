package org.unitedinternet.cosmo.dav.provider;

import org.apache.jackrabbit.webdav.WebdavResponse;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.MethodNotAllowedException;
import org.unitedinternet.cosmo.dav.WebDavResource;

import java.io.IOException;

/**
 * <p>
 * An implementation of <code>DavProvider</code> that implements access to <code>DavUserPrincipalCollection</code> resources.
 * </p>
 *
 * @see DavProvider
 * @see DavUserPrincipalCollection
 */
public class UserPrincipalProvider extends CollectionProvider {

    public UserPrincipalProvider(DavResourceFactory resourceFactory) {
        super(resourceFactory);
    }

    public void put(DavRequest request, WebdavResponse response, DavContent content) throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("PUT not allowed for user principal collection");
    }

    public void delete(DavRequest request, WebdavResponse response, WebDavResource resource) throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("DELETE not allowed for user principal collection");
    }

    public void copy(DavRequest request, WebdavResponse response, WebDavResource resource) throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("COPY not allowed for user principal collection");
    }

}
