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

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Base class for model objects.
 */
@Access(AccessType.FIELD)
@MappedSuperclass
public abstract class BaseModelObject implements Serializable {

    private static final long serialVersionUID = 8396186357498363587L;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id = Long.valueOf(-1);

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
