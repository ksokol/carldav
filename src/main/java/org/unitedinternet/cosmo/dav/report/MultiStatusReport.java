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

import carldav.jackrabbit.webdav.CustomMultiStatus;
import carldav.jackrabbit.webdav.CustomMultiStatusResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

/**
 * Base class for WebDAV reports that return multistatus responses.
 */
public abstract class MultiStatusReport extends ReportBase {

    private CustomMultiStatus multistatus = new CustomMultiStatus();
    protected int propfindType = PROPFIND_ALL_PROP;
    private DavPropertyNameSet propfindProps;

    public final boolean isMultiStatusReport() {
        return true;
    }

    /**
     * Generates and writes the multistatus response.
     */
    protected void output(HttpServletResponse response)
            throws CosmoDavException {
        try {
            buildMultistatus();
            sendXmlResponse(response, multistatus, 207);
        } catch (Exception e) {
            throw new CosmoDavException(e);
        }
    }

    public final void buildMultistatus() throws CosmoDavException {

        DavPropertyNameSet resultProps = createResultPropSpec();

        for (WebDavResource result : getResults()) {
            CustomMultiStatusResponse msr = buildMultiStatusResponse(result, resultProps);
            multistatus.addResponse2(msr);
        }
    }

    protected DavPropertyNameSet createResultPropSpec() {
        return new DavPropertyNameSet(propfindProps);
    }

    /**
     * Returns a <code>MultiStatusResponse</code> describing the
     * specified resource including the specified properties.
     */
    protected CustomMultiStatusResponse buildMultiStatusResponse(WebDavResource resource, DavPropertyNameSet props) {
        if (props.isEmpty()) {
            String href = resource.getResourceLocator().
                    getHref(resource.isCollection());
            return new CustomMultiStatusResponse(href, 200);
        }
        return new CustomMultiStatusResponse(resource, props, propfindType);
    }

    protected MultiStatus getMultiStatus() {
        return multistatus;
    }

    public final Element toXml(Document document) {
        try {
            runQuery();
        } catch (CosmoDavException e) {
            throw new RuntimeException(e);
        }

        return multistatus.toXml(document);
    }

    public void setPropFindType(int type) {
        this.propfindType = type;
    }

    public DavPropertyNameSet getPropFindProps() {
        return propfindProps;
    }

    public void setPropFindProps(DavPropertyNameSet props) {
        this.propfindProps = props;
    }

    private void sendXmlResponse(HttpServletResponse httpResponse, XmlSerializable serializable, int status) {
        httpResponse.setStatus(status);

        if (serializable != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                Document doc = DomUtil.createDocument();
                doc.appendChild(serializable.toXml(doc));

                // JCR-2636: Need to use an explicit OutputStreamWriter
                // instead of relying on the built-in UTF-8 serialization
                // to avoid problems with surrogate pairs on Sun JRE 1.5.
                Writer writer = new OutputStreamWriter(out, "UTF-8");
                DomUtil.transformDocument(doc, writer);
                writer.flush();

                // TODO: Should this be application/xml? See JCR-1621
                httpResponse.setContentType("text/xml; charset=UTF-8");
                httpResponse.setContentLength(out.size());
                out.writeTo(httpResponse.getOutputStream());
            } catch (Exception e) {
                throw new CosmoDavException(e);
            }
        }
    }
}
