package org.unitedinternet.cosmo.dao.hibernate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.IntegrationTestSupport;
import carldav.repository.UserRepository;
import carldav.entity.User;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class HibernateUserRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testDeleteUser() {
        var user1 = new User();
        user1.setEmail("user1@user1.com");
        user1.setPassword("user1password");

        userRepository.save(user1);

        var queryUser1 = userRepository.findByEmailIgnoreCase("user1@user1.com");
        assertNotNull(queryUser1);
        userRepository.delete(queryUser1);

        queryUser1 = userRepository.findByEmailIgnoreCase("user1");
        assertNull(queryUser1);
    }
}
