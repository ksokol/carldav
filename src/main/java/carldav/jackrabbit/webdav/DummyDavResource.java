package carldav.jackrabbit.webdav;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.lock.ActiveLock;
import org.apache.jackrabbit.webdav.lock.LockInfo;
import org.apache.jackrabbit.webdav.lock.LockManager;
import org.apache.jackrabbit.webdav.lock.Scope;
import org.apache.jackrabbit.webdav.lock.Type;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.PropEntry;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.WebDavResource;

import java.io.IOException;
import java.util.List;

/**
 * @author Kamill Sokol
 */
@Deprecated
public class DummyDavResource implements DavResource, WebDavResource {

    private final WebDavResource resource;

    public DummyDavResource(final WebDavResource resource) {
        this.resource = resource;
    }

    @Override
    public DavCollection getParent() throws CosmoDavException {
        return resource.getParent();
    }

    @Override
    public void removeMember2(final WebDavResource member) throws DavException {

    }

    @Override
    public void writeTo(final OutputContext out) throws CosmoDavException, IOException {

    }

    @Override
    public Report getReport(final ReportInfo info) throws CosmoDavException {
        return null;
    }

    @Override
    public DavResourceLocator getResourceLocator() {
        return null;
    }

    @Override
    public String getETag() {
        return null;
    }

    @Override
    public List<WebDavResource> getMembers2() {
        return null;
    }

    @Override
    public String getComplianceClass() {
        return null;
    }

    @Override
    public String getSupportedMethods() {
        return null;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public org.apache.jackrabbit.webdav.DavResourceLocator getLocator() {
        return null;
    }

    @Override
    public String getResourcePath() {
        return null;
    }

    @Override
    public String getHref() {
        return resource.getHref();
    }

    @Override
    public long getModificationTime() {
        return 0;
    }

    @Override
    public void spool(final OutputContext outputContext) throws IOException {

    }

    @Override
    public DavPropertyName[] getPropertyNames() {
        return resource.getPropertyNames();
    }

    @Override
    public DavProperty<?> getProperty(final DavPropertyName name) {
        return resource.getProperty(name);
    }

    @Override
    public DavPropertySet getProperties() {
        return resource.getProperties();
    }

    @Override
    public void setProperty(final DavProperty<?> property) throws DavException {

    }

    @Override
    public void removeProperty(final DavPropertyName propertyName) throws DavException {

    }

    @Override
    public MultiStatusResponse alterProperties(final List<? extends PropEntry> changeList) throws DavException {
        return null;
    }

    @Override
    public void addMember(final DavResource resource, final InputContext inputContext) throws DavException {

    }

    @Override
    public void removeMember(final DavResource member) throws DavException {

    }

    @Override
    public void move(final DavResource destination) throws DavException {

    }

    @Override
    public void copy(final DavResource destination, final boolean shallow) throws DavException {

    }

    @Override
    public boolean isLockable(final Type type, final Scope scope) {
        return false;
    }

    @Override
    public boolean hasLock(final Type type, final Scope scope) {
        return false;
    }

    @Override
    public ActiveLock getLock(final Type type, final Scope scope) {
        return null;
    }

    @Override
    public ActiveLock[] getLocks() {
        return new ActiveLock[0];
    }

    @Override
    public ActiveLock lock(final LockInfo reqLockInfo) throws DavException {
        return null;
    }

    @Override
    public ActiveLock refreshLock(final LockInfo reqLockInfo, final String lockToken) throws DavException {
        return null;
    }

    @Override
    public void unlock(final String lockToken) throws DavException {

    }

    @Override
    public void addLockManager(final LockManager lockmgr) {

    }

    @Override
    public DavResourceFactory getFactory() {
        return null;
    }

    @Override
    public DavSession getSession() {
        return null;
    }
}
