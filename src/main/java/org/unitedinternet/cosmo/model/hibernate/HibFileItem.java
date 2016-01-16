/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.model.hibernate;

import org.apache.commons.io.IOUtils;
import org.unitedinternet.cosmo.CosmoIOException;
import org.unitedinternet.cosmo.model.DataSizeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
@DiscriminatorValue("file")
public class HibFileItem extends HibContentItem {

    private static final long serialVersionUID = 1L;

    public static final long MAX_CONTENT_SIZE = 10 * 1024 * 1024;

    @Column(name = "contentType", length=64)
    private String contentType = null;
    
    @Column(name = "contentLanguage", length=32)
    private String contentLanguage = null;
    
    @Column(name = "contentEncoding", length=32)
    private String contentEncoding = null;
    
    @Column(name = "contentLength")
    private Long contentLength = null;
    
    @OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinColumn(name="contentdataid")
    private HibContentData contentData = null;

    public byte[] getContent() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            InputStream contentStream = contentData.getContentInputStream();
            IOUtils.copy(contentStream, bos);
            contentStream.close();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new CosmoIOException("Error getting content", e);
        }
    }

    public void setContent(byte[] content) {
        if (content.length > MAX_CONTENT_SIZE) {
            throw new DataSizeException("Item content too large");
        }
        
        try {
            setContent(new ByteArrayInputStream(content));
        } catch (IOException e) {
            throw new CosmoIOException("Error setting content", e);
        }
    }

    public void setContent(InputStream is) throws IOException {
        if(contentData==null) {
            contentData = new HibContentData(); 
        }
        
        contentData.setContentInputStream(is);
        
        // Verify size is not greater than MAX.
        // TODO: do this checking in ContentData.setContentInputStream()
        if (contentData.getSize() > MAX_CONTENT_SIZE) {
            throw new DataSizeException("Item content too large");
        }
        
        setContentLength(contentData.getSize());
    }

    public InputStream getContentInputStream() {
        if(contentData==null) {
            return null;
        }
        else {
            return contentData.getContentInputStream();
        }
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public String getContentLanguage() {
        return contentLanguage;
    }

    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
