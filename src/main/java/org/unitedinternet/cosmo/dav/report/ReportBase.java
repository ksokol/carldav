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
package org.unitedinternet.cosmo.dav.report;

import carldav.jackrabbit.webdav.CustomDomUtils;
import carldav.jackrabbit.webdav.CustomReport;
import carldav.jackrabbit.webdav.CustomReportInfo;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Base class for WebDAV reports.
 * </p>
 */
public abstract class ReportBase implements CustomReport, ExtendedDavConstants {

    private WebDavResource resource;
    private CustomReportInfo info;
    private HashSet<WebDavResource> results;

    public void init(WebDavResource resource,
                     CustomReportInfo info)
            throws CosmoDavException {
        this.resource = resource;
        this.info = info;
        this.results = new HashSet<>();
        parseReport(info);
    }

    public void run(HttpServletResponse response) throws CosmoDavException {
        runQuery();
        output(response);
    }

    protected abstract void parseReport(CustomReportInfo info)
            throws CosmoDavException;

    /**
     * <p>
     * Executes the report query and stores the result.
     * Calls the following methods:
     * </p>
     * <ol>
     * <li> {@link #doQuerySelf(WebDavResource)} on the targeted resource </li>
     * <li> {@link #doQueryChildren(DavCollection)} if the targeted resource is
     * a collection and the depth is 1 or Infinity </li>
     * <li> {@link #doQueryDescendents(DavCollection)} if the targeted resource
     * is a collection and the depth is Infinity</li>
     * </ol>
     */
    protected void runQuery()
            throws CosmoDavException {
        doQuerySelf(resource);

        if (info.getDepth() == DEPTH_0) {
            return;
        }

        if (!(resource instanceof DavCollection)) {
            throw new BadRequestException("Report may not be run with depth " +
                    info.getDepth() + " against a non-collection resource");
        }
        DavCollection collection = (DavCollection) resource;

        doQueryChildren(collection);
        if (info.getDepth() == DEPTH_1) {
            return;
        }
        
        doQueryDescendents(collection);
    }

    /**
     * Writes the report result to the response.
     */
    protected abstract void output(HttpServletResponse response)
            throws CosmoDavException;

    /**
     * Performs the report query on the specified resource.
     */
    protected abstract void doQuerySelf(WebDavResource resource)
            throws CosmoDavException;

    /**
     * Performs the report query on the specified collection's children.
     */
    protected abstract void doQueryChildren(DavCollection collection)
            throws CosmoDavException;

    /**
     * Performs the report query on the descendents of the specified collection.
     * Should recursively call the method against each of the children of the
     * provided collection that are themselves collections.
     */
    protected void doQueryDescendents(DavCollection collection) throws CosmoDavException {
        for (final WebDavResource member : collection.getCollectionMembers()) {
            if (member.isCollection()) {
                DavCollection dc = (DavCollection) member;
                doQuerySelf(dc);
                doQueryChildren(dc);
                doQueryDescendents(dc);
            }
        }
    }

    protected static Element getReportElementFrom(CustomReportInfo reportInfo) {
        if (reportInfo == null) {
            return null;
        }

        if(reportInfo instanceof CustomReportInfo) {
            CustomReportInfo customReportInfo = reportInfo;
            return customReportInfo.getDocumentElement();
        }

        Document document = CustomDomUtils.createDocument();
        return reportInfo.toXml(document);
    }

    public WebDavResource getResource() {
        return resource;
    }

    public CustomReportInfo getInfo() {
        return info;
    }

    public Set<WebDavResource> getResults() {
        return results;
    }
}
