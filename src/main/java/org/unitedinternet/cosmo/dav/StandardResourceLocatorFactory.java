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
package org.unitedinternet.cosmo.dav;

import org.springframework.util.Assert;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.*;

/**
 * Standard implementation of {@link DavResourceLocatorFactory}.
 * Returns instances of {@link StandardResourceLocator}.
 *
 * @see DavResourceLocatorFactory
 */
public class StandardResourceLocatorFactory implements DavResourceLocatorFactory, ExtendedDavConstants {

    private final String contextPath;

    public StandardResourceLocatorFactory(String contextPath) {
        Assert.hasText(contextPath, "contextPath is null");
        this.contextPath = "/".equals(contextPath) ? "" : contextPath;
    }

    public DavResourceLocator createResourceLocatorByPath(URL context,
                                                          String path) {
        return new StandardResourceLocator(context, path, this);
    }

    public DavResourceLocator createResourceLocatorByUri(URL context,
                                                         String raw)
        throws CosmoDavException {
        try {
            URI uri = new URI(raw);
            URL url;
            if (raw.startsWith("/")) {
                // absolute-path relative URL
                url = new URL(context, uri.getRawPath());
            } else {
                // absolute URL
                url = new URL(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getRawPath());

                // make sure that absolute URLs use the same scheme and
                // authority (host:port)
                if (url.getProtocol() != null &&
                    ! url.getProtocol().equals(context.getProtocol())) {
                    throw new BadRequestException("target " + uri
                            + " does not specify same scheme " + "as context "
                            + context.toString());
                }
                
                // look at host
                if(url.getHost() !=null &&  ! url.getHost().equals(context.getHost())) {
                    throw new BadRequestException("target " + uri
                            + " does not specify same host " + "as context "
                            + context.toString());
                }
                
                // look at ports
                // take default ports 80 and 443 into account so that
                // https://server is the same as https://server:443
                int port1 = translatePort(context.getProtocol(), context.getPort());
                int port2 = translatePort(url.getProtocol(), url.getPort());
                
                if(port1!=port2) {
                    throw new BadRequestException("target " + uri
                            + " does not specify same port " + "as context "
                            + context.toString());
                }
            }

            String path = url.getPath().replaceFirst(contextPath, "");
            return new StandardResourceLocator(context, URLDecoder.decode(path, "UTF-8"), this);
        } catch (URISyntaxException | MalformedURLException | UnsupportedEncodingException e) {
            throw new BadRequestException("Invalid URL: " + e.getMessage());
        }
    }

    public DavResourceLocator createHomeLocator(URL context, String userId) {
        String path = TEMPLATE_HOME.bind(userId);
        return new StandardResourceLocator(context, path, this);
    }

    @Override
    public DavResourceLocator createPrincipalLocator(final URL context, final String userId) {
        try {
            final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUri(context.toURI());
            uriComponentsBuilder.replacePath(uriComponentsBuilder.build().getPath());
            final URI uri = uriComponentsBuilder.build().toUri();
            return createHomeLocator(uri.toURL(), userId);
        } catch (Exception e) {
            throw new CosmoDavException(e);
        }
    }

    @Override
    public DavResourceLocator createResourceLocatorFromRequest(final HttpServletRequest request) {
        DavResourceLocator locator;
        URL context;
        try {
            final ServletUriComponentsBuilder servletUriComponentsBuilder = ServletUriComponentsBuilder.fromRequest(request);
            final String firstPathSegment = servletUriComponentsBuilder.build().getPathSegments().get(0);
            String basePath;

            if(contextPath.equals(firstPathSegment)) {
                basePath = "/";
            } else {
                basePath = "".equals(contextPath) ? "/" : contextPath;
            }

            context = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), basePath);
            locator = createResourceLocatorByUri(context, request.getRequestURI());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return locator;
    }

    private int translatePort(String protocol, int port) {
        if (port == -1 || port == 80 && "http".equals(protocol) 
                || port == 443 && "https".equals(protocol)) {
            return -1;
        }
        else {
            return port;
        }
    }
}
