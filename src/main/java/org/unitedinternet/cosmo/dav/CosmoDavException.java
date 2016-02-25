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

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class CosmoDavException extends RuntimeException implements ExtendedDavConstants {

    private static final long serialVersionUID = 2980452139790396998L;
    private transient DavNamespaceContext nsc;

    private int errorCode = INTERNAL_SERVER_ERROR.value();

    public CosmoDavException(int code) {
        this(code, null, null);
    }

    public CosmoDavException(String message) {
        this(500, message, null);
    }

    public CosmoDavException(int code,
                        String message) {
        this(code, message, null);
    }

    public CosmoDavException(Throwable t) {
        this(500, t.getMessage(), t);
    }

    public CosmoDavException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        nsc = new DavNamespaceContext();
    }

    public int getErrorCode() {
        return errorCode;
    }

    public boolean hasContent() {
        return true;
    }

    public DavNamespaceContext getNamespaceContext() {
        return nsc;
    }

    public void writeTo(XMLStreamWriter writer)
        throws XMLStreamException {
        writer.setNamespaceContext(nsc);
        writer.writeStartElement("DAV:", "error");
        for (String uri : nsc.getNamespaceURIs()) {
            writer.writeNamespace(nsc.getPrefix(uri), uri);
        }
        writeContent(writer);
        writer.writeEndElement();
    }

    protected void writeContent(XMLStreamWriter writer)
        throws XMLStreamException {
        writer.writeStartElement(NS_COSMO, "internal-server-error");
        if (getMessage() != null) {
            writer.writeCharacters(getMessage());
        }
        writer.writeEndElement();
    }

    public static class DavNamespaceContext implements NamespaceContext {
        private HashMap<String,String> uris;
        private HashMap<String,HashSet<String>> prefixes;

        /**
         * Constructor.
         */
        public DavNamespaceContext() {
            uris = new HashMap<>();
            uris.put("D", "DAV:");
            uris.put(PRE_COSMO, NS_COSMO);

            prefixes = new HashMap<>();

            HashSet<String> dav = new HashSet<>(1);
            dav.add("D");
            prefixes.put("DAV:", dav);

            HashSet<String> cosmo = new HashSet<>(1);
            cosmo.add(PRE_COSMO);
            prefixes.put(NS_COSMO, cosmo);
        }

        // NamespaceContext methods

        public String getNamespaceURI(String prefix) {
            return uris.get(prefix);
        }

        public String getPrefix(String namespaceURI) {
            if (prefixes.get(namespaceURI) == null) {
                return null;
            }
            return prefixes.get(namespaceURI).iterator().next();
        }

        public Iterator getPrefixes(String namespaceURI) {
            if (prefixes.get(namespaceURI) == null) {
                return null;
            }
            return prefixes.get(namespaceURI).iterator();
        }

        // our methods

        public Set<String> getNamespaceURIs() {
            return prefixes.keySet();
        }

        public void addNamespace(String prefix, String namespaceURI) {
            uris.put(prefix, namespaceURI);

            HashSet<String> ns = new HashSet<>(1);
            ns.add(prefix);
            prefixes.put(namespaceURI, ns);
        }
    }
}
