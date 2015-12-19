package org.unitedinternet.cosmo.acegisecurity.userdetails

import org.junit.Before
import org.junit.Test
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.unitedinternet.cosmo.dao.UserDao
import org.unitedinternet.cosmo.model.User

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

/**
 * @author Kamill Sokol
 */
class CosmoUserDetailsServiceTests {

    private static String USERNAME = "username"

    private UserDetailsService uut
    private User user

    @Before
    void before() {
        UserDao userDao = mock(UserDao.class)
        uut = new CosmoUserDetailsService(userDao)

        user = mock(User.class)

        when(user.getEmail()).thenReturn(USERNAME)
        when(user.getPassword()).thenReturn(USERNAME)
        when(userDao.getUserByEmail(USERNAME)).thenReturn(user)
    }

    @Test
    void hasRootRole() {
        when(user.getRoles()).thenReturn("ROLE_ROOT")

        UserDetails userDetails = uut.loadUserByUsername(USERNAME)

        assertThat(userDetails.getAuthorities(), everyItem(hasProperty("authority", is("ROLE_ROOT"))))
    }

    @Test
    void isLockedAccount() {
        when(user.isLocked()).thenReturn(true)

        UserDetails userDetails = uut.loadUserByUsername(USERNAME)

        assertThat(userDetails.isAccountNonLocked(), is(false));
    }
}
