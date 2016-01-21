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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Entity
@DiscriminatorValue("collection")
public class HibCollectionItem extends HibItem {

    private static final long serialVersionUID = 1L;

    @OneToMany(targetEntity=HibCollectionItemDetails.class, mappedBy="collection", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.DELETE }) 
    private Set<HibCollectionItemDetails> childDetails = new HashSet<>();

    public Set<HibItem> getChildren() {
        final Set<HibItem> children = new HashSet<>();
        for(HibCollectionItemDetails cid: childDetails) {
            children.add(cid.getItem());
        }
        return children;
    }
}
