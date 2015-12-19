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
import org.unitedinternet.cosmo.service.impl.StandardUserService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class MockUser extends MockAuditableObject implements User {

    /**
     */
    public static final int USERNAME_LEN_MIN = 3;
    /**
     */
    public static final int USERNAME_LEN_MAX = 32;
    /**
     */
    public static final Pattern USERNAME_PATTERN =
        Pattern.compile("^[\\u0020-\\ud7ff\\ue000-\\ufffd&&[^\\u007f\\u003a;/\\\\]]+$");
    
    /**
     */
    public static final int FIRSTNAME_LEN_MIN = 1;
    /**
     */
    public static final int FIRSTNAME_LEN_MAX = 128;
    /**
     */
    public static final int LASTNAME_LEN_MIN = 1;
    /**
     */
    public static final int LASTNAME_LEN_MAX = 128;
    /**
     */
    public static final int EMAIL_LEN_MIN = 1;
    /**
     */
    public static final int EMAIL_LEN_MAX = 128;

    
    private String username;

    private String password;

    private String email;

    private Boolean admin;

    private Boolean locked;

    public MockUser() {
        admin = Boolean.FALSE;
        locked = Boolean.FALSE;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#getUsername()
     */
    /**
     * Gets username.
     * @return The username.
     */
    public final String getUsername() {
        return username;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#setUsername(java.lang.String)
     */
    /**
     * Sets username.
     * @param username The username.
     */
    public final void setUsername(final String username) {
        this.username = username;
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
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#getAdmin()
     */
    /**
     * Gets admin.
     * @return admin.
     */
    public final Boolean getAdmin() {
        return admin;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#isLocked()
     */
    /**
     * Is locked.
     * @return The boolean for it is locked or not.
     */
    public final Boolean isLocked() {
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
        if (username == null) {
            return super.hashCode();
        }
        else {
            return username.hashCode();
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#validate()
     */
    /**
     * Validate.
     */
    public final void validate() {
        validateUsername();
        validateEmail();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#validateUsername()
     */
    /**
     * Validate username.
     */
    public final void validateUsername() {
        if (username == null) {
            throw new ModelValidationException(this,"Username not specified");
        }
        if (username.length() < USERNAME_LEN_MIN ||
            username.length() > USERNAME_LEN_MAX) {
            throw new ModelValidationException(this,"Username must be " +
                                               USERNAME_LEN_MIN + " to " +
                                               USERNAME_LEN_MAX +
                                               " characters in length");
        }
        Matcher m = USERNAME_PATTERN.matcher(username);
        if (! m.matches()) {
            throw new ModelValidationException(this,"Username contains illegal " +
                                               "characters");
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#validateRawPassword()
     */
    /**
     * Validate raw password.
     */
    public final void validateRawPassword() {
        StandardUserService.validateRawPassword(this);
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

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#calculateEntityTag()
     */
    /**
     * Calculates entity tag.
     * @return The entity tag
     */
    public final String calculateEntityTag() {
        String username = getEmail() != null ? getEmail() : "-";
        String modTime = getModifiedDate() != null ?
            Long.valueOf(getModifiedDate().getTime()).toString() : "-";
        String etag = username + ":" + modTime;
        return encodeEntityTag(etag.getBytes());
    }
}
