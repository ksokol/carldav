package org.unitedinternet.cosmo.service.impl;

import carldav.entity.User;
import carldav.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.IntegrationTestSupport;
import org.unitedinternet.cosmo.service.UserService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardUserServiceTest extends IntegrationTestSupport {

    private int useq = 0;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService service;

    @Test
    void testGetUsers() {
        var u1 = makeDummyUser();
        userRepository.save(u1);
        var u2 = makeDummyUser();
        userRepository.save(u2);
        var u3 = makeDummyUser();
        userRepository.save(u3);

        var tmp = service.getUsers();
        var users = new ArrayList<>();

        tmp.forEach(users::add);

        assertTrue(users.contains(u1), "User 1 not found in users");
        assertTrue(users.contains(u2), "User 2 not found in users");
        assertTrue(users.contains(u3), "User 3 not found in users");
    }

    @Test
    void testGetUser() {
        var u1 = makeDummyUser();
        var username1 = u1.getEmail();
        userRepository.save(u1);

        var user = service.getUser(username1);
        assertNotNull(user, "User " + username1 + " null");
    }

    @Test
    void testCreateUser() {
        var u1 = makeDummyUser();
        var password = u1.getPassword();

        var user = service.createUser(u1);
        assertNotNull(userRepository.findByEmailIgnoreCase(u1.getEmail()), "User not stored");
        assertNotEquals(user.getPassword(), password, "Original and stored password are the same");
    }

    @Test
    void testRemoveUser() {
        var u1 = makeDummyUser();

        service.createUser(u1);
        service.removeUser(u1);

        assertNull(userRepository.findByEmailIgnoreCase(u1.getEmail()), "User not removed");
    }

    private User makeDummyUser() {
        var serial = Integer.toString(++useq);
        var username = "dummy" + serial;

        var user = new User();
        user.setEmail(username + "@localhost");
        user.setPassword(username);

        return user;
    }
}
