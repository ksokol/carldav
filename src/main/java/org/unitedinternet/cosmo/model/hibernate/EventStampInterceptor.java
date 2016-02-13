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

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import java.io.Serializable;

/**
 * Hibernate Interceptor that updates BaseEventStamp timeRangeIndexes.
 */
public class EventStampInterceptor extends EmptyInterceptor {

    private static final long serialVersionUID = 1L;

    private final EntityConverter entityConverter;

    public EventStampInterceptor(final EntityConverter entityConverter) {
        this.entityConverter = entityConverter;
    }

    @Override
    public boolean onFlushDirty(Object object, Serializable id, Object[] currentState,
            Object[] previousState, String[] propertyNames, Type[] types) {
        if(! (object instanceof HibBaseEventStamp)) {
            return false;
        }
        
        // calculate time-range-index
        HibBaseEventStamp es = (HibBaseEventStamp) object;
        HibEventTimeRangeIndex index = entityConverter.calculateEventStampIndexes(es);
        
        if(index==null) {
            return false;
        }
        
        // update modifiedDate and entityTag
        for ( int i=0; i < propertyNames.length; i++ ) {
            if ( "timeRangeIndex".equals( propertyNames[i] ) ) {
                currentState[i] = index;
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean onSave(Object object, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        
        if(! (object instanceof HibBaseEventStamp)) {
            return false;
        }
        
        // calculate time-range-index
        HibBaseEventStamp es = (HibBaseEventStamp) object;
        HibEventTimeRangeIndex index = entityConverter.calculateEventStampIndexes(es);
        
        if(index==null) {
            return false;
        }
        
        // update modifiedDate and entityTag
        for ( int i=0; i < propertyNames.length; i++ ) {
            if ( "timeRangeIndex".equals( propertyNames[i] ) ) {
                state[i] = index;
                return true;
            }
        }
        
        return false;
    }
}
