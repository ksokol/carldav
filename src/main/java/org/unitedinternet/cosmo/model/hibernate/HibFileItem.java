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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
@DiscriminatorValue("file")
public class HibFileItem extends HibItem {

    private static final long serialVersionUID = 3L;

    @Column(name = "contentType", length=64)
    private String contentType = null;

    @Column(name = "content", columnDefinition="CLOB")
    @Lob
    private String contentData;

    public String getContent() {
        return contentData;
    }

    public void setContent(String content) {
        this.contentData = content;
    }

    public Long getContentLength() {
        return contentData == null ? 0L : (long) contentData.length();
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
