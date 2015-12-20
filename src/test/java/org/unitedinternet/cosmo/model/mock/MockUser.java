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
package org.unitedinternet.cosmo.model.mock;

import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.User;

public class MockUser implements User {

    public static final int EMAIL_LEN_MIN = 1;
    public static final int EMAIL_LEN_MAX = 128;

    private Long id;
    private String password;
    private String email;
    private Boolean locked = Boolean.FALSE;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#getPassword()
     */
    /**
     * Gets password.
     * @return The password.
     */
    public final String getPassword() {
        return password;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#setPassword(java.lang.String)
     */
    /**
     * Sets password.
     * @param password The password.
     */
    public final void setPassword(final String password) {
        this.password = password;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#getEmail()
     */
    /**
     * Gets email.
     * @return The email.
     */
    public final String getEmail() {
        return email;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#setEmail(java.lang.String)
     */
    /**
     * Sets email.
     * @param email The email.
     */
    public final void setEmail(final String email) {
        this.email = email;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#isLocked()
     */
    /**
     * Is locked.
     * @return The boolean for it is locked or not.
     */
    public final boolean isLocked() {
        return locked;
    }

    @Override
    public String getRoles() {
        return null;
    }

    @Override
    public void setRoles(final String roles) {

    }

    /**
     * Username determines equality
     * @param obj The object.
     * @return The equals boolean. 
     */
    @Override
    public final boolean equals(final Object obj) {
        if (obj == null || email == null) {
            return false;
        }
        if (! (obj instanceof User)) {
            return false;
        }
        
        return email.equals(((User) obj).getEmail());
    }

    /**
     * Hashcode.
     * {@inheritDoc}
     * @return The hashCode.
     */
    @Override
    public final int hashCode() {
    if (email == null) {
        return super.hashCode();
    }
    else {
        return email.hashCode();
    }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#validate()
     */
    /**
     * Validate.
     */
    public final void validate() {
        validateEmail();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#validateEmail()
     */
    /**
     * Validate email.
     */
    public final void validateEmail() {
        if (email == null) {
            throw new ModelValidationException(this,"Email is null");
        }
        if (email.length() < EMAIL_LEN_MIN ||
            email.length() > EMAIL_LEN_MAX) {
            throw new ModelValidationException(this,"Email must be " +
                                               EMAIL_LEN_MIN + " to " +
                                               EMAIL_LEN_MAX +
                                               " characters in length");
        }
    }
}
