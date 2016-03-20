/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dao.hibernate;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.IntegrationTestSupport;
import carldav.repository.UserRepository;
import carldav.entity.User;

public class HibernateUserRepositoryTest extends IntegrationTestSupport {
    
    @Autowired
    private UserRepository userRepository;

    @Test
    public void testDeleteUser() throws Exception {
        User user1 = new User();
        user1.setEmail("user1@user1.com");
        user1.setPassword("user1password");

        userRepository.save(user1);
        
        User queryUser1 = userRepository.findByEmailIgnoreCase("user1@user1.com");
        Assert.assertNotNull(queryUser1);
        userRepository.delete(queryUser1);

        queryUser1 = userRepository.findByEmailIgnoreCase("user1");
        Assert.assertNull(queryUser1);
    }
}
