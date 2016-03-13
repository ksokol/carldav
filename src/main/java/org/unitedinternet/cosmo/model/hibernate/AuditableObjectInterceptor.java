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

import carldav.service.time.TimeService;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Date;

public class AuditableObjectInterceptor extends EmptyInterceptor {

    private static final long serialVersionUID = 2206186604411196082L;

    private final TimeService timeService;

    public AuditableObjectInterceptor(final TimeService timeService) {
        Assert.notNull(timeService, "timeService is null");
        this.timeService = timeService;
    }

    @Override
    public boolean onFlushDirty(Object object, Serializable id, Object[] currentState,
            Object[] previousState, String[] propertyNames, Type[] types) {
        return onSave(object, id, currentState, propertyNames, types);
    }

    @Override
    public boolean onSave(Object object, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if(! (object instanceof HibAuditableObject)) {
            return false;
        }

        HibAuditableObject ao = (HibAuditableObject) object;
        Date curDate = timeService.getCurrentTime();

        for ( int i=0; i < propertyNames.length; i++ ) {
            if ("modifiedDate".equals(propertyNames[i])) {
                state[i] = curDate;
            } else if("etag".equals( propertyNames[i] )) {
                state[i] = ao.calculateEntityTag();
            }
        }
        return true;
    }
}
