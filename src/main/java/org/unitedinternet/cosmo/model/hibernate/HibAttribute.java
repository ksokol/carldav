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

import org.hibernate.annotations.Target;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
// Define indexes on discriminator and key fields
@Table(
        name="attribute")
@DiscriminatorColumn(
        name="attributetype",
        discriminatorType=DiscriminatorType.STRING,
        length=16)
public abstract class HibAttribute extends HibAuditableObject {

    private static final long serialVersionUID = 1L;

    @Target(String.class)
    private String qname;
    
    @ManyToOne(targetEntity=HibItem.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "itemid", nullable = false)
    private HibItem item;
}
