package org.unitedinternet.cosmo.dav.provider;

import carldav.service.generator.IdGenerator;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResponse;
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

    public UserPrincipalProvider(DavResourceFactory resourceFactory, IdGenerator idGenerator) {
        super(resourceFactory, idGenerator);
    }

    public void put(DavRequest request, DavResponse response, DavContent content) throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("PUT not allowed for user principal collection");
    }

    public void delete(DavRequest request, DavResponse response, WebDavResource resource) throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("DELETE not allowed for user principal collection");
    }

    public void copy(DavRequest request, DavResponse response, WebDavResource resource) throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("COPY not allowed for user principal collection");
    }

}
