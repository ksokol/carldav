/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.security.util;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.filter.NoteItemFilter;
import org.unitedinternet.cosmo.security.CosmoSecurityContext;

/**
 * Contains methods that help determine if a
 * security context has sufficient privileges for certain
 * resources.
 */
public class SecurityHelper {

    /**
     * Determines if the current security context has access to
     * User.  The context must either be the user, or have admin access.
     * @param context security context
     * @param user user
     * @return true if the security context has sufficient privileges
     *         to view user
     */
    public boolean hasUserAccess(CosmoSecurityContext context, User user) {
        if(context.getUser()==null) {
            return false;
        }
        
        if(context.getUser().getAdmin()) {
            return true;
        }
        
        if(context.getUser().equals(user)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * @param context security context
     * @param filter item filter
     * @return true if the 
     */
    public boolean hasAccessToFilter(CosmoSecurityContext context, ItemFilter filter) {
        // admin has access to everything
        if(context.getUser()!=null && context.getUser().getAdmin().booleanValue()) {
            return true;
        }
        
        // Otherwise require read access to parent or note
        if(filter.getParent()!=null) {
            return hasReadAccess(context, filter.getParent());
        }
        
        if (filter instanceof NoteItemFilter) {
            NoteItemFilter nif = (NoteItemFilter) filter;
            if (nif.getMasterNoteItem() != null) {
                return hasReadAccess(context, nif.getMasterNoteItem());
            }
        }
        
        // otherwise no access
        return false;
    }
    
    /**
     * @param context security context
     * @param item existing item
     * @return true if the security context has sufficient privileges
     *         to view the item
     */
    public boolean hasReadAccess(CosmoSecurityContext context, Item item) {
        if(context.getUser()!=null) {
            return hasReadAccess(context.getUser(), item);
        }

        return false;
    }
    
   
    public boolean hasWriteTicketAccess(CosmoSecurityContext context, Item item) {
        if(context.getUser()==null) {
            return false;
        }
        
        if(item.getOwner().equals(context.getUser())) {
            return true;
        }
       
        return false;
    }
    
    /**
     * @param context security context
     * @param item existing item
     * @return true if the security context has sufficient privileges
     *         to update the item
     */
    public boolean hasWriteAccess(CosmoSecurityContext context, Item item) {
        if(context.getUser()!=null) {
            return hasWriteAccess(context.getUser(), item);
        }

        return false;
    }
    
    private boolean hasReadAccess(User user, Item item) {
        // admin always has access
        if(user.getAdmin()!=null && user.getAdmin().booleanValue()) {
            return true;
        }
        
        // Case 1. User owns item
        if(item.getOwner().equals(user)) {
            return true;
        }
        
        // Case 2: User owns collection that item is in
        for(CollectionItem parent: item.getParents()) {
            if(parent.getOwner().equals(user)) {
                return true;
            }
        }

        // otherwise no access
        return false;
    }

    private boolean hasWriteAccess(User user, Item item) {
        // admin always has access
        if(user.getAdmin()!=null && user.getAdmin().booleanValue()) {
            return true;
        }
        
        // Case 1. User owns item
        if(item.getOwner().equals(user)) {
            return true;
        }
        
        // Case 2: User owns collection that item is in
        for(CollectionItem parent: item.getParents()) {
            if(parent.getOwner().equals(user)) {
                return true;
            }
        }

        // otherwise no access
        return false;
    }
    
}
