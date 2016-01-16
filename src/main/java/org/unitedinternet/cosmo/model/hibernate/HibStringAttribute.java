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

import org.hibernate.validator.constraints.Length;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("string")
public class HibStringAttribute extends HibAttribute {

    private static final long serialVersionUID = 1L;
    
    @Column(name="stringvalue")
    @Length(min=0)
    private String value;

    public HibStringAttribute() {
    }

    public HibStringAttribute(QName qname, String value) {
        setQName(qname);
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public void setValue(Object value) {
        if (value != null && !(value instanceof String)) {
            throw new ModelValidationException(
                    "attempted to set non String value on attribute");
        }
        setValue((String) value);
    }

    /**
     * Convienence method for returning a String value on a StringAttribute
     * with a given QName stored on the given item.
     * @param item item to fetch StringAttribute from
     * @param qname QName of attribute
     * @return String value of StringAttribute
     */
    public static String getValue(Item item, QName qname) {
        HibStringAttribute ta = (HibStringAttribute) item.getAttribute(qname);
        if(ta==null) {
            return null;
        }
        else {
            return ta.getValue();
        }
    }
    
    /**
     * Convienence method for setting a String value on a StringAttribute
     * with a given QName stored on the given item.
     * @param item item to fetch StringAttribute from
     * @param qname QName of attribute
     * @param value value to set on StringAttribute
     */
    public static void setValue(Item item, QName qname, String value) {
        HibStringAttribute attr = (HibStringAttribute) item.getAttribute(qname);
        if(attr==null && value!=null) {
            attr = new HibStringAttribute(qname,value);
            item.addAttribute(attr);
            return;
        }
        if(value==null) {
            item.removeAttribute(qname);
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
