/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo;

import org.junit.Ignore;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.HibEntityFactory;
import org.unitedinternet.cosmo.util.VersionFourGenerator;

@Ignore
public class TestHelper {

    static int useq = 0;

    private EntityFactory entityFactory = new HibEntityFactory(new VersionFourGenerator());

    /**
     * Makes dummy user.
     * @param username The username.
     * @param password The password.
     * @return The user.
     */
    public User makeDummyUser(String username,
                              String password) {
        if (username == null) {
            throw new IllegalArgumentException("username required");
        }
        
        if (password == null) {
            throw new IllegalArgumentException("password required");
        }

        User user = entityFactory.createUser();
        user.setUsername(username);
        user.setEmail(username + "@localhost");
        user.setPassword(password);

        return user;
    }

    /**
     * Makes dummy user.
     * @return The user.
     */
    public User makeDummyUser() {
        String serial = Integer.toString(++useq);;
        String username = "dummy" + serial;
        return makeDummyUser(username, username);
    }
}
