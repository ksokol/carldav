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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.MessageStamp;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.Stamp;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


/**
 * Hibernate persistent MessageStamp.
 */
@Entity
@DiscriminatorValue("message")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HibMessageStamp extends HibStamp implements MessageStamp {

    private static final long serialVersionUID = -6100568628972081120L;

    public static final QName ATTR_MESSAGE_FROM = new HibQName(
            MessageStamp.class, "from");
    
    public static final QName ATTR_MESSAGE_TO = new HibQName(
            MessageStamp.class, "to");
    
    public static final QName ATTR_MESSAGE_CC = new HibQName(
            MessageStamp.class, "cc");
    
    public static final QName ATTR_MESSAGE_BCC = new HibQName(
            MessageStamp.class, "bcc");

    /** default constructor */
    public HibMessageStamp() {
    }
    
    public HibMessageStamp(Item item) {
        setItem(item);
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Stamp#getType()
     */
    public String getType() {
        return "message";
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#getFrom()
     */
    public String getFrom() {
        // from stored as StringAttribute on Item
        return HibStringAttribute.getValue(getItem(), ATTR_MESSAGE_FROM);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#setFrom(java.lang.String)
     */
    public void setFrom(String from) {
        // from stored as StringAttribute on Item
        HibStringAttribute.setValue(getItem(), ATTR_MESSAGE_FROM, from);
        updateTimestamp();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#getTo()
     */
    public String getTo() {
        // to stored as StringAttribute on Item
        return HibStringAttribute.getValue(getItem(), ATTR_MESSAGE_TO);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#setTo(java.lang.String)
     */
    public void setTo(String to) {
        // to stored as StringAttribute on Item
        HibStringAttribute.setValue(getItem(), ATTR_MESSAGE_TO, to);
        updateTimestamp();
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#getBcc()
     */
    public String getBcc() {
        // bcc stored as StringAttribute on Item
        return HibStringAttribute.getValue(getItem(), ATTR_MESSAGE_BCC);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#setBcc(java.lang.String)
     */
    public void setBcc(String bcc) {
        //bcc stored as StringAttribute on Item
        HibStringAttribute.setValue(getItem(), ATTR_MESSAGE_BCC, bcc);
        updateTimestamp();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#getCc()
     */
    public String getCc() {
        // cc stored as StringAttribute on Item
        return HibStringAttribute.getValue(getItem(), ATTR_MESSAGE_CC);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#setCc(java.lang.String)
     */
    public void setCc(String cc) {
        // cc stored as StringAttribute on Item
        HibStringAttribute.setValue(getItem(), ATTR_MESSAGE_CC, cc);
        updateTimestamp();
    }

    /**
     * Return MessageStamp from Item
     * @param item
     * @return MessageStamp from Item
     */
    public static MessageStamp getStamp(Item item) {
        return (MessageStamp) item.getStamp(MessageStamp.class);
    }
    
    public Stamp copy() {
        MessageStamp stamp = new HibMessageStamp();
        return stamp;
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }
}
