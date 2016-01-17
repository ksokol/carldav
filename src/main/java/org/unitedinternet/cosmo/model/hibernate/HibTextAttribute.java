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
import org.hibernate.annotations.Type;
import org.unitedinternet.cosmo.CosmoIOException;
import org.unitedinternet.cosmo.dao.ModelValidationException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("text")
public class HibTextAttribute extends HibAttribute {

    private static final long serialVersionUID = 1L;
    
    @Column(name="textvalue", length= 2147483647)
    @Type(type="materialized_clob")
    private String value;

    public HibTextAttribute() {
    }

    public HibTextAttribute(HibQName qname, String value) {
        setQName(qname);
        this.value = value;
    }

    public HibTextAttribute(HibQName qname, Reader reader) {
        setQName(qname);
        this.value = read(reader);
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getLength() {
        if(value!=null) {
            return value.length();
        }
        else {
            return 0;
        }
    }
    
    public void setValue(Object value) {
        if (value != null && !(value instanceof String) &&
            !(value instanceof Reader)) {
            throw new ModelValidationException(
                    "attempted to set non String or Reader value on attribute");
        }
        if (value instanceof Reader) {
            setValue(read((Reader) value));
        } else {
            setValue((String) value);
        }
    }

    private String read(Reader reader) {
        if(reader==null) {
            return null;
        }
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(reader, writer);
        } catch (IOException e) {
            throw new CosmoIOException("error reading stream", e);
        }
        return writer.toString();
    }
    
    /**
     * Convienence method for returning a String value on a TextAttribute
     * with a given QName stored on the given item.
     * @param hibItem item to fetch TextAttribute from
     * @param qname QName of attribute
     * @return String value of TextAttribute
     */
    public static String getValue(HibItem hibItem, HibQName qname) {
        HibTextAttribute ta = (HibTextAttribute) hibItem.getAttribute(qname);
        if(ta==null) {
            return null;
        }
        else {
            return ta.getValue();
        }
    }
    
    /**
     * Convienence method for setting a String value on a TextAttribute
     * with a given QName stored on the given item.
     * @param hibItem item to fetch TextAttribute from
     * @param qname QName of attribute
     * @param value value to set on TextAttribute
     */
    public static void setValue(HibItem hibItem, HibQName qname, String value) {
        HibTextAttribute attr = (HibTextAttribute) hibItem.getAttribute(qname);
        if(attr==null && value!=null) {
            attr = new HibTextAttribute(qname,value);
            hibItem.addAttribute(attr);
            return;
        }
        if(value==null) {
            hibItem.removeAttribute(qname);
        }
        else {
            attr.setValue(value);
        }
    }
    
    /**
     * Convienence method for setting a Reader value on a TextAttribute
     * with a given QName stored on the given item.
     * @param hibItem item to fetch TextAttribute from
     * @param qname QName of attribute
     * @param value value to set on TextAttribute
     */
    public static void setValue(HibItem hibItem, HibQName qname, Reader value) {
        HibTextAttribute attr = (HibTextAttribute) hibItem.getAttribute(qname);
        if(attr==null && value!=null) {
            attr = new HibTextAttribute(qname,value);
            hibItem.addAttribute(attr);
            return;
        }
        if(value==null) {
            hibItem.removeAttribute(qname);
        }
        else {
            attr.setValue(value);
        }
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }
}
