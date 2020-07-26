package org.unitedinternet.cosmo.dav.caldav.report;

import carldav.jackrabbit.webdav.DavConstants;
import carldav.jackrabbit.webdav.MultiStatusResponse;
import carldav.jackrabbit.webdav.version.report.ReportInfo;
import carldav.jackrabbit.webdav.version.report.ReportType;
import carldav.jackrabbit.webdav.xml.DomUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.util.UriUtils;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.impl.DavCalendarResource;
import org.unitedinternet.cosmo.dav.impl.DavItemResourceBase;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static carldav.jackrabbit.webdav.DavConstants.HREF;
import static carldav.jackrabbit.webdav.DavConstants.PROPFIND_ALL_PROP;
import static carldav.jackrabbit.webdav.DavConstants.PROPFIND_BY_PROPERTY;
import static carldav.jackrabbit.webdav.DavConstants.PROPFIND_PROPERTY_NAMES;
import static carldav.jackrabbit.webdav.DavConstants.XML_HREF;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.ELEMENT_CALDAV_CALENDAR_MULTIGET;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.NS_CALDAV;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.PRE_CALDAV;

public class MultigetReport extends CaldavMultiStatusReport {

    private static final Pattern HREF_EMAIL_PATTERN = Pattern.compile("/(([\\w-\\.]+)(@|%40)((?:[\\w]+\\.)+)([a-zA-Z]{2,}))(/?)");
    private static final Pattern RESOURCE_UUID_PATTERN = Pattern.compile("/\\{?\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}\\}?");
    public static final ReportType REPORT_TYPE_CALDAV_MULTIGET =
            ReportType.register(new QName(NS_CALDAV, ELEMENT_CALDAV_CALENDAR_MULTIGET, PRE_CALDAV), MultigetReport.class);

    private Set<String> hrefs;

    public ReportType getType() {
        return REPORT_TYPE_CALDAV_MULTIGET;
    }

    protected void parseReport(ReportInfo info) {
        if (!getType().isRequestedReportType(info)) {
            throw new CosmoDavException("Report not of type " + getType().getReportName());
        }

        setPropFindProps(info.getPropertyNameSet());
        if (info.containsContentElement(DavConstants.ALLPROP)) {
            setPropFindType(PROPFIND_ALL_PROP);
        } else if (info.containsContentElement(DavConstants.PROPNAME)) {
            setPropFindType(PROPFIND_PROPERTY_NAMES);
        } else {
            setPropFindType(PROPFIND_BY_PROPERTY);
            setOutputFilter(findOutputFilter(info));
        }

        var hrefElements = info.getContentElements(HREF);
        if (hrefElements.isEmpty()) {
            throw new BadRequestException("Expected at least one " + XML_HREF);
        }
        if (getResource() instanceof DavItemResourceBase && hrefElements.size() > 1) {
            throw new BadRequestException("Expected at most one " + XML_HREF);
        }

        var resourceUrl = getResource(). getResourceLocator().getUrl(true, getResource().isCollection());
        String resourceUUID = null;
        var resourceUUIDMatcher = RESOURCE_UUID_PATTERN.matcher(resourceUrl.getPath());
        if (resourceUUIDMatcher.find()) {
            resourceUUID = resourceUUIDMatcher.group(0);
        }
        hrefs = new HashSet<>();
        for (var element : hrefElements) {
            var href = DomUtils.getTextTrim(element);
            href = updateHrefElementWithRequestUrlUUID(element, href, resourceUUID);
            // validate and absolutize submitted href
            var memberUrl = normalizeHref(resourceUrl, href);

            // check if the href refers to the targeted resource (or to a
            // descendent if the target is a collection)
            if (getResource() instanceof DavCollection) {
                if (!isDescendentOrEqual(resourceUrl, memberUrl)) {
                    throw new BadRequestException("Href " + href + " does not refer to the requested collection " + resourceUrl + " or a descendent");
                }
            } else {
                if (!memberUrl.toString().equals(resourceUrl.toString())) {
                    throw new BadRequestException("Href " + href + " does not refer to the requested resource " + resourceUrl);
                }
            }

            // use the absolute path of our normalized URL as the href
            hrefs.add(memberUrl.getPath());
        }
    }

    private static String updateHrefElementWithRequestUrlUUID (Element hrefElement, String davHref, String resourceUUID) {
        if (StringUtils.isNotEmpty(resourceUUID)) {
            var davEmailMatcher = HREF_EMAIL_PATTERN.matcher(davHref);
            if (davEmailMatcher.find()) {
                var email = davEmailMatcher.group(0);
                var newHref = davHref.replaceFirst(email.replaceAll("/",""),resourceUUID.replaceAll("/",""));
                hrefElement.getFirstChild().setNodeValue(newHref);
                return newHref;
            }
        }
        return davHref;
    }

    protected void doQuerySelf(WebDavResource resource) {
        //no implementation
    }

    protected void doQueryChildren(DavCollection collection) {
        //no implementation
    }

    protected void runQuery() {
        var propspec = createResultPropSpec();

        if (getResource() instanceof DavCollection) {
            var collection = (DavCollection) getResource();
            for (var href : hrefs) {
                var target = collection.findMember(href);

                var multiStatus = getMultiStatus();
                if (target != null) {
                    multiStatus.addResponse(buildMultiStatusResponse(target, propspec));
                }
                else {
                    multiStatus.addResponse(new MultiStatusResponse(href, 404));
                }
            }
            return;
        }

        if (getResource() instanceof DavCalendarResource) {
            var multiStatus = getMultiStatus();
            multiStatus.addResponse(buildMultiStatusResponse(getResource(), propspec));
            return;
        }

        throw new UnprocessableEntityException(getType() + " report not supported for non-calendar resources");
    }

    private static URL normalizeHref(URL context, String href) {
        URL url = null;
        try {
            url = new URL(context, href);
            // check that the URL is escaped. it's questionable whether or
            // not we should all unescaped URLs, but at least as of
            // 10/02/2007, iCal 3.0 generates them
            url.toURI();
            return url;
        } catch (URISyntaxException e) {
            try {
                var escaped = new URI(url.getProtocol(), url.getAuthority(), url.getPath(), url.getQuery(), url.getRef());
                return new URL(escaped.toString());
            } catch (URISyntaxException | MalformedURLException e2) {
                throw new BadRequestException("Malformed unescaped href " + href + ": " + e.getMessage());
            }
        } catch (MalformedURLException e) {
            throw new BadRequestException("Malformed href " + href + ": " + e.getMessage());
        }
    }

    private static boolean isDescendentOrEqual(URL collection, URL test) {
        if (collection == null || test == null) {
            return false;
        }
        if (collection.toString().equals(test.toString())) {
            return true;
        }

        var testPathDecoded = UriUtils.decode(test.getPath(), "UTF-8");
        var collectionPathDecoded = UriUtils.decode(collection.getPath(), "UTF-8");

        return testPathDecoded.startsWith(collectionPathDecoded);
    }
}
