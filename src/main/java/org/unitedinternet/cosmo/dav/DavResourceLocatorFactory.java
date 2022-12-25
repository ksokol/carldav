package org.unitedinternet.cosmo.dav;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URL;

public interface DavResourceLocatorFactory {

  DavResourceLocator createResourceLocatorByPath(URL context, String path);

  DavResourceLocator createResourceLocatorByUri(URL context, String uri) throws CosmoDavException;

  DavResourceLocator createHomeLocator(URL context, String userId);

  DavResourceLocator createPrincipalLocator(final URL context, final String userId);

  DavResourceLocator createResourceLocatorFromRequest(final HttpServletRequest request);
}
