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

    Boolean isLocked();

    /**
     */
    void validateRawPassword();

    String calculateEntityTag();

}