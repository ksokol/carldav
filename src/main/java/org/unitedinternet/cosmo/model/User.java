/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.model;

import java.util.Set;

/**
 * Represents a user in the cosmo server.
 */
public interface User extends AuditableObject{

    /**
     */
    String USERNAME_OVERLORD = "root";

    /**
     */
    int PASSWORD_LEN_MIN = 5;
    /**
     */
    int PASSWORD_LEN_MAX = 16;

    /**
     */
    String getUid();

    /**
     * @param uid
     */
    void setUid(String uid);

    /**
     */
    String getUsername();

    /**
     */
    void setUsername(String username);

    /**
     */
    String getOldUsername();

    /**
     */
    boolean isUsernameChanged();

    /**
     */
    String getPassword();

    /**
     */
    void setPassword(String password);

    /**
     */
    String getFirstName();

    /**
     */
    void setFirstName(String firstName);

    /**
     */
    String getLastName();

    /**
     */
    void setLastName(String lastName);

    /**
     */
    String getEmail();

    /**
     */
    void setEmail(String email);

    /**
     */
    String getOldEmail();

    /**
     */
    boolean isEmailChanged();

    /**
     */
    Boolean getAdmin();

    /**
     */
    void setAdmin(Boolean admin);

    /**
     */
    String getActivationId();

    /**
     */
    boolean isOverlord();

    /**
     */
    boolean isActivated();

    Boolean isLocked();

    /**
     */
    void validateRawPassword();

    Set<Preference> getPreferences();

    void addPreference(Preference preference);

    Preference getPreference(String key);

    void removePreference(String key);

    void removePreference(Preference preference);

    Set<CollectionSubscription> getCollectionSubscriptions();

    void addSubscription(CollectionSubscription subscription);

    /**
     * Get the CollectionSubscription with the specified displayName
     * @param displayname display name of subscription to return
     * @return subscription with specified display name
     */
    CollectionSubscription getSubscription(String displayname);

    /** */
    void removeSubscription(CollectionSubscription sub);

    String calculateEntityTag();

}