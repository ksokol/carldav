package org.unitedinternet.cosmo.dav.impl;

import edu.emory.mathcs.backport.java.util.Collections;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.dav.property.IsCollection;
import org.unitedinternet.cosmo.dav.property.ResourceType;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * <p>
 * Models a WebDAV principal collection (as described in RFC 3744) that contains a principal resource for each user
 * account in the server. The principal collection itself is not backed by a persistent entity.
 * </p>
 *
 * @see DavResourceBase
 * @see DavCollection
 */
public class DavUserPrincipalCollection extends DavResourceBase implements DavCollection {

    public DavUserPrincipalCollection(DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
        super(locator, factory);

        registerLiveProperty(DavPropertyName.DISPLAYNAME);
        registerLiveProperty(DavPropertyName.ISCOLLECTION);
        registerLiveProperty(DavPropertyName.RESOURCETYPE);
    }

    public String getSupportedMethods() {
        return "OPTIONS, PROPFIND";
    }

    public boolean isCollection() {
        return true;
    }

    public long getModificationTime() {
        return -1;
    }

    public boolean exists() {
        return true;
    }

    public String getDisplayName() {
        return "User Principals";
    }

    public String getETag() {
        return null;
    }

    public void writeTo(OutputContext outputContext) throws CosmoDavException, IOException {
        throw new UnsupportedOperationException();
    }

    public void addMember(org.apache.jackrabbit.webdav.DavResource member, InputContext inputContext)
            throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public DavResourceIterator getMembers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DavResourceIterator getCollectionMembers() {
        throw new UnsupportedOperationException();
    }

    public void removeMember(org.apache.jackrabbit.webdav.DavResource member) throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public WebDavResource getCollection() {
        throw new UnsupportedOperationException();
    }

    public void move(org.apache.jackrabbit.webdav.DavResource destination) throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public void copy(org.apache.jackrabbit.webdav.DavResource destination, boolean shallow)
            throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public DavCollection getParent() throws CosmoDavException {
        return null;
    }

    public void addContent(DavContent content, InputContext context) throws CosmoDavException {
        throw new UnsupportedOperationException();
    }

    public DavUserPrincipal findMember(String uri) throws CosmoDavException {
        DavResourceLocator locator = getResourceLocator().getFactory().createResourceLocatorByUri(getResourceLocator().getContext(), uri);
        return (DavUserPrincipal) getResourceFactory().resolve(locator);
    }

    protected Set<QName> getResourceTypes() {
        HashSet<QName> rt = new HashSet<>(1);
        rt.add(RESOURCE_TYPE_COLLECTION);
        return rt;
    }

    public Set<ReportType> getReportTypes() {
        return Collections.emptySet();
    }

    protected void loadLiveProperties(DavPropertySet properties) {
        properties.add(new DisplayName(getDisplayName()));
        properties.add(new ResourceType(getResourceTypes()));
        properties.add(new IsCollection(isCollection()));
        //TODO properties.add(new CurrentUserPrincipal(getResourceLocator(),  getSecurityManager().getSecurityContext().getUser()));
    }
}